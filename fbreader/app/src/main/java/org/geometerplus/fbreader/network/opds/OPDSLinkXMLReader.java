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

package org.geometerplus.fbreader.network.opds;

import java.util.*;

import org.geometerplus.zlibrary.core.constants.XMLNamespaces;
import org.geometerplus.zlibrary.core.util.MimeType;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.atom.ATOMLink;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.authentication.litres.LitResAuthenticationManager;
import org.geometerplus.fbreader.network.rss.RSSNetworkLink;
import org.geometerplus.fbreader.network.urlInfo.*;

class OPDSLinkXMLReader extends OPDSXMLReader implements OPDSConstants {
	private static class FeedHandler extends AbstractOPDSFeedHandler {
		private final NetworkLibrary myLibrary;

		private final List<INetworkLink> myLinks = new LinkedList<INetworkLink>();

		private String myAuthenticationType;
		private final LinkedList<URLRewritingRule> myUrlRewritingRules = new LinkedList<URLRewritingRule>();
		private final HashMap<RelationAlias,String> myRelationAliases = new HashMap<RelationAlias,String>();
		private final LinkedHashMap<String,String> myExtraData = new LinkedHashMap<String,String>();

		FeedHandler(NetworkLibrary library) {
			myLibrary = library;
		}

		List<INetworkLink> links() {
			return myLinks;
		}

		void setAuthenticationType(String type) {
			myAuthenticationType = type;
		}

		void addUrlRewritingRule(URLRewritingRule rule) {
			myUrlRewritingRules.add(rule);
		}

		void addRelationAlias(RelationAlias alias, String relation) {
			myRelationAliases.put(alias, relation);
		}

		void putExtraData(String name, String value) {
			myExtraData.put(name, value);
		}

		void clear() {
			myAuthenticationType = null;
			myUrlRewritingRules.clear();
			myRelationAliases.clear();
			myExtraData.clear();
		}

		public boolean processFeedEntry(OPDSEntry entry) {
			final String id = entry.Id.Uri;
			if (id == null) {
				return false;
			}
			final CharSequence title = entry.Title;
			final CharSequence summary = entry.Content;
			final String language = entry.DCLanguage;

			final UrlInfoCollection<UrlInfoWithDate> infos =
				new UrlInfoCollection<UrlInfoWithDate>();
			for (ATOMLink link : entry.Links) {
				final String href = link.getHref();
				final MimeType mime = MimeType.get(link.getType());
				final String rel = link.getRel();
				if (rel == REL_IMAGE_THUMBNAIL || rel == REL_THUMBNAIL) {
					if (MimeType.IMAGE_PNG.equals(mime) || MimeType.IMAGE_JPEG.equals(mime)) {
						infos.addInfo(new UrlInfoWithDate(UrlInfo.Type.Thumbnail, href, mime));
					}
				} else if ((rel != null && rel.startsWith(REL_IMAGE_PREFIX)) || rel == REL_COVER) {
					if (MimeType.IMAGE_PNG.equals(mime) || MimeType.IMAGE_JPEG.equals(mime)) {
						infos.addInfo(new UrlInfoWithDate(UrlInfo.Type.Image, href, mime));
					}
				} else if (rel == null) {
					if (MimeType.APP_ATOM_XML.weakEquals(mime)
						|| MimeType.APP_RSS_XML.weakEquals(mime)) {
						infos.addInfo(new UrlInfoWithDate(UrlInfo.Type.Catalog, href, mime));
					}
				} else if (rel == "search") {
					if (MimeType.APP_ATOM_XML.weakEquals(mime)
						|| MimeType.TEXT_HTML.weakEquals(mime)) {
						final OpenSearchDescription descr = OpenSearchDescription.createDefault(href, mime);
						if (descr.isValid()) {
							// TODO: Why do we use '%s'? Use Description instead??? (this needs to rewrite SEARCH engine logic a little)
							infos.addInfo(new UrlInfoWithDate(UrlInfo.Type.Search, descr.makeQuery("%s"), mime));
						}
					}
				} else if (rel == "listbooks") {
					infos.addInfo(new UrlInfoWithDate(UrlInfo.Type.ListBooks, href, mime));
				} else if (rel == REL_LINK_SIGN_IN) {
					infos.addInfo(new UrlInfoWithDate(UrlInfo.Type.SignIn, href, mime));
				} else if (rel == REL_LINK_SIGN_OUT) {
					infos.addInfo(new UrlInfoWithDate(UrlInfo.Type.SignOut, href, mime));
				} else if (rel == REL_LINK_SIGN_UP) {
					infos.addInfo(new UrlInfoWithDate(UrlInfo.Type.SignUp, href, mime));
				} else if (rel == REL_LINK_TOPUP) {
					infos.addInfo(new UrlInfoWithDate(UrlInfo.Type.TopUp, href, mime));
				} else if (rel == REL_LINK_RECOVER_PASSWORD) {
					infos.addInfo(new UrlInfoWithDate(UrlInfo.Type.RecoverPassword, href, mime));
				}
			}

			if (title != null && infos.getInfo(UrlInfo.Type.Catalog) != null) {
				final INetworkLink l = link(id, title, summary, language, infos);
				if (l != null) {
					myLinks.add(l);
				}
			}
			return false;
		}

		private INetworkLink link(
			String id,
			CharSequence title,
			CharSequence summary,
			String language,
			UrlInfoCollection<UrlInfoWithDate> infos
		) {
			final String titleString = title.toString();
			final String summaryString = summary != null ? summary.toString() : null;

			final UrlInfo catalogInfo = infos.getInfo(UrlInfo.Type.Catalog);

			if (MimeType.APP_ATOM_XML.weakEquals(catalogInfo.Mime)) {
				final OPDSNetworkLink opdsLink = new OPDSPredefinedNetworkLink(
					myLibrary,
					OPDSNetworkLink.INVALID_ID,
					id,
					titleString,
					summaryString,
					language,
					infos
				);

				opdsLink.setRelationAliases(myRelationAliases);
				opdsLink.setUrlRewritingRules(myUrlRewritingRules);
				opdsLink.setExtraData(myExtraData);

				if (myAuthenticationType == "litres") {
					opdsLink.setAuthenticationManager(
						NetworkAuthenticationManager.createManager(
							myLibrary, opdsLink, LitResAuthenticationManager.class
						)
					);
				}
				return opdsLink;
			} else if (MimeType.APP_RSS_XML.weakEquals(catalogInfo.Mime)) {
				return new RSSNetworkLink(
					OPDSNetworkLink.INVALID_ID,
					id,
					titleString,
					summaryString,
					language,
					infos
				);
			} else {
				return null;
			}
		}

		public boolean processFeedMetadata(OPDSFeedMetadata feed, boolean beforeEntries) {
			return false;
		}

		public void processFeedStart() {
		}

		public void processFeedEnd() {
		}
	}

	public OPDSLinkXMLReader(NetworkLibrary library) {
		super(library, new FeedHandler(library), false);
	}

	public List<INetworkLink> links() {
		return getFeedHandler().links();
	}

	private FeedHandler getFeedHandler() {
		return (FeedHandler)getATOMFeedHandler();
	}

	private static final String FBREADER_ADVANCED_SEARCH = "advancedSearch";
	private static final String FBREADER_AUTHENTICATION = "authentication";
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
