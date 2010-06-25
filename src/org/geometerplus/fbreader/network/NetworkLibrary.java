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

import java.io.*;
import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;
import org.geometerplus.zlibrary.core.network.ZLNetworkManager;

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
		private final int mySize;
		private Comparator<INetworkLink> myComparator;

		public CompositeList(ArrayList<ArrayList<? extends INetworkLink>> lists,
				Comparator<INetworkLink> comparator) {
			int size = 0;
			for (ArrayList<? extends INetworkLink> list: lists) {
				size += list.size();
			}
			myLists = lists;
			mySize = size;
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
				return myIndex < mySize;
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
			if (location < 0 || location > mySize) {
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
			return mySize;
		}
	}

	private static class LinksComparator implements Comparator<INetworkLink> {
		public int compare(INetworkLink link1, INetworkLink link2) {
			String title1 = link1.getTitle();
			for (int index = 0; index < title1.length(); ++index) {
				final char ch = title1.charAt(index);
				if (ch < 128 && Character.isLetter(ch)) {
					title1 = title1.substring(index);
					break;
				}
			}
			String title2 = link2.getTitle();
			for (int index = 0; index < title2.length(); ++index) {
				final char ch = title2.charAt(index);
				if (ch < 128 && Character.isLetter(ch)) {
					title2 = title2.substring(index);
					break;
				}
			}
			return title1.compareTo(title2);
		}
	}


	public final ZLStringOption NetworkSearchPatternOption = new ZLStringOption("NetworkSearch", "Pattern", "");

	private final ArrayList<INetworkLink> myLoadedLinks = new ArrayList<INetworkLink>();
	private final ArrayList<ICustomNetworkLink> myCustomLinks = new ArrayList<ICustomNetworkLink>();

	private final CompositeList myLinks;

	private final RootTree myRootTree = new RootTree();

	private boolean myUpdateChildren = true;
	private boolean myUpdateVisibility;

	private NetworkLibrary() {
		LinkedList<String> catalogs = readCatalogFileNames();
		OPDSLinkReader reader = new OPDSLinkReader();
		for (String fileName: catalogs) {
			INetworkLink link = reader.readDocument(ZLResourceFile.createResourceFile("data/network/" + fileName));
			if (link != null) {
				myLoadedLinks.add(link);
			}
		}

		Map<String, String> links;

		links = new TreeMap<String, String>();
		links.put(INetworkLink.URL_MAIN, "http://bookserver.archive.org/catalog/");
		myCustomLinks.add( reader.createCustomLink("archive.org",
				"Internet Archive Catalog", null, null, links));

		links = new TreeMap<String, String>();
		links.put(INetworkLink.URL_MAIN, "http://pragprog.com/magazines.opds");
		myCustomLinks.add( reader.createCustomLink("pragprog.com",
				"PragPub Magazine", "The Pragmatic Bookshelf", null, links));

		LinksComparator comparator = new LinksComparator(); 
		Collections.sort(myLoadedLinks, comparator);
		Collections.sort(myCustomLinks, comparator);

		ArrayList<ArrayList<? extends INetworkLink>> linksList = new ArrayList<ArrayList<? extends INetworkLink>>();
		linksList.add(myLoadedLinks);
		linksList.add(myCustomLinks);
		myLinks = new CompositeList(linksList, comparator); 
	}

	private final LinkedList<String> readCatalogFileNames() {
		final LinkedList<String> catalogs = new LinkedList<String>();
		final ZLResourceFile catalogsFile = ZLResourceFile.createResourceFile("data/network/catalogs.txt");
		try {
			InputStream stream = catalogsFile.getInputStream();
			if (stream != null) {
				Scanner scanner = new Scanner(stream);
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine().trim();
					if (line.length() > 0) {
						catalogs.add(line);
					}
				}
				scanner.close();
			}
		} catch (IOException ex) {
		}
		return catalogs;
	}

	public String rewriteUrl(String url, boolean externalUrl) {
		final String host = ZLNetworkUtil.hostFromUrl(url).toLowerCase();
		for (INetworkLink link: myLinks) {
			if (host.contains(link.getSiteName())) {
				url = link.rewriteUrl(url, externalUrl);
			}
		}
		return url;
	}

	public void invalidate() {
		myUpdateChildren = true;
	}

	public void invalidateVisibility() {
		myUpdateVisibility = true;
	}

	private void makeUpToDate() {
		final LinkedList<FBTree> toRemove = new LinkedList<FBTree>();

		ListIterator<FBTree> nodeIterator = myRootTree.subTrees().listIterator();
		FBTree currentNode = null;
		int nodeCount = 0;

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
				final INetworkLink nodeLink = ((NetworkCatalogTree)currentNode).Item.Link;
				if (nodeLink == link) {
					currentNode = null;
					++nodeCount;
					processed = true;
					break;
				} else {
					boolean found = false;
					ListIterator<INetworkLink> jt = myLinks.listIterator(it);
					while (jt.hasNext()) {
						if (nodeLink == jt.next()) {
							found = true;
							break;
						}
					}
					if (!found) {
						toRemove.add(currentNode);
						currentNode = null;
						++nodeCount;
					} else {
						break;
					}
				}
			}
			final int nextIndex = nodeIterator.nextIndex();
			if (!processed) {
				new NetworkCatalogRootTree(myRootTree, link, nodeCount++).Item.onDisplayItem();
				nodeIterator = myRootTree.subTrees().listIterator(nextIndex + 1);
			}
		}

		while (currentNode != null || nodeIterator.hasNext()) {
			if (currentNode == null) {
				currentNode = nodeIterator.next();
			}
			toRemove.add(currentNode);
			currentNode = null;
			//++nodeCount; // TODO: where to increment???
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
		if (myUpdateChildren) {
			myUpdateChildren = false;
			makeUpToDate();
		}
		if (myUpdateVisibility) {
			myUpdateVisibility = false;
			updateVisibility();
		}
	}

	public NetworkTree getTree() {
		synchronize();
		return myRootTree;
	}


	// returns Error Message
	public String simpleSearch(String pattern, final NetworkOperationData.OnNewItemListener listener) {
		LinkedList<ZLNetworkRequest> requestList = new LinkedList<ZLNetworkRequest>();
		LinkedList<NetworkOperationData> dataList = new LinkedList<NetworkOperationData>();

		NetworkOperationData.OnNewItemListener synchronizedListener = new NetworkOperationData.OnNewItemListener() {
			public synchronized boolean onNewItem(NetworkLibraryItem item) {
				return listener.onNewItem(item);
			}
		};

		for (INetworkLink link: myLoadedLinks) {
			//if (link.OnOption.getValue()) {
			// execute next code only if link is enabled
			//}
			NetworkOperationData data = new NetworkOperationData(link, synchronizedListener);
			ZLNetworkRequest request = link.simpleSearchRequest(pattern, data);
			if (request != null) {
				dataList.add(data);
				requestList.add(request);
			}
		}

		while (requestList.size() != 0) {
			final String errorMessage = ZLNetworkManager.Instance().perform(requestList);
			if (errorMessage != null) {
				return errorMessage;
			}

			requestList.clear();

			for (NetworkOperationData data: dataList) {
				ZLNetworkRequest request = data.resume();
				if (request != null) {
					requestList.add(request);
				}
			}
		}

		return null;
	}
}
