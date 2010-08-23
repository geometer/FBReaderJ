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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

import org.geometerplus.fbreader.constants.XMLNamespace;
import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkImage;
import org.geometerplus.fbreader.network.atom.ATOMLink;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.authentication.litres.LitResAuthenticationManager;

public class OPDSLinkXMLReader extends OPDSXMLReader {

	private static class LinkReader implements OPDSFeedReader {

		private List<INetworkLink> myLinksBuffer;

		private String myAuthenticationType;
		private boolean myHasStableIdentifiers;
		private final LinkedList<URLRewritingRule> myUrlRewritingRules = new LinkedList<URLRewritingRule>();
		private HashMap<RelationAlias, String> myRelationAliases = new HashMap<RelationAlias, String>();

		public LinkReader(List<INetworkLink> links) {
			myLinksBuffer = links;
		}

		public void setAuthenticationType(String type) {
			myAuthenticationType = type;
		}

		public void setHasStableIdentifiers(boolean value) {
			myHasStableIdentifiers = value;
		}

		public void addUrlRewritingRule(URLRewritingRule rule) {
			myUrlRewritingRules.add(rule);
		}

		public void addRelationAlias(RelationAlias alias, String relation) {
			myRelationAliases.put(alias, relation);
		}

		public void clear() {
			myAuthenticationType = null;
			myHasStableIdentifiers = false;
			myUrlRewritingRules.clear();
			myRelationAliases.clear();
		}

		private static final String ENTRY_ID_PREFIX = "urn:fbreader-org-catalog:";

		public boolean processFeedEntry(OPDSEntry entry) {
			final String id = entry.Id.Uri;
			if (id == null || id.length() <= ENTRY_ID_PREFIX.length()
					|| !id.startsWith(ENTRY_ID_PREFIX)) {
				return false;
			}
			final String siteName = id.substring(ENTRY_ID_PREFIX.length());
			final String title = entry.Title;
			final String summary = entry.Content;

			String icon = null; 
			final HashMap<String, String> links = new HashMap<String, String>();
			final HashMap<String, Integer> urlConditions = new HashMap<String, Integer>();
			for (ATOMLink link: entry.Links) {
				final String href = link.getHref();
				final String type = link.getType();
				final String rel = link.getRel();
				if (rel == OPDSConstants.REL_COVER) {
					if (icon == null &&
							(type == NetworkImage.MIME_PNG ||
							 type == NetworkImage.MIME_JPEG)) {
						icon = href;
					}
				} else if (rel == OPDSConstants.REL_THUMBNAIL) {
					if (type == NetworkImage.MIME_PNG ||
							type == NetworkImage.MIME_JPEG) {
						icon = href;
					}
				} else if (rel == null) {
					if (type == OPDSConstants.MIME_APP_ATOM) {
						links.put(INetworkLink.URL_MAIN, href);
					}
				} else if (rel == "search") {
					if (type == OPDSConstants.MIME_APP_ATOM) {
						final OpenSearchDescription descr = OpenSearchDescription.createDefault(href);
						if (descr.isValid()) {
							// TODO: May be do not use '%s'??? Use Description instead??? (this needs to rewrite SEARCH engine logic a little)
							links.put(INetworkLink.URL_SEARCH, descr.makeQuery("%s"));
						}
					}
				} else if (rel == OPDSConstants.REL_LINK_SIGN_IN) {
					links.put(INetworkLink.URL_SIGN_IN, href);
				} else if (rel == OPDSConstants.REL_LINK_SIGN_OUT) {
					links.put(INetworkLink.URL_SIGN_OUT, href);
				} else if (rel == OPDSConstants.REL_LINK_SIGN_UP) {
					links.put(INetworkLink.URL_SIGN_UP, href);
				} else if (rel == OPDSConstants.REL_LINK_REFILL_ACCOUNT) {
					links.put(INetworkLink.URL_REFILL_ACCOUNT, href);
				} else if (rel == OPDSConstants.REL_LINK_RECOVER_PASSWORD) {
					links.put(INetworkLink.URL_RECOVER_PASSWORD, href);
				} else if (rel == OPDSConstants.REL_CONDITION_NEVER) {
					urlConditions.put(href, OPDSLink.FeedCondition.NEVER);
				} else if (rel == OPDSConstants.REL_CONDITION_SIGNED_IN) {
					urlConditions.put(href, OPDSLink.FeedCondition.SIGNED_IN);
				}
			}

			final String sslCertificate;
			final String path = "data/network/" + siteName + ".crt";
			if (ZLResourceFile.createResourceFile(path).exists()) {
				sslCertificate = path;
			} else {
				sslCertificate = null;
			}

			INetworkLink result = link(siteName, title, summary, icon, links, urlConditions, sslCertificate);
			if (result != null) {
				myLinksBuffer.add(result);
			}
			return false; 
		}

		private INetworkLink link(String siteName, String title, String summary, String icon,
				Map<String, String> links, HashMap<String, Integer> urlConditions, String sslCertificate) {
			if (siteName == null || title == null || links.get(INetworkLink.URL_MAIN) == null) {
				return null;
			}

			OPDSLink opdsLink = new OPDSLink(
				siteName,
				title,
				summary,
				icon,
				links,
				myHasStableIdentifiers
			);

			/*if (!mySearchType.empty()) {
				opdsLink.setupAdvancedSearch(
					mySearchType,
					mySearchFields["titleOrSeries"],
					mySearchFields["author"],
					mySearchFields["tag"],
					mySearchFields["annotation"]
				);
			}*/
			opdsLink.setRelationAliases(myRelationAliases);
			opdsLink.setUrlConditions(urlConditions);
			opdsLink.setUrlRewritingRules(myUrlRewritingRules);

			NetworkAuthenticationManager authManager = null;
			if (myAuthenticationType == "basic") {
				//authManager = new BasicAuthenticationManager(opdsLink, sslCertificate);
			} else if (myAuthenticationType == "litres") {
				authManager = new LitResAuthenticationManager(opdsLink, sslCertificate);
			}
			opdsLink.setAuthenticationManager(authManager);

			return opdsLink;
		}

		public boolean processFeedMetadata(OPDSFeedMetadata feed, boolean beforeEntries) { return false; }
		public void processFeedStart() {}
		public void processFeedEnd() {}
	}

	public OPDSLinkXMLReader(List<INetworkLink> links) {
		super(new LinkReader(links));
	}

	private String myFBReaderNamespaceId;

	@Override
	public void namespaceMapChangedHandler(HashMap<String, String> namespaceMap) {
		super.namespaceMapChangedHandler(namespaceMap);

		myFBReaderNamespaceId = null;

		for (Map.Entry<String,String> entry : namespaceMap.entrySet()) {
			final String value = entry.getValue();
			if (value == XMLNamespace.FBReaderCatalogMetadata) {
				myFBReaderNamespaceId = intern(entry.getKey());
			}
		}
	}


	private static final String FBREADER_ADVANCED_SEARCH = "advancedSearch";
	private static final String FBREADER_AUTHENTICATION = "authentication";
	private static final String FBREADER_STABLE_IDENTIFIERS = "hasStableIdentifiers";
	private static final String FBREADER_REWRITING_RULE = "urlRewritingRule";
	private static final String FBREADER_RELATION_ALIAS = "relationAlias";

	@Override
	public boolean startElementHandler(final String tagPrefix, final String tag,
			final ZLStringMap attributes, final String bufferContent) {
		switch (getState()) {
		case FEED:
			if (tagPrefix == myAtomNamespaceId && tag == TAG_ENTRY) {
				((LinkReader) myFeedReader).clear();
			}
			break;
		case F_ENTRY:
			if (tagPrefix == myFBReaderNamespaceId) {
				if (tag == FBREADER_ADVANCED_SEARCH) {
					return false;
				} else if (tag == FBREADER_AUTHENTICATION) {
					final String type = attributes.getValue("type");
					((LinkReader) myFeedReader).setAuthenticationType(type);
					return false;
				} else if (tag == FBREADER_RELATION_ALIAS) {
					final String name = attributes.getValue("name");
					final String type = attributes.getValue("type");
					String alias = attributes.getValue("alias");
					if (alias != null && name != null) {
						if (alias.length() == 0) {
							alias = null;
						}
						((LinkReader) myFeedReader).addRelationAlias(new RelationAlias(alias, type), name);
					}
					return false;
				} else if (tag == FBREADER_REWRITING_RULE) {
					final String type = attributes.getValue("type");
					final String apply = attributes.getValue("apply");
					final String name = attributes.getValue("name");
					final String value = attributes.getValue("value");
					final int typeValue;
					if (type == "addUrlParameter") {
						typeValue = URLRewritingRule.ADD_URL_PARAMETER;
					} else {
						return false;
					}
					final int applyValue;
					if (apply == "external") {
						applyValue = URLRewritingRule.APPLY_EXTERNAL;
					} else if (apply == "internal") {
						applyValue = URLRewritingRule.APPLY_INTERNAL;
					} else {
						applyValue = URLRewritingRule.APPLY_ALWAYS;
					}
					((LinkReader) myFeedReader).addUrlRewritingRule(new URLRewritingRule(typeValue, applyValue, name, value));
					return false;
				} else if (tag == FBREADER_STABLE_IDENTIFIERS) {
					((LinkReader) myFeedReader).setHasStableIdentifiers(true);
					return false;
				}
			}
			break;
		}
		return super.startElementHandler(tagPrefix, tag, attributes, bufferContent);
	}

	@Override
	public boolean endElementHandler(final String tagPrefix, final String tag,
			final String bufferContent) {
		return super.endElementHandler(tagPrefix, tag, bufferContent);
	}
}
