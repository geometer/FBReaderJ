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

package org.geometerplus.fbreader.network.opds;

import java.util.*;

import org.geometerplus.zlibrary.core.constants.XMLNamespaces;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.util.MimeType;
import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.atom.*;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.authentication.litres.LitResAuthenticationManager;
import org.geometerplus.fbreader.network.urlInfo.*;

class OPDSLinkXMLReader extends OPDSXMLReader implements OPDSConstants {
	private static class FeedHandler implements ATOMFeedHandler<OPDSFeedMetadata,OPDSEntry> {
		private NetworkLibrary.OnNewLinkListener myListener;

		private String myAuthenticationType;
		private boolean myHasStableIdentifiers;
		private final LinkedList<URLRewritingRule> myUrlRewritingRules = new LinkedList<URLRewritingRule>();
		private final HashMap<RelationAlias, String> myRelationAliases = new HashMap<RelationAlias, String>();
		private final LinkedHashMap<String,String> myExtraData = new LinkedHashMap<String,String>(); 

		private ATOMUpdated myUpdatedTime;
		private ATOMUpdated myReadAfterTime;

		public FeedHandler(NetworkLibrary.OnNewLinkListener listener, ATOMUpdated readAfter) {
			myListener = listener;
			myReadAfterTime = readAfter;
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

		public void putExtraData(String name, String value) {
			myExtraData.put(name, value);
		}

		public void clear() {
			myAuthenticationType = null;
			myHasStableIdentifiers = false;
			myUrlRewritingRules.clear();
			myRelationAliases.clear();
			myExtraData.clear();
		}

		public ATOMUpdated getUpdatedTime() {
			return myUpdatedTime;
		}

		private static final String ENTRY_ID_PREFIX = "urn:fbreader-org-catalog:";

		public boolean processFeedEntry(OPDSEntry entry) {
			final String id = entry.Id.Uri;
			if (id == null || id.length() <= ENTRY_ID_PREFIX.length()
					|| !id.startsWith(ENTRY_ID_PREFIX)) {
				return false;
			}
			final String siteName = id.substring(ENTRY_ID_PREFIX.length());
			final CharSequence title = entry.Title;
			final CharSequence summary = entry.Content;
			final String language = entry.DCLanguage;

			final UrlInfoCollection<UrlInfoWithDate> infos =
				new UrlInfoCollection<UrlInfoWithDate>();
			final HashMap<String,NetworkCatalogItem.Accessibility> urlConditions =
				new HashMap<String,NetworkCatalogItem.Accessibility>();
			for (ATOMLink link: entry.Links) {
				final String href = link.getHref();
				final MimeType type = MimeType.get(link.getType());
				final String rel = link.getRel();
				if (rel == REL_IMAGE_THUMBNAIL || rel == REL_THUMBNAIL) {
					if (MimeType.IMAGE_PNG.equals(type) || MimeType.IMAGE_JPEG.equals(type)) {
						infos.addInfo(new UrlInfoWithDate(UrlInfo.Type.Thumbnail, href));
					}
				} else if ((rel != null && rel.startsWith(REL_IMAGE_PREFIX)) || rel == REL_COVER) {
					if (MimeType.IMAGE_PNG.equals(type) || MimeType.IMAGE_JPEG.equals(type)) {
						infos.addInfo(new UrlInfoWithDate(UrlInfo.Type.Image, href));
					}
				} else if (rel == null) {
					if (MimeType.APP_ATOM.equals(type)) {
						infos.addInfo(new UrlInfoWithDate(UrlInfo.Type.Catalog, href));
					}
				} else if (rel == "search") {
					if (MimeType.APP_ATOM.equals(type)) {
						final OpenSearchDescription descr = OpenSearchDescription.createDefault(href);
						if (descr.isValid()) {
							// TODO: May be do not use '%s'??? Use Description instead??? (this needs to rewrite SEARCH engine logic a little)
							infos.addInfo(new UrlInfoWithDate(UrlInfo.Type.Search, descr.makeQuery("%s")));
						}
					}
				} else if (rel == REL_LINK_SIGN_IN) {
					infos.addInfo(new UrlInfoWithDate(UrlInfo.Type.SignIn, href));
				} else if (rel == REL_LINK_SIGN_OUT) {
					infos.addInfo(new UrlInfoWithDate(UrlInfo.Type.SignOut, href));
				} else if (rel == REL_LINK_SIGN_UP) {
					infos.addInfo(new UrlInfoWithDate(UrlInfo.Type.SignUp, href));
				} else if (rel == REL_LINK_TOPUP) {
					infos.addInfo(new UrlInfoWithDate(UrlInfo.Type.TopUp, href));
				} else if (rel == REL_LINK_RECOVER_PASSWORD) {
					infos.addInfo(new UrlInfoWithDate(UrlInfo.Type.RecoverPassword, href));
				} else if (rel == REL_CONDITION_NEVER) {
					urlConditions.put(href, NetworkCatalogItem.Accessibility.NEVER);
				} else if (rel == REL_CONDITION_SIGNED_IN) {
					urlConditions.put(href, NetworkCatalogItem.Accessibility.SIGNED_IN);
				} else if (rel == REL_CONDITION_HAS_BOOKS) {
					urlConditions.put(href, NetworkCatalogItem.Accessibility.HAS_BOOKS);
				}
			}

			final String sslCertificate;
			final String path = "network/" + siteName + ".crt";
			if (ZLResourceFile.createResourceFile(path).exists()) {
				sslCertificate = path;
			} else {
				sslCertificate = null;
			}

			INetworkLink result = link(siteName, title, summary, language, infos, urlConditions, sslCertificate);
			if (result != null) {
				myListener.onNewLink(result);
			}
			return false; 
		}

		private INetworkLink link(
			String siteName,
			CharSequence title,
			CharSequence summary,
			String language,
			UrlInfoCollection<UrlInfoWithDate> infos,
			HashMap<String,NetworkCatalogItem.Accessibility> urlConditions,
			String sslCertificate
		) {
			if (siteName == null || title == null || infos.getInfo(UrlInfo.Type.Catalog) == null) {
				return null;
			}

			final String titleString = title.toString();
			final String summaryString = summary != null ? summary.toString() : null;

			OPDSNetworkLink opdsLink = new OPDSNetworkLink(
				siteName,
				titleString,
				summaryString,
				language,
				infos,
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
			opdsLink.setExtraData(myExtraData);

			NetworkAuthenticationManager authManager = null;
			if (myAuthenticationType == "basic") {
				//authManager = NetworkAuthenticationManager.createManager(opdsLink, sslCertificate, BasicAuthenticationManager.class);
			} else if (myAuthenticationType == "litres") {
				authManager = NetworkAuthenticationManager.createManager(opdsLink, sslCertificate, LitResAuthenticationManager.class);
			}
			opdsLink.setAuthenticationManager(authManager);

			return opdsLink;
		}

		public boolean processFeedMetadata(OPDSFeedMetadata feed, boolean beforeEntries) {
			myUpdatedTime = feed.Updated;
			if (myUpdatedTime != null && myReadAfterTime != null
					&& myUpdatedTime.compareTo(myReadAfterTime) <= 0) {
				return true;
			}
			return myListener == null; // no listener -- no need to proceed
		}

		public void processFeedStart() {
			myUpdatedTime = null;
		}

		public void processFeedEnd() {
		}
	}

	public OPDSLinkXMLReader() {
		super(new FeedHandler(null, null), false);
	}

	public OPDSLinkXMLReader(NetworkLibrary.OnNewLinkListener listener, ATOMUpdated readAfter) {
		super(new FeedHandler(listener, readAfter), false);
	}

	private FeedHandler getFeedHandler() {
		return (FeedHandler)getATOMFeedHandler();
	}

	public ATOMUpdated getUpdatedTime() {
		return getFeedHandler().getUpdatedTime();
	}

	private static final String FBREADER_ADVANCED_SEARCH = "advancedSearch";
	private static final String FBREADER_AUTHENTICATION = "authentication";
	private static final String FBREADER_STABLE_IDENTIFIERS = "hasStableIdentifiers";
	private static final String FBREADER_REWRITING_RULE = "urlRewritingRule";
	private static final String FBREADER_RELATION_ALIAS = "relationAlias";
	private static final String FBREADER_EXTRA = "extra";

	@Override
	public boolean startElementHandler(final String ns, final String tag,
			final ZLStringMap attributes, final String bufferContent) {
		switch (myState) {
			case FEED:
				if (ns == XMLNamespaces.Atom && tag == TAG_ENTRY) {
					getFeedHandler().clear();
				}
				break;
			case F_ENTRY:
				if (ns == XMLNamespaces.FBReaderCatalogMetadata) {
					if (tag == FBREADER_ADVANCED_SEARCH) {
						return false;
					} else if (tag == FBREADER_AUTHENTICATION) {
						final String type = attributes.getValue("type");
						getFeedHandler().setAuthenticationType(type);
						return false;
					} else if (tag == FBREADER_RELATION_ALIAS) {
						final String name = attributes.getValue("name");
						final String type = attributes.getValue("type");
						String alias = attributes.getValue("alias");
						if (alias != null && name != null) {
							if (alias.length() == 0) {
								alias = null;
							}
							getFeedHandler().addRelationAlias(new RelationAlias(alias, type), name);
						}
						return false;
					} else if (tag == FBREADER_REWRITING_RULE) {
						getFeedHandler().addUrlRewritingRule(new URLRewritingRule(attributes));
						return false;
					} else if (tag == FBREADER_STABLE_IDENTIFIERS) {
						getFeedHandler().setHasStableIdentifiers(true);
						return false;
					} else if (tag == FBREADER_EXTRA) {
						final String name = attributes.getValue("name");
						final String value = attributes.getValue("value");
						if (name != null && value != null) {
							getFeedHandler().putExtraData(name, value);
						}
					}
				}
				break;
		}
		return super.startElementHandler(ns, tag, attributes, bufferContent);
	}
}
