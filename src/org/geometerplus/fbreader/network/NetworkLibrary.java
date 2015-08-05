/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.*;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.network.*;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.fbreader.fbreader.options.SyncOptions;
import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.network.opds.OPDSSyncNetworkLink;
import org.geometerplus.fbreader.network.opds.OPDSLinkReader;
import org.geometerplus.fbreader.network.tree.*;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;

public class NetworkLibrary {
	public interface ChangeListener {
		public enum Code {
			InitializationFinished,
			InitializationFailed,
			SomeCode,
			/*
			ItemAdded,
			ItemRemoved,
			StatusChanged,
			*/
			SignedIn,
			Found,
			NotFound,
			EmptyCatalog,
			NetworkError
		}

		void onLibraryChanged(Code code, Object[] params);
	}

	private static NetworkLibrary ourInstance;

	public static NetworkLibrary Instance(SystemInfo systemInfo) {
		if (ourInstance == null) {
			ourInstance = new NetworkLibrary(systemInfo);
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

	public final SystemInfo SystemInfo;

	// that's important to keep this list synchronized
	// it can be used from background thread
	private final List<INetworkLink> myLinks =
		Collections.synchronizedList(new ArrayList<INetworkLink>());
	private final Set<ChangeListener> myListeners =
		Collections.synchronizedSet(new HashSet<ChangeListener>());
	private final Map<NetworkTree,NetworkItemsLoader> myLoaders =
		Collections.synchronizedMap(new HashMap<NetworkTree,NetworkItemsLoader>());

	private final Map<String,WeakReference<ZLImage>> myImageMap =
		Collections.synchronizedMap(new HashMap<String,WeakReference<ZLImage>>());

	public List<String> allIds() {
		final ArrayList<String> ids = new ArrayList<String>();
		synchronized (myLinks) {
			for (INetworkLink link : myLinks) {
				ids.add(link.getUrl(UrlInfo.Type.Catalog));
			}
		}
		return ids;
	}

	private ZLStringListOption myActiveIdsOption;
	private ZLStringListOption activeIdsOption() {
		if (myActiveIdsOption == null) {
			myActiveIdsOption = new ZLStringListOption(
				"Options",
				"ActiveIds",
				"",
				","
			);
		}
		return myActiveIdsOption;
	}

	public List<String> activeIds() {
		return activeIdsOption().getValue();
	}

	public void setLinkActive(INetworkLink link, boolean active) {
		if (link == null) {
			return;
		}
		setLinkActive(link.getUrl(UrlInfo.Type.Catalog), active);
		myChildrenAreInvalid = true;
	}

	public void setLinkActive(String id, boolean active) {
		if (id == null) {
			return;
		}
		final List<String> oldIds = activeIds();
		if (oldIds.contains(id) == active) {
			return;
		}

		final List<String> newIds;
		if (active) {
			newIds = new ArrayList<String>(oldIds.size() + 1);
			newIds.add(id);
			newIds.addAll(oldIds);
		} else {
			newIds = new ArrayList<String>(oldIds);
			newIds.remove(id);
		}
		activeIdsOption().setValue(newIds);
		invalidateChildren();
	}

	public void setActiveIds(List<String> ids) {
		activeIdsOption().setValue(ids);
		invalidateChildren();
	}

	List<INetworkLink> activeLinks() {
		final Map<String,INetworkLink> linksById = new TreeMap<String,INetworkLink>();
		synchronized (myLinks) {
			for (INetworkLink link : myLinks) {
				final String id = link.getUrl(UrlInfo.Type.Catalog);
				if (id != null) {
					linksById.put(id, link);
				}
			}
		}

		final List<INetworkLink> result = new LinkedList<INetworkLink>();
		INetworkLink syncLink = linksById.get(SyncOptions.DOMAIN);
		if (syncLink == null) {
			syncLink = new OPDSSyncNetworkLink(this);
		}
		result.add(syncLink);
		for (String id : activeIds()) {
			final INetworkLink link = linksById.get(id);
			if (link != null) {
				result.add(link);
			}
		}
		return result;
	}

	public INetworkLink getLinkByUrl(String url) {
		if (url == null) {
			return null;
		}
		synchronized (myLinks) {
			for (INetworkLink link : myLinks) {
				if (url.equals(link.getUrlInfo(UrlInfo.Type.Catalog).Url)) {
					return link;
				}
			}
		}
		return null;
	}

	public NetworkTree getCatalogTreeByUrl(String url) {
		for (FBTree tree : getRootTree().subtrees()) {
			if (tree instanceof NetworkCatalogRootTree) {
				final String cUrl =
					((NetworkCatalogTree)tree).getLink().getUrlInfo(UrlInfo.Type.Catalog).Url;
				if (url.equals(cUrl)) {
					return (NetworkTree)tree;
				}
			}
		}
		return null;
	}

	public NetworkTree getCatalogTreeByUrlAll(String url) {
		for (FBTree tree : getRootAllTree().subtrees()) {
			if (tree instanceof NetworkCatalogRootTree) {
				final String cUrl =
					((NetworkCatalogTree)tree).getLink().getUrlInfo(UrlInfo.Type.Catalog).Url;
				if (url.equals(cUrl)) {
					return (NetworkTree)tree;
				}
			}
		}
		return null;
	}

	public INetworkLink getLinkByStringId(String stringId) {
		synchronized (myLinks) {
			for (INetworkLink link : myLinks) {
				if (stringId.equals(link.getStringId())) {
					return link;
				}
			}
		}
		return null;
	}

	private final RootTree myRootAllTree = new RootTree(this, "@AllRoot", false);
	private final RootTree myRootTree = new RootTree(this, "@Root", false);
	private final RootTree myFakeRootTree = new RootTree(this, "@FakeRoot", true);

	private boolean myChildrenAreInvalid = true;
	private boolean myUpdateVisibility;

	private volatile boolean myIsInitialized;

	private final SearchItem mySearchItem = new AllCatalogsSearchItem(this);

	private NetworkLibrary(SystemInfo systemInfo) {
		SystemInfo = systemInfo;
	}

	public void clearExpiredCache(int hours) {
		final Queue<File> toVisit = new LinkedList<File>();
		final Set<File> processedDirs = new HashSet<File>();
		final File root = new File(SystemInfo.networkCacheDirectory());
		toVisit.add(root);
		processedDirs.add(root);

		while (!toVisit.isEmpty()) {
			final File[] children = toVisit.remove().listFiles();
			if (children == null) {
				continue;
			}
			for (File child : children) {
				if (child.isDirectory()) {
					if (!processedDirs.contains(child)) {
						toVisit.add(child);
						processedDirs.add(child);
					}
				} else {
					final long age = System.currentTimeMillis() - child.lastModified();
					if (age / 1000 / 60 / 60 >= hours) {
						child.delete();
					}
				}
			}
		}
	}

	public boolean isInitialized() {
		return myIsInitialized;
	}

	public synchronized void initialize(ZLNetworkContext nc) throws ZLNetworkException {
		if (myIsInitialized) {
			return;
		}

		try {
			myLinks.addAll(OPDSLinkReader.loadOPDSLinks(this, nc, OPDSLinkReader.CacheMode.LOAD));
		} catch (ZLNetworkException e) {
			removeAllLoadedLinks();
			fireModelChangedEvent(ChangeListener.Code.InitializationFailed, e.getMessage());
			throw e;
		}

		final NetworkDatabase db = NetworkDatabase.Instance();
		if (db != null) {
			myLinks.addAll(db.listLinks());
		}

		synchronize();

		myIsInitialized = true;
		fireModelChangedEvent(ChangeListener.Code.InitializationFinished);
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

	private volatile boolean myUpdateInProgress;
	private Object myUpdateLock = new Object();

	public void runBackgroundUpdate(final boolean force) {
		if (!isInitialized()) {
			return;
		}

		final Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					myUpdateInProgress = true;
					fireModelChangedEvent(ChangeListener.Code.SomeCode);
					runBackgroundUpdateInternal(force);
				} catch (ZLNetworkException e) {
					fireModelChangedEvent(ChangeListener.Code.NetworkError, e.getMessage());
				} finally {
					myUpdateInProgress = false;
					fireModelChangedEvent(ChangeListener.Code.SomeCode);
				}
			}
		});
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	private void runBackgroundUpdateInternal(boolean force) throws ZLNetworkException {
		final ZLNetworkContext quietContext = new QuietNetworkContext();
		synchronized (myUpdateLock) {
			final OPDSLinkReader.CacheMode mode =
				force ? OPDSLinkReader.CacheMode.CLEAR : OPDSLinkReader.CacheMode.UPDATE;
			final List<INetworkLink> loadedLinks = OPDSLinkReader.loadOPDSLinks(this, quietContext, mode);
			if (!loadedLinks.isEmpty()) {
				removeAllLoadedLinks();
				myLinks.addAll(loadedLinks);
			}
			invalidateChildren();

			// we create this copy to prevent long operations on synchronized list
			final List<INetworkLink> linksCopy = new ArrayList<INetworkLink>(myLinks);
			for (INetworkLink link : linksCopy) {
				if (link.getType() == INetworkLink.Type.Custom) {
					final ICustomNetworkLink customLink = (ICustomNetworkLink)link;
					if (force || customLink.isObsolete(12 * 60 * 60 * 1000)) { // 12 hours
						try {
							customLink.reloadInfo(quietContext, true, true);
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
				if (link instanceof IPredefinedNetworkLink &&
					((IPredefinedNetworkLink)link).servesHost(host)) {
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

	private void makeUpToDateRootAll() {
		myRootAllTree.clear();
		synchronized (myLinks) {
			for (INetworkLink link : myLinks) {
				for (FBTree t : myRootAllTree.subtrees()) {
					final INetworkLink l = ((NetworkTree)t).getLink();
					if (l != null && link.compareTo(l) <= 0) {
						break;
					}
				}
				new NetworkCatalogRootTree(myRootAllTree, link);
			}
		}
	}

	private void makeUpToDate() {
		firstTimeComputeActiveIds();

		final Map<INetworkLink,List<NetworkCatalogTree>> linkToTreeMap =
			new HashMap<INetworkLink,List<NetworkCatalogTree>>();
		for (FBTree tree : myRootTree.subtrees()) {
			if (tree instanceof NetworkCatalogTree) {
				final NetworkCatalogTree nTree = (NetworkCatalogTree)tree;
				final INetworkLink link = nTree.getLink();
				if (link != null) {
					List<NetworkCatalogTree> list = linkToTreeMap.get(link);
					if (list == null) {
						list = new LinkedList<NetworkCatalogTree>();
						linkToTreeMap.put(link, list);
					}
					list.add(nTree);
				}
			}
		}

		if (!myRootTree.hasChildren()) {
			//new RecentCatalogListTree(
			//	myRootTree, new RecentCatalogListItem(resource().getResource("recent"))
			//);
			new SearchCatalogTree(myRootTree, mySearchItem);
			// normal catalog items to be inserted here
			new ManageCatalogsItemTree(myRootTree);
			new AddCustomCatalogItemTree(myRootTree);
		}

		boolean changedCatalogsList = false;
		int index = 1;
		for (INetworkLink link : activeLinks()) {
			final List<NetworkCatalogTree> trees = linkToTreeMap.remove(link);
			if (trees != null) {
				for (NetworkCatalogTree t : trees) {
					myRootTree.moveSubtree(t, index++);
				}
			} else {
				new NetworkCatalogRootTree(myRootTree, link, index++);
				changedCatalogsList = true;
			}
		}

		for (List<NetworkCatalogTree> trees : linkToTreeMap.values()) {
			for (NetworkCatalogTree t : trees) {
				t.removeSelf();
				changedCatalogsList = true;
			}
		}

		if (changedCatalogsList) {
			mySearchItem.setPattern(null);
		}

		fireModelChangedEvent(ChangeListener.Code.SomeCode);
	}

	private void firstTimeComputeActiveIds() {
		final ZLBooleanOption firstLaunchOption = new ZLBooleanOption(
			"Options",
			"firstLaunch",
			true
		);
		if (!firstLaunchOption.getValue()) {
			return;
		}

		final ArrayList<String> ids = new ArrayList<String>();
		// language codes were saved in this options in versions before 1.9
		final Collection<String> codes = new ZLStringListOption(
			"Options",
			"ActiveLanguages",
			ZLibrary.Instance().defaultLanguageCodes(),
			","
		).getValue();
		synchronized (myLinks) {
			for (INetworkLink link : myLinks) {
				if (link instanceof ICustomNetworkLink ||
					codes.contains(link.getLanguage())) {
					ids.add(link.getUrl(UrlInfo.Type.Catalog));
				}
			}
		}
		setActiveIds(ids);

		firstLaunchOption.setValue(false);
	}

	private void updateVisibility() {
		for (FBTree tree : myRootTree.subtrees()) {
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
			makeUpToDateRootAll();
		}
		if (myUpdateVisibility) {
			myUpdateVisibility = false;
			updateVisibility();
		}
		fireModelChangedEvent(ChangeListener.Code.SomeCode);
	}

	public NetworkTree getRootTree() {
		return myRootTree;
	}

	public NetworkTree getRootAllTree() {
		return myRootAllTree;
	}

	public NetworkBookTree getFakeBookTree(NetworkBookItem book) {
		final String id = book.getStringId();
		for (FBTree tree : myFakeRootTree.subtrees()) {
			if (tree instanceof NetworkBookTree &&
				id.equals(tree.getUniqueKey().Id)) {
				return (NetworkBookTree)tree;
			}
		}
		return new NetworkBookTree(myFakeRootTree, book, true);
	}

	public BasketCatalogTree getFakeBasketTree(BasketItem item) {
		final String id = item.getStringId();
		for (FBTree tree : myFakeRootTree.subtrees()) {
			if (tree instanceof BasketCatalogTree &&
				id.equals(tree.getUniqueKey().Id)) {
				return (BasketCatalogTree)tree;
			}
		}
		return new BasketCatalogTree(myFakeRootTree, item);
	}

	public NetworkCatalogTree getFakeCatalogTree(NetworkCatalogItem item) {
		final String id = item.getStringId();
		for (FBTree tree : myFakeRootTree.subtrees()) {
			if (tree instanceof NetworkCatalogTree &&
				id.equals(tree.getUniqueKey().Id)) {
				return (NetworkCatalogTree)tree;
			}
		}
		return new NetworkCatalogTree(myFakeRootTree, item.Link, item, 0);
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
		return parentTree != null ? (NetworkTree)parentTree.getSubtree(key.Id) : null;
	}

	public void addCustomLink(ICustomNetworkLink link) {
		final int id = link.getId();
		if (id == ICustomNetworkLink.INVALID_ID) {
			synchronized (myLinks) {
				final INetworkLink existing = getLinkByUrl(link.getUrl(UrlInfo.Type.Catalog));
				if (existing == null) {
					myLinks.add(link);
				} else {
					setLinkActive(existing, true);
					fireModelChangedEvent(ChangeListener.Code.SomeCode);
					return;
				}
			}
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
		setLinkActive(link, true);
		fireModelChangedEvent(ChangeListener.Code.SomeCode);
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
	/*private*/ public void fireModelChangedEvent(ChangeListener.Code code, Object ... params) {
		synchronized (myListeners) {
			for (ChangeListener l : myListeners) {
				l.onLibraryChanged(code, params);
			}
		}
	}

	public final void storeLoader(NetworkTree tree, NetworkItemsLoader loader) {
		myLoaders.put(tree, loader);
	}

	public final NetworkItemsLoader getStoredLoader(NetworkTree tree) {
		return tree != null ? myLoaders.get(tree) : null;
	}

	public final boolean isUpdateInProgress() {
		return myUpdateInProgress;
	}

	public final void startLoading(NetworkCatalogItem item) {
		if (item != null) {
			item.UpdatingInProgress = true;
			fireModelChangedEvent(ChangeListener.Code.SomeCode);
		}
	}

	public final void stopLoading(NetworkCatalogItem item) {
		if (item != null) {
			item.UpdatingInProgress = false;
			fireModelChangedEvent(ChangeListener.Code.SomeCode);
		}
	}

	public boolean isLoadingInProgress(NetworkTree tree) {
		return
			(tree instanceof NetworkCatalogTree &&
				((NetworkCatalogTree)tree).Item.UpdatingInProgress) ||
			getStoredLoader(tree) != null;
	}

	public final void removeStoredLoader(NetworkTree tree) {
		myLoaders.remove(tree);
	}

	public ZLImage getImageByUrl(String url, MimeType mimeType) {
		synchronized (myImageMap) {
			final WeakReference<ZLImage> ref = myImageMap.get(url);
			if (ref != null) {
				final ZLImage image = ref.get();
				if (image != null) {
					return image;
				}
			}
			final ZLImage image = new NetworkImage(url, SystemInfo);
			myImageMap.put(url, new WeakReference<ZLImage>(image));
			return image;
		}
	}
}
