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

	public final ZLStringOption NetworkSearchPatternOption = new ZLStringOption("NetworkSearch", "Pattern", "");


	private final ArrayList<NetworkLink> myLinks = new ArrayList<NetworkLink>();
	private final RootTree myRootTree = new RootTree();

	private boolean myUpdateChildren = true;
	private boolean myUpdateVisibility;

	private static class LinksComparator implements Comparator<NetworkLink> {
		public int compare(NetworkLink link1, NetworkLink link2) {
			String title1 = link1.Title;
			for (int index = 0; index < title1.length(); ++index) {
				final char ch = title1.charAt(index);
				if (ch < 128 && Character.isLetter(ch)) {
					title1 = title1.substring(index);
					break;
				}
			}
			String title2 = link2.Title;
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

	private NetworkLibrary() {
		LinkedList<String> catalogs = readCatalogFileNames();
		OPDSLinkReader reader = new OPDSLinkReader();
		for (String fileName: catalogs) {
			NetworkLink link = reader.readDocument(ZLResourceFile.createResourceFile("data/network/" + fileName));
			if (link != null) {
				myLinks.add(link);
			}
		}
		Collections.sort(myLinks, new LinksComparator());
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

	public List<NetworkLink> links() {
		return Collections.unmodifiableList(myLinks);
	}

	public String rewriteUrl(String url, boolean externalUrl) {
		final String host = ZLNetworkUtil.hostFromUrl(url).toLowerCase();
		for (NetworkLink link: myLinks) {
			if (host.contains(link.SiteName)) {
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

		Iterator<FBTree> nodeIterator = myRootTree.subTrees().iterator();
		FBTree currentNode = null;
		int nodeCount = 0;

		for (int i = 0; i < myLinks.size(); ++i) {
			NetworkLink link = myLinks.get(i);
			if (!link.OnOption.getValue()) {
				continue;
			}
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
				final NetworkLink nodeLink = ((NetworkCatalogTree)currentNode).Item.Link;
				if (nodeLink == link) {
					currentNode = null;
					++nodeCount;
					processed = true;
					break;
				} else {
					boolean found = false;
					for (int j = i + 1; j < myLinks.size(); ++j) {
						if (nodeLink == myLinks.get(j)) {
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
			if (!processed) {
				NetworkCatalogRootTree ptr = new NetworkCatalogRootTree(myRootTree, link, nodeCount++);
				ptr.Item.onDisplayItem();
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

		for (NetworkLink link: myLinks) {
			if (link.OnOption.getValue()) {
				NetworkOperationData data = new NetworkOperationData(link, synchronizedListener);
				ZLNetworkRequest request = link.simpleSearchRequest(pattern, data);
				if (request != null) {
					dataList.add(data);
					requestList.add(request);
				}
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
