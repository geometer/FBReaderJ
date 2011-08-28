/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.network;

import java.util.*;

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;
import org.geometerplus.zlibrary.core.language.ZLLanguageUtil;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.network.tree.*;
import org.geometerplus.fbreader.network.opds.OPDSLinkReader;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;

public class NetworkLibrary {
	public interface ChangeListener {
		public enum Code {
			SomeCode
			/*
			ItemAdded,
			ItemRemoved,
			StatusChanged,
			Found,
			NotFound
			*/
		}

		void onLibraryChanged(Code code);
	}

	private static NetworkLibrary ourInstance;

	public static NetworkLibrary Instance() {
		if (ourInstance == null) {
			ourInstance = new NetworkLibrary();
		}
		return ourInstance;
	}

	public static ZLResource resource() {
		return ZLResource.resource("networkLibrary");
	}

	public interface OnNewLinkListener {
		void onNewLink(INetworkLink link);
	}

	public final ZLStringOption NetworkSearchPatternOption =
		new ZLStringOption("NetworkSearch", "Pattern", "");

	// that's important to keep this list synchronized
	// it can be used from background thread
	private final List<INetworkLink> myLinks =
		Collections.synchronizedList(new ArrayList<INetworkLink>());
	private final Set<ChangeListener> myListeners =
		Collections.synchronizedSet(new HashSet<ChangeListener>());

	public List<String> languageCodes() {
		final TreeSet<String> languageSet = new TreeSet<String>();
		synchronized (myLinks) {
			for (INetworkLink link : myLinks) {
				languageSet.add(link.getLanguage());
			}
		}
		return new ArrayList<String>(languageSet);
	}

	private ZLStringOption myActiveLanguageCodesOption;
	private ZLStringOption activeLanguageCodesOption() {
 		if (myActiveLanguageCodesOption == null) {
			final TreeSet<String> defaultCodes = new TreeSet<String>(new ZLLanguageUtil.CodeComparator());
			defaultCodes.addAll(ZLibrary.Instance().defaultLanguageCodes());
			myActiveLanguageCodesOption =
				new ZLStringOption(
					"Options",
					"ActiveLanguages",
					commaSeparatedString(defaultCodes)
				);
		}
		return myActiveLanguageCodesOption;
	}

	public Collection<String> activeLanguageCodes() {
		return Arrays.asList(activeLanguageCodesOption().getValue().split(","));
	}

	public void setActiveLanguageCodes(Collection<String> codes) {
		final TreeSet<String> allCodes = new TreeSet<String>(new ZLLanguageUtil.CodeComparator());
		allCodes.addAll(ZLibrary.Instance().defaultLanguageCodes());
		allCodes.removeAll(languageCodes());
		allCodes.addAll(codes);
		activeLanguageCodesOption().setValue(commaSeparatedString(allCodes));
		invalidateChildren();
	}

	private String commaSeparatedString(Collection<String> codes) {
		final StringBuilder builder = new StringBuilder();
		for (String code : codes) {
			builder.append(code);
			builder.append(",");
		}
		if (builder.length() > 0) {
			builder.delete(builder.length() - 1, builder.length());
		}
		return builder.toString();
	}

	private List<INetworkLink> activeLinks() {
		final LinkedList<INetworkLink> filteredList = new LinkedList<INetworkLink>();
		final Collection<String> codes = activeLanguageCodes();
		synchronized (myLinks) {
			for (INetworkLink link : myLinks) {
				if (link instanceof ICustomNetworkLink ||
					codes.contains(link.getLanguage())) {
					filteredList.add(link);
				}
			}
		}
		return filteredList;
	}

	public INetworkLink getLinkByUrl(String url) {
		synchronized (myLinks) {
			for (INetworkLink link : myLinks) {
				if (url.equals(link.getUrlInfo(UrlInfo.Type.Catalog).Url)) {
					return link;
				}
			}
		}
		return null;
	}

	public INetworkLink getLinkBySiteName(String siteName) {
		synchronized (myLinks) {
			for (INetworkLink link : myLinks) {
				if (siteName.equals(link.getSiteName())) {
					return link;
				}
			}
		}
		return null;
	}

	private final RootTree myRootTree = new RootTree("@Root", false);
	private final RootTree myFakeRootTree = new RootTree("@FakeRoot", true);

	private boolean myChildrenAreInvalid = true;
	private boolean myUpdateVisibility;

	private volatile boolean myIsInitialized;

	private NetworkLibrary() {
	}

	public boolean isInitialized() {
		return myIsInitialized;
	}

	public synchronized void initialize() throws ZLNetworkException {
		if (myIsInitialized) {
			return;
		}

		try {
			myLinks.addAll(OPDSLinkReader.loadOPDSLinks(OPDSLinkReader.CacheMode.LOAD));
		} catch (ZLNetworkException e) {
			removeAllLoadedLinks();
			throw e;
		}

		final NetworkDatabase db = NetworkDatabase.Instance();
		if (db != null) {
			myLinks.addAll(db.listLinks());
		}

		synchronize();

		myIsInitialized = true;
	}

	private void removeAllLoadedLinks() {
		final LinkedList<INetworkLink> toRemove = new LinkedList<INetworkLink>();
		synchronized (myLinks) {
			for (INetworkLink link : myLinks) {
				if (!(link instanceof ICustomNetworkLink)) {
					toRemove.add(link);
				}
			}
		}
		myLinks.removeAll(toRemove);
	}

	/*private void testDate(ATOMDateConstruct date1, ATOMDateConstruct date2) {
		String sign = " == ";
		final int diff = date1.compareTo(date2);
		if (diff > 0) {
			sign = " > ";
		} else if (diff < 0) {
			sign = " < ";
		}
		Log.w("FBREADER", "" + date1 + sign + date2);
	}*/

	private Object myBackgroundLock = new Object();

	// This method must be called from background thread
	public void runBackgroundUpdate(boolean clearCache) throws ZLNetworkException {
		synchronized (myBackgroundLock) {
			final OPDSLinkReader.CacheMode mode =
				clearCache ? OPDSLinkReader.CacheMode.CLEAR : OPDSLinkReader.CacheMode.UPDATE;
			final List<INetworkLink> loadedLinks = OPDSLinkReader.loadOPDSLinks(mode);
			if (!loadedLinks.isEmpty()) {
				removeAllLoadedLinks();
				myLinks.addAll(loadedLinks);
			}
			invalidateChildren();

			// we create this copy to prevent long operations on synchronized list
			final List<INetworkLink> linksCopy = new ArrayList<INetworkLink>(myLinks);
			for (INetworkLink link : linksCopy) {
				if (link instanceof ICustomNetworkLink) {
					final ICustomNetworkLink customLink = (ICustomNetworkLink)link;
					if (customLink.isObsolete(12 * 60 * 60 * 1000)) { // 12 hours
						try {
							customLink.reloadInfo(true);
							NetworkDatabase.Instance().saveLink(customLink);
						} catch (Throwable t) {
							// ignore
						}
					}
				}
			}

			synchronize();
		}
	}

	public String rewriteUrl(String url, boolean externalUrl) {
		final String host = ZLNetworkUtil.hostFromUrl(url).toLowerCase();
		synchronized (myLinks) {
			for (INetworkLink link : myLinks) {
				if (host.contains(link.getSiteName())) {
					url = link.rewriteUrl(url, externalUrl);
				}
			}
		}
		return url;
	}

	private void invalidateChildren() {
		myChildrenAreInvalid = true;
	}

	public void invalidateVisibility() {
		myUpdateVisibility = true;
	}

//	private static boolean linkIsChanged(INetworkLink link) {
//		return
//			link instanceof ICustomNetworkLink &&
//			((ICustomNetworkLink)link).hasChanges();
//	}

//	private static void makeValid(INetworkLink link) {
//		if (link instanceof ICustomNetworkLink) {
//			((ICustomNetworkLink)link).resetChanges();
//		}
//	}

	private void makeUpToDate() {
		final SortedSet<INetworkLink> linkSet = new TreeSet<INetworkLink>(activeLinks());

		final LinkedList<FBTree> toRemove = new LinkedList<FBTree>();

		// we do remove sum tree items:
		for (FBTree t : myRootTree.subTrees()) {
			if (t instanceof NetworkCatalogTree) {
				final INetworkLink link = ((NetworkCatalogTree)t).Item.Link;
				if (link != null) {
					if (!linkSet.contains(link)) {
                        // 1. links not listed in activeLinks list right now
						toRemove.add(t);
					} else if (link instanceof ICustomNetworkLink &&
								((ICustomNetworkLink)link).hasChanges()) {
                        // 2. custom links that were changed
						toRemove.add(t);
					} else {
						linkSet.remove(link);
					}
				}
			} else {
				// 3. non-catalog nodes
				toRemove.add(t);
			}
		}
		for (FBTree tree : toRemove) {
			tree.removeSelf();
		}

		// we do add new network catalog items
		for (INetworkLink link : linkSet) {
			int index = 0;
			for (FBTree t : myRootTree.subTrees()) {
				final INetworkLink l = ((NetworkCatalogTree)t).Item.Link;
				if (l != null && link.compareTo(l) <= 0) {
					break;
				}
				++index;
			}
			new NetworkCatalogRootTree(myRootTree, link, index);
		}
		// we do add non-catalog items
		new AddCustomCatalogItemTree(myRootTree);

		fireModelChangedEvent(ChangeListener.Code.SomeCode);
	}

	private void updateVisibility() {
		for (FBTree tree : myRootTree.subTrees()) {
			if (tree instanceof NetworkCatalogTree) {
				((NetworkCatalogTree)tree).updateVisibility();
			}
		}
		fireModelChangedEvent(ChangeListener.Code.SomeCode);
	}

	public void synchronize() {
		if (myChildrenAreInvalid) {
			myChildrenAreInvalid = false;
			makeUpToDate();
		}
		if (myUpdateVisibility) {
			myUpdateVisibility = false;
			updateVisibility();
		}
	}

	public NetworkTree getRootTree() {
		return myRootTree;
	}

	public NetworkCatalogTree getFakeCatalogTree(NetworkCatalogItem item) {
		final String id = item.getStringId();
		for (FBTree tree : myFakeRootTree.subTrees()) {
			final NetworkCatalogTree ncTree = (NetworkCatalogTree)tree;
			if (id.equals(ncTree.getUniqueKey().Id)) {
				return ncTree;
			}
		}
		return new NetworkCatalogTree(myFakeRootTree, item, 0);
	}

	public NetworkTree getTreeByKey(NetworkTree.Key key) {
		if (key == null) {
			return null;
		}
		if (key.Parent == null) {
			if (key.equals(myRootTree.getUniqueKey())) {
				return myRootTree;
			}
			if (key.equals(myFakeRootTree.getUniqueKey())) {
				return myFakeRootTree;
			}
			return null;
		}
		final NetworkTree parentTree = getTreeByKey(key.Parent);
		if (parentTree == null) {
			return null;
		}
		return parentTree != null ? (NetworkTree)parentTree.getSubTree(key.Id) : null;
	}

	public void simpleSearch(String pattern, final NetworkOperationData.OnNewItemListener listener) throws ZLNetworkException {
		LinkedList<ZLNetworkRequest> requestList = new LinkedList<ZLNetworkRequest>();
		LinkedList<NetworkOperationData> dataList = new LinkedList<NetworkOperationData>();

		final NetworkOperationData.OnNewItemListener synchronizedListener = new NetworkOperationData.OnNewItemListener() {
			public synchronized void onNewItem(INetworkLink link, NetworkItem item) {
				listener.onNewItem(link, item);
			}
			public synchronized boolean confirmInterrupt() {
				return listener.confirmInterrupt();
			}
			public synchronized void commitItems(INetworkLink link) {
				listener.commitItems(link);
			}
		};

		for (INetworkLink link : activeLinks()) {
			final NetworkOperationData data = link.createOperationData(synchronizedListener);
			final ZLNetworkRequest request = link.simpleSearchRequest(pattern, data);
			if (request != null) {
				dataList.add(data);
				requestList.add(request);
			}
		}

		while (requestList.size() != 0) {
			ZLNetworkManager.Instance().perform(requestList);

			requestList.clear();

			if (listener.confirmInterrupt()) {
				return;
			}
			for (NetworkOperationData data : dataList) {
				ZLNetworkRequest request = data.resume();
				if (request != null) {
					requestList.add(request);
				}
			}
		}
	}

	public void addCustomLink(ICustomNetworkLink link) {
		final int id = link.getId();
		if (id == ICustomNetworkLink.INVALID_ID) {
			myLinks.add(link);
		} else {
			synchronized (myLinks) {
				for (int i = myLinks.size() - 1; i >= 0; --i) {
					final INetworkLink l = myLinks.get(i);
					if (l instanceof ICustomNetworkLink && ((ICustomNetworkLink)l).getId() == id) {
						myLinks.set(i, link);
						break;
					}
				}
			}
		}
		NetworkDatabase.Instance().saveLink(link);
		invalidateChildren();
	}

	public void removeCustomLink(ICustomNetworkLink link) {
		myLinks.remove(link);
		NetworkDatabase.Instance().deleteLink(link);
		invalidateChildren();
	}

	public void addChangeListener(ChangeListener listener) {
		myListeners.add(listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		myListeners.remove(listener);
	}

	// TODO: change to private
	/*private*/ public void fireModelChangedEvent(ChangeListener.Code code) {
		synchronized (myListeners) {
			for (ChangeListener l : myListeners) {
				l.onLibraryChanged(code);
			}
		}
	}
}
