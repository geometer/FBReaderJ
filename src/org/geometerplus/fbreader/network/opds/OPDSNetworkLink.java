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

package org.geometerplus.fbreader.network.opds;

import java.util.*;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.geometerplus.zlibrary.core.util.ZLMiscUtil;
import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;


class OPDSNetworkLink extends AbstractNetworkLink {

	public interface FeedCondition {
		int REGULAR = 0;
		int NEVER = 1;
		int SIGNED_IN = 2;
	}

	private TreeMap<RelationAlias, String> myRelationAliases;

	private TreeMap<String, Integer> myUrlConditions;
	private final LinkedList<URLRewritingRule> myUrlRewritingRules = new LinkedList<URLRewritingRule>();
	private final Map<String,String> myExtraData = new HashMap<String,String>();
	private NetworkAuthenticationManager myAuthenticationManager;

	private final boolean myHasStableIdentifiers;

	OPDSNetworkLink(String siteName, String title, String summary, String icon, String language,
			Map<String, String> links, boolean hasStableIdentifiers) {
		super(siteName, title, summary, icon, language, links);
		myHasStableIdentifiers = hasStableIdentifiers;
	}

	final void setRelationAliases(Map<RelationAlias, String> relationAliases) {
		if (relationAliases != null && relationAliases.size() > 0) {
			myRelationAliases = new TreeMap<RelationAlias, String>(relationAliases);
		} else {
			myRelationAliases = null;
		}
	}

	final void setUrlConditions(Map<String, Integer> conditions) {
		if (conditions != null && conditions.size() > 0) {
			myUrlConditions = new TreeMap<String, Integer>(conditions);
		} else {
			myUrlConditions = null;
		}
	}

	final void setUrlRewritingRules(List<URLRewritingRule> rules) {
		myUrlRewritingRules.clear();
		myUrlRewritingRules.addAll(rules);
	}

	final void setExtraData(Map<String,String> extraData) {
		myExtraData.clear();
		myExtraData.putAll(extraData);
	}

	final void setAuthenticationManager(NetworkAuthenticationManager mgr) {
		myAuthenticationManager = mgr;
	}

	ZLNetworkRequest createNetworkData(String url, final OPDSCatalogItem.State result) {
		if (url == null) {
			return null;
		}
		url = rewriteUrl(url, false);
		return new ZLNetworkRequest(url) {
			@Override
			public void handleStream(URLConnection connection, InputStream inputStream) throws IOException, ZLNetworkException {
				if (result.Listener.confirmInterrupt()) {
					return;
				}

				new OPDSXMLReader(
					new NetworkOPDSFeedReader(URL, result)
				).read(inputStream);

				if (result.Listener.confirmInterrupt()) {
					if (!myHasStableIdentifiers && result.LastLoadedId != null) {
						// If current catalog doesn't have stable identifiers
						// and catalog wasn't completely loaded (i.e. LastLoadedIdentifier is not null)
						// then reset state to load current page from the beginning 
						result.LastLoadedId = null;
					} else {
						result.Listener.commitItems(OPDSNetworkLink.this);
					}
				} else {
					result.Listener.commitItems(OPDSNetworkLink.this);
				}
			}
		};
	}

	@Override
	public OPDSCatalogItem.State createOperationData(INetworkLink link,
			NetworkOperationData.OnNewItemListener listener) {
		return new OPDSCatalogItem.State(link, listener);
	}

	public ZLNetworkRequest simpleSearchRequest(String pattern, NetworkOperationData data) {
		final String url = getLink(URL_SEARCH);
		if (url == null) {
			return null;
		}
		try {
			pattern = URLEncoder.encode(pattern, "utf-8");
		} catch (UnsupportedEncodingException e) {
		}
		return createNetworkData(url.replace("%s", pattern), (OPDSCatalogItem.State)data);
	}

	public ZLNetworkRequest resume(NetworkOperationData data) {
		return createNetworkData(data.ResumeURI, (OPDSCatalogItem.State) data);
	}

	public NetworkCatalogItem libraryItem() {
		TreeMap<Integer, String> urlMap = new TreeMap<Integer, String>();
		urlMap.put(NetworkCatalogItem.URL_CATALOG, getLink(URL_MAIN));
		return new OPDSCatalogItem(this, getTitle(), getSummary(), getIcon(), urlMap, myExtraData);
	}

	public NetworkAuthenticationManager authenticationManager() {
		return myAuthenticationManager;
	}

	public String rewriteUrl(String url, boolean isUrlExternal) {
		for (URLRewritingRule rule: myUrlRewritingRules) {
			if (rule.Apply != URLRewritingRule.APPLY_ALWAYS) {
				if ((rule.Apply == URLRewritingRule.APPLY_EXTERNAL && !isUrlExternal)
					|| (rule.Apply == URLRewritingRule.APPLY_INTERNAL && isUrlExternal)) {
					continue;
				}
			}
			switch (rule.Type) {
			case URLRewritingRule.ADD_URL_PARAMETER:
				url = ZLNetworkUtil.appendParameter(url, rule.Name, rule.Value);
				break;
			}
		}
		return url;
	}

	int getCondition(String url) {
		if (myUrlConditions == null) {
			return FeedCondition.REGULAR;
		}
		Integer cond = myUrlConditions.get(url);
		if (cond == null) {
			return FeedCondition.REGULAR;
		}
		return cond.intValue();
	}

	// rel and type must be either null or interned String objects.
	String relation(String rel, String type) {
		if (myRelationAliases == null) {
			return rel;
		}
		RelationAlias alias = new RelationAlias(rel, type);
		String mapped = myRelationAliases.get(alias);
		if (mapped != null) {
			return mapped;
		}
		if (type != null) {
			alias = new RelationAlias(rel, null);
			mapped = myRelationAliases.get(alias);
			if (mapped != null) {
				return mapped;
			}
		}
		return rel;
	}

	@Override
	public String toString() {
		return "OPDSNetworkLink: {super=" + super.toString()
			+ "; stableIds=" + myHasStableIdentifiers
			+ "; authManager=" + (myAuthenticationManager != null ? myAuthenticationManager.getClass().getName() : null)
			+ "; relationAliases=" + myRelationAliases
			+ "; urlConditions=" + myUrlConditions
			+ "; rewritingRules=" + myUrlRewritingRules
			+ "}";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof OPDSNetworkLink)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		final OPDSNetworkLink lnk = (OPDSNetworkLink) o;
		if (myHasStableIdentifiers != lnk.myHasStableIdentifiers
				|| !ZLMiscUtil.mapsEquals(myRelationAliases, lnk.myRelationAliases)
				|| !ZLMiscUtil.mapsEquals(myUrlConditions, lnk.myUrlConditions)
				|| !ZLMiscUtil.listsEquals(myUrlRewritingRules, lnk.myUrlRewritingRules)
				|| myAuthenticationManager != lnk.myAuthenticationManager) {
			return false;
		}
		return true;
	}
}
