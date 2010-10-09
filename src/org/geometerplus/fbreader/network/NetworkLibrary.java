/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.network.tree.*;
import org.geometerplus.fbreader.network.opds.OPDSLinkReader;


public class NetworkLibrary {
	private static NetworkLibrary ourInstance;

	public static NetworkLibrary Instance() {
		if (ourInstance == null) {
			ourInstance = new NetworkLibrary();
		}
		return ourInstance;
	}

	private static class CompositeList extends AbstractSequentialList<INetworkLink> {

		private final ArrayList<ArrayList<? extends INetworkLink>> myLists;
		private Comparator<INetworkLink> myComparator;

		public CompositeList(ArrayList<ArrayList<? extends INetworkLink>> lists,
				Comparator<INetworkLink> comparator) {
			myLists = lists;
			myComparator = comparator;
		}

		private class Iterator implements ListIterator<INetworkLink> {
			private int myIndex;
			private ArrayList<Integer> myPositions;

			private final INetworkLink getNextByIndex(int index) {
				final int position = myPositions.get(index);
				return (position < myLists.get(index).size()) ?
						myLists.get(index).get(position) :
						null;
			}

			private final INetworkLink getPrevByIndex(int index) {
				final int position = myPositions.get(index);
				return (position > 0) ?
						myLists.get(index).get(position - 1) :
						null;
			}

			public Iterator() {
				myPositions = new ArrayList<Integer>(Collections.nCopies(myLists.size(), 0));
			}

			public Iterator(Iterator it) {
				myIndex = it.myIndex;
				myPositions = new ArrayList<Integer>(it.myPositions);
			}

			public boolean hasNext() {
				return myIndex < size();
			}

			public boolean hasPrevious() {
				return myIndex > 0;
			}

			public int nextIndex() {
				return myIndex;
			}

			public int previousIndex() {
				return myIndex - 1;
			}

			public INetworkLink next() {
				final int size = myLists.size();
				if (size == 0) {
					throw new NoSuchElementException();
				}
				int nextIndex = -1;
				INetworkLink nextLink = null;;
				for (nextIndex = 0; nextIndex < size; ++nextIndex) {
					nextLink = getNextByIndex(nextIndex);
					if (nextLink != null) {
						break;
					}
				}
				if (nextLink == null) {
					throw new NoSuchElementException();
				}
				for (int i = nextIndex + 1; i < size; ++i) {
					INetworkLink link = getNextByIndex(i);
					if (link != null && myComparator.compare(link, nextLink) < 0) {
						nextLink = link;
						nextIndex = i;
					}
				}
				myPositions.set(nextIndex, myPositions.get(nextIndex) + 1);
				++myIndex;
				return nextLink;
			}

			public INetworkLink previous() {
				final int size = myLists.size();
				if (size == 0) {
					throw new NoSuchElementException();
				}
				int prevIndex = -1;
				INetworkLink prevLink = null;;
				for (prevIndex = 0; prevIndex < size; ++prevIndex) {
					prevLink = getPrevByIndex(prevIndex);
					if (prevLink != null) {
						break;
					}
				}
				if (prevLink == null) {
					throw new NoSuchElementException();
				}
				for (int i = prevIndex + 1; i < size; ++i) {
					INetworkLink link = getPrevByIndex(i);
					if (link != null && myComparator.compare(link, prevLink) >= 0) {
						prevLink = link;
						prevIndex = i;
					}
				}
				myPositions.set(prevIndex, myPositions.get(prevIndex) - 1);
				--myIndex;
				return prevLink;
			}

			public void add(INetworkLink arg0) { throw new UnsupportedOperationException(); }
			public void remove() { throw new UnsupportedOperationException(); }
			public void set(INetworkLink arg0) { throw new UnsupportedOperationException(); }
		};

		@Override
		public ListIterator<INetworkLink> listIterator(int location) {
			if (location < 0 || location > size()) {
				throw new IndexOutOfBoundsException();
			}
			Iterator it = new Iterator();
			while (location-- > 0) {
				it.next();
			}
			return it;
		}

		// returns a copy of iterator
		public ListIterator<INetworkLink> listIterator(ListIterator<INetworkLink> it) {
			return new Iterator((Iterator)it);
		}

		@Override
		public int size() {
			int size = 0;
			for (ArrayList<? extends INetworkLink> list: myLists) {
				size += list.size();
			}
			return size;
		}
	}

	private static class LinksComparator implements Comparator<INetworkLink> {
		private static String filterLinkTitle(String title) {
			for (int index = 0; index < title.length(); ++index) {
				final char ch = title.charAt(index);
				if (ch < 128 && Character.isLetter(ch)) {
					return title.substring(index);
				}
			}
			return title;
		}

		public int compare(INetworkLink link1, INetworkLink link2) {
			final String title1 = filterLinkTitle(link1.getTitle());
			final String title2 = filterLinkTitle(link2.getTitle());
			return title1.compareToIgnoreCase(title2);
		}
	}

	
	public interface OnNewLinkListener {
		void onNewLink(INetworkLink link);
	}


	public final ZLStringOption NetworkSearchPatternOption = new ZLStringOption("NetworkSearch", "Pattern", "");

	private final ArrayList<INetworkLink> myLoadedLinks = new ArrayList<INetworkLink>();
	private final ArrayList<ICustomNetworkLink> myCustomLinks = new ArrayList<ICustomNetworkLink>();
	private final CompositeList myLinks;

	private final RootTree myRootTree = new RootTree();

	private boolean myUpdateChildren = true;
	private boolean myInvalidateChildren;
	private boolean myUpdateVisibility;

	private NetworkLibrary() {
		ArrayList<ArrayList<? extends INetworkLink>> linksList = new ArrayList<ArrayList<? extends INetworkLink>>();
		linksList.add(myLoadedLinks);
		linksList.add(myCustomLinks);
		myLinks = new CompositeList(linksList, new LinksComparator());
	}

	public void initialize() throws ZLNetworkException {
		final LinksComparator comparator = new LinksComparator(); 

		try {
			OPDSLinkReader.loadOPDSLinks(OPDSLinkReader.CACHE_LOAD, new OnNewLinkListener() {
				public void onNewLink(INetworkLink link) {
					addLinkInternal(myLoadedLinks, link, comparator);
				}
			});
		} catch (ZLNetworkException e) {
			synchronized (myLinks) {
				myLoadedLinks.clear();
			}
			throw e;
		}

		NetworkDatabase.Instance().loadCustomLinks(
			new NetworkDatabase.ICustomLinksHandler() {
				public void handleCustomLinkData(int id, String siteName,
						String title, String summary, String icon, Map<String, String> links) {
					final ICustomNetworkLink link = OPDSLinkReader.createCustomLink(id, siteName, title, summary, icon, links);
					if (link != null) {
						addLinkInternal(myCustomLinks, link, comparator);
						link.setSaveLinkListener(myChangesListener);
					}
				}
			}
		);

		/*testDate(new ATOMUpdated(2010,  1,  1,  1,  0,  0,  0,  2,  0),
				 new ATOMUpdated(2009, 12, 31, 23,  0,  0,  0,  0,  0));
		testDate(new ATOMUpdated(2010, 12, 31, 23, 40,  0,  0, -1, -30),
				 new ATOMUpdated(2011,  1,  1,  1, 10,  0,  0,  0,  0));
		testDate(new ATOMUpdated(2010,  1, 31, 23, 40,  0,  0, -1, -30),
				 new ATOMUpdated(2010,  2,  1,  1, 10,  0,  0,  0,  0));
		testDate(new ATOMUpdated(2010,  2, 28, 23, 40,  0,  0, -1, -30),
				 new ATOMUpdated(2010,  3,  1,  1, 10,  0,  0,  0,  0));
		testDate(new ATOMUpdated(2012,  2, 28, 23, 40,  0,  0, -1, -30),
				 new ATOMUpdated(2012,  2, 29,  1, 10,  0,  0,  0,  0));
		testDate(new ATOMUpdated(2012,  2, 15, 23, 40,  0,  0, -1, -30),
				 new ATOMUpdated(2012,  2, 16,  1, 10,  0,  0,  0,  0));
		testDate(new ATOMUpdated(2012,  2, 15, 23, 40,  1,  0,  3, 30),
				 new ATOMUpdated(2012,  2, 15, 23, 40,  0,  0,  3, 30));
		testDate(new ATOMUpdated(2012,  2, 15, 23, 40,  0,  0,  3, 30),
				 new ATOMUpdated(2012,  2, 15, 23, 40,  1,  0,  3, 30));
		testDate(new ATOMUpdated(2012,  2, 15, 23, 40,  0,  0.001f,  3, 30),
				 new ATOMUpdated(2012,  2, 15, 23, 40,  0,  0,  3, 30));*/
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

	private ArrayList<INetworkLink> myBackgroundLinks;
	private Object myBackgroundLock = new Object();

	// This method must be called from background thread
	public void runBackgroundUpdate(boolean clearCache) throws ZLNetworkException {
		synchronized (myBackgroundLock) {
			myBackgroundLinks = new ArrayList<INetworkLink>();

			final int cacheMode = clearCache ? OPDSLinkReader.CACHE_CLEAR : OPDSLinkReader.CACHE_UPDATE;
			try {
				OPDSLinkReader.loadOPDSLinks(cacheMode, new OnNewLinkListener() {
					public void onNewLink(INetworkLink link) {
						myBackgroundLinks.add(link);
					}
				});
			} catch (ZLNetworkException e) {
				myBackgroundLinks = null;
				throw e;
			} finally {
				if (myBackgroundLinks != null) {
					if (myBackgroundLinks.isEmpty()) {
						myBackgroundLinks = null;
					} else {
						Collections.sort(myBackgroundLinks, new LinksComparator());
					}
				}
			}
		}
	}

	// This method MUST be called from main thread
	// This method has effect only when runBackgroundUpdate method has returned null.
	//
	// synchronize() method MUST be called after this method
	public void finishBackgroundUpdate() {
		synchronized (myBackgroundLock) {
			if (myBackgroundLinks == null) {
				return;
			}
			synchronized (myLinks) {
				myLoadedLinks.clear();
				myLoadedLinks.addAll(myBackgroundLinks);
				updateChildren();
			}
		}
	}


	public String rewriteUrl(String url, boolean externalUrl) {
		final String host = ZLNetworkUtil.hostFromUrl(url).toLowerCase();
		synchronized (myLinks) {
			for (INetworkLink link: myLinks) {
				if (host.contains(link.getSiteName())) {
					url = link.rewriteUrl(url, externalUrl);
				}
			}
		}
		return url;
	}

	public void invalidateChildren() {
		myInvalidateChildren = true;
	}

	public void updateChildren() {
		myUpdateChildren = true;
	}

	public void invalidateVisibility() {
		myUpdateVisibility = true;
	}


	private static boolean linksEqual(INetworkLink l1, INetworkLink l2) {
		return l1 == l2 || l1.getSiteName().equals(l2.getSiteName());
	}

	private static boolean linkIsInvalid(INetworkLink link, INetworkLink nodeLink) {
		if (link instanceof ICustomNetworkLink) {
			if (link != nodeLink) {
				throw new RuntimeException("Two equal custom links!!! That's impossible");
			}
			return ((ICustomNetworkLink) link).hasChanges();
		}
		return !link.equals(nodeLink);
	}

	private static void makeValid(INetworkLink link) {
		if (link instanceof ICustomNetworkLink) {
			((ICustomNetworkLink) link).resetChanges();
		}
	}

	private void makeUpToDate() {
		final LinkedList<FBTree> toRemove = new LinkedList<FBTree>();

		ListIterator<FBTree> nodeIterator = myRootTree.subTrees().listIterator();
		FBTree currentNode = null;
		int nodeCount = 0;

		synchronized (myLinks) {
			ListIterator<INetworkLink> it = myLinks.listIterator();
			while (it.hasNext()) {
				INetworkLink link = it.next();
				/*if (!link.OnOption.getValue()) {
					continue;
				}*/
				boolean processed = false;
				while (currentNode != null || nodeIterator.hasNext()) {
					if (currentNode == null) {
						currentNode = nodeIterator.next();
					}
					if (!(currentNode instanceof NetworkCatalogTree)) {
						currentNode = null;
						++nodeCount;
						continue;
					}
					final INetworkLink nodeLink = ((NetworkCatalogTree) currentNode).Item.Link;
					if (linksEqual(link, nodeLink)) {
						if (linkIsInvalid(link, nodeLink)) {
							toRemove.add(currentNode);
						} else {
							processed = true;
						}
						currentNode = null;
						++nodeCount;
						break;
					} else {
						INetworkLink newNodeLink = null;
						ListIterator<INetworkLink> jt = myLinks.listIterator(it);
						while (jt.hasNext()) {
							final INetworkLink jlnk = jt.next();
							if (linksEqual(nodeLink, jlnk)) {
								newNodeLink = jlnk;
								break;
							}
						}
						if (newNodeLink == null || linkIsInvalid(newNodeLink, nodeLink)) {
							toRemove.add(currentNode);
							currentNode = null;
							++nodeCount;
						} else {
							break;
						}
					}
				}
				if (!processed) {
					makeValid(link);
					final int nextIndex = nodeIterator.nextIndex();
					new NetworkCatalogRootTree(myRootTree, link, nodeCount++).Item.onDisplayItem();
					nodeIterator = myRootTree.subTrees().listIterator(nextIndex + 1);
				}
			}
		}

		while (currentNode != null || nodeIterator.hasNext()) {
			if (currentNode == null) {
				currentNode = nodeIterator.next();
			}
			if (currentNode instanceof NetworkCatalogTree) {
				toRemove.add(currentNode);
			}
			currentNode = null;
		}

		for (FBTree tree: toRemove) {
			tree.removeSelf();
		}
	}

	private void updateVisibility() {
		for (FBTree tree: myRootTree.subTrees()) {
			if (!(tree instanceof NetworkCatalogTree)) {
				continue;
			}
			((NetworkCatalogTree) tree).updateVisibility();
		}
	}

	public void synchronize() {
		if (myUpdateChildren || myInvalidateChildren) {
			if (myInvalidateChildren) {
				final LinksComparator cmp = new LinksComparator();
				//Collections.sort(myLoadedLinks, cmp); // this collection is always sorted
				Collections.sort(myCustomLinks, cmp);
			}
			myUpdateChildren = false;
			myInvalidateChildren = false;
			makeUpToDate();
		}
		if (myUpdateVisibility) {
			myUpdateVisibility = false;
			updateVisibility();
		}
	}

	public NetworkTree getTree() {
		return myRootTree;
	}


	public void simpleSearch(String pattern, final NetworkOperationData.OnNewItemListener listener) throws ZLNetworkException {
		LinkedList<ZLNetworkRequest> requestList = new LinkedList<ZLNetworkRequest>();
		LinkedList<NetworkOperationData> dataList = new LinkedList<NetworkOperationData>();

		final NetworkOperationData.OnNewItemListener synchronizedListener = new NetworkOperationData.OnNewItemListener() {
			public synchronized void onNewItem(INetworkLink link, NetworkLibraryItem item) {
				listener.onNewItem(link, item);
			}
			public synchronized boolean confirmInterrupt() {
				return listener.confirmInterrupt();
			}
			public synchronized void commitItems(INetworkLink link) {
				listener.commitItems(link);
			}
		};

		synchronized (myLinks) {
			for (INetworkLink link: myLinks) {
				//if (link.OnOption.getValue()) {
				// execute next code only if link is enabled
				//}
				final NetworkOperationData data = link.createOperationData(link, synchronizedListener);
				final ZLNetworkRequest request = link.simpleSearchRequest(pattern, data);
				if (request != null) {
					dataList.add(data);
					requestList.add(request);
				}
			}
		}

		while (requestList.size() != 0) {
			ZLNetworkManager.Instance().perform(requestList);

			requestList.clear();

			if (listener.confirmInterrupt()) {
				return;
			}
			for (NetworkOperationData data: dataList) {
				ZLNetworkRequest request = data.resume();
				if (request != null) {
					requestList.add(request);
				}
			}
		}
	}

	private ICustomNetworkLink.SaveLinkListener myChangesListener = new ICustomNetworkLink.SaveLinkListener() {
		public void onSaveLink(ICustomNetworkLink link) {
			NetworkDatabase.Instance().saveCustomLink(link);
		}
	};

	private <T extends INetworkLink> void addLinkInternal(ArrayList<T> list, T link, LinksComparator comparator) {
		synchronized (myLinks) {
			final int index = Collections.binarySearch(list, link, comparator);
			if (index >= 0) {
				throw new RuntimeException("Unable to add link with duplicated title to the library");
			}
			final int insertAt = -index - 1;
			list.add(insertAt, link);
		}
	}

	public void addCustomLink(ICustomNetworkLink link) {
		addLinkInternal(myCustomLinks, link, new LinksComparator());
		link.setSaveLinkListener(myChangesListener);
		link.saveLink();
	}

	public void removeCustomLink(ICustomNetworkLink link) {
		synchronized (myLinks) {
			final int index = Collections.binarySearch(myCustomLinks, link, new LinksComparator());
			if (index < 0) {
				return;
			}
			myCustomLinks.remove(index);
		}
		NetworkDatabase.Instance().deleteCustomLink(link);
		link.setSaveLinkListener(null);
	}

	public boolean hasCustomLinkTitle(String title, ICustomNetworkLink exeptFor) {
		synchronized (myLinks) {
			for (INetworkLink link: myLinks) {
				if (link != exeptFor && link.getTitle().equals(title)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean hasCustomLinkSite(String siteName, ICustomNetworkLink exeptFor) {
		synchronized (myLinks) {
			for (INetworkLink link: myLinks) {
				if (link != exeptFor && link.getSiteName().equals(siteName)) {
					return true;
				}
			}
		}
		return false;
	}
}
