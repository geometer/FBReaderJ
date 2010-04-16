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

import org.geometerplus.zlibrary.core.xml.*;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.authentication.litres.LitResAuthenticationManager;


public class OPDSLinkReader extends ZLXMLReaderAdapter {

	private String mySiteName;
	private String myTitle;
	private String mySummary;
	private String myIcon;

	private final HashMap<String, String> myLinks = new HashMap<String, String>();

	private HashMap<RelationAlias, String> myRelationAliases = new HashMap<RelationAlias, String>();

	private String mySearchType;
	private final HashMap<String, String> mySearchFields = new HashMap<String, String>();

	private final HashMap<String, Integer> myUrlConditions = new HashMap<String, Integer>();
	private String myAuthenticationType;
	private final LinkedList<URLRewritingRule> myUrlRewritingRules = new LinkedList<URLRewritingRule>();

	private String mySSLCertificate;

	private NetworkLink link() {
		if (mySiteName == null || myTitle == null || myLinks.get(NetworkLink.URL_MAIN) == null) {
			return null;
		}

		OPDSLink opdsLink = new OPDSLink(
			mySiteName,
			myTitle,
			mySummary,
			myIcon,
			myLinks
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
		opdsLink.setUrlConditions(myUrlConditions);
		opdsLink.setUrlRewritingRules(myUrlRewritingRules);

		NetworkAuthenticationManager authManager = null;
		if (myAuthenticationType == "basic") {
			//authManager = new BasicAuthenticationManager(opdsLink, mySSLCertificate);
		} else if (myAuthenticationType == "litres") {
			authManager = new LitResAuthenticationManager(opdsLink, mySSLCertificate);
		}
		opdsLink.setAuthenticationManager(authManager);

		return opdsLink;
	}

	public NetworkLink readDocument(ZLFile file) {
		mySiteName = myTitle = mySummary = myIcon = mySearchType = myAuthenticationType = mySSLCertificate = null;
		myLinks.clear();
		mySearchFields.clear();
		myUrlConditions.clear();
		myUrlRewritingRules.clear();
		myRelationAliases.clear();

		String path = file.getPath();
		if (path.endsWith(".xml")) {
			path = path.substring(0, path.length() - 4) + ".crt";
			if (ZLResourceFile.createResourceFile(path).exists()) {
				mySSLCertificate = path;
			}
		}

		myState = READ_NOTHING;

		if (!ZLXMLProcessor.read(this, file)) {
			return null;
		}

		return link();
	}


	private static final String TAG_SITE = "site";
	private static final String TAG_LINK = "link";
	private static final String TAG_TITLE = "title";
	private static final String TAG_SUMMARY = "summary";
	private static final String TAG_ICON = "icon";
	private static final String TAG_RELATION_ALIASES = "relationAliases";
	private static final String TAG_ALIAS = "alias";
	private static final String TAG_SEARCH_DESCRIPTION = "advancedSearch";
	private static final String TAG_FEEDS = "feeds";
	private static final String TAG_AUTHENTICATION = "authentication";
	private static final String TAG_URL_REWRITING_RULES = "urlRewritingRules";
	private static final String TAG_FIELD = "field";
	private static final String TAG_CONDITION = "condition";
	private static final String TAG_RULE = "rule";

	private static final int READ_NOTHING = 0;
	private static final int READ_SITENAME = 1;
	private static final int READ_TITLE = 2;
	private static final int READ_SUMMARY = 3;
	private static final int READ_ICON_NAME = 4;
	private static final int READ_LINK = 5;
	private static final int READ_SEARCH_DESCRIPTION = 6;
	private static final int READ_SEARCH_FIELD = 7;
	private static final int READ_FEEDS = 8;
	private static final int READ_FEEDS_CONDITION = 9;
	private static final int READ_URL_REWRITING_RULES = 10;
	private static final int READ_RELATION_ALIASES = 11;

	private int myState;

	private String myAttrBuffer;
	private final StringBuffer myBuffer = new StringBuffer();


	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		tag = tag.intern();
		if (TAG_SITE == tag) {
			myState = READ_SITENAME;
		} else if (TAG_TITLE == tag) {
			myState = READ_TITLE;
		} else if (TAG_SUMMARY == tag) {
			myState = READ_SUMMARY;
		} else if (TAG_ICON == tag) {
			myState = READ_ICON_NAME;
		} else if (TAG_LINK == tag) {
			String linkType = attributes.getValue("rel");
			if (linkType != null) {
				myAttrBuffer = linkType;
				myLinks.remove(myAttrBuffer);
				myState = READ_LINK;
			}
		} else if (TAG_SEARCH_DESCRIPTION == tag) {
			String searchType = attributes.getValue("style");
			if (searchType != null) {
				mySearchType = searchType;
				myState = READ_SEARCH_DESCRIPTION;
			}
		} else if (myState == READ_SEARCH_DESCRIPTION && TAG_FIELD == tag) {
			String name = attributes.getValue("name");
			if (name != null) {
				myAttrBuffer = name;
				mySearchFields.remove(myAttrBuffer);
				myState = READ_SEARCH_FIELD;
			}
		} else if (TAG_FEEDS == tag) {
			myState = READ_FEEDS;
		} else if (myState == READ_FEEDS && TAG_CONDITION == tag) {
			String show = attributes.getValue("show");
			if (show != null) {
				myAttrBuffer = show;
				myState = READ_FEEDS_CONDITION;
			}
		} else if (TAG_AUTHENTICATION == tag) {
			String authenticationType = attributes.getValue("type");
			if (authenticationType != null) {
				myAuthenticationType = authenticationType;
			}
		} else if (TAG_URL_REWRITING_RULES == tag) {
			myState = READ_URL_REWRITING_RULES;
		} else if (myState == READ_URL_REWRITING_RULES && TAG_RULE == tag) {
			String type  = attributes.getValue("type");
			String apply = attributes.getValue("apply");
			String name  = attributes.getValue("name");
			String value = attributes.getValue("value");

			int ruleApply = URLRewritingRule.APPLY_ALWAYS;
			if (apply != null) {
				apply = apply.intern();
				if (apply == "external") {
					ruleApply = URLRewritingRule.APPLY_EXTERNAL;
				} else if (apply == "internal") {
					ruleApply = URLRewritingRule.APPLY_INTERNAL;
				} else if (apply != "always") {
					type = null;
				}
			}

			if (type != null && name != null && value != null) {
				if (type == "addUrlParameter") {
					myUrlRewritingRules.add(new URLRewritingRule(URLRewritingRule.ADD_URL_PARAMETER, ruleApply, name, value));
				}
			}
		} else if (TAG_RELATION_ALIASES == tag) {
			myState = READ_RELATION_ALIASES;
		} else if (myState == READ_RELATION_ALIASES && TAG_ALIAS == tag) {
			String alias = attributes.getValue("alias");
			String name  = attributes.getValue("name");
			String type  = attributes.getValue("type");
			if (alias != null && name != null) {
				if (alias.length() == 0) {
					alias = null;
				}
				myRelationAliases.put(new RelationAlias(alias, type), name);
			}
		}
		return false;
	}

	@Override
	public boolean endElementHandler(String tag) {
		tag = tag.intern();

		String bufferContent = myBuffer.toString().trim();
		myBuffer.delete(0, myBuffer.length());

		if (bufferContent.length() != 0) {
			switch (myState) {
				case READ_NOTHING:
				case READ_SEARCH_DESCRIPTION:
				case READ_FEEDS:
				case READ_URL_REWRITING_RULES:
				case READ_RELATION_ALIASES:
					break;
				case READ_SITENAME:
					mySiteName = bufferContent;
					break;
				case READ_TITLE:
					myTitle = bufferContent;
					break;
				case READ_SUMMARY:
					mySummary = bufferContent;
					break;
				case READ_ICON_NAME:
					myIcon = bufferContent;
					break;
				case READ_LINK:
					myLinks.put(myAttrBuffer, bufferContent);
					break;
				case READ_SEARCH_FIELD:
					mySearchFields.put(myAttrBuffer, bufferContent);
					break;
				case READ_FEEDS_CONDITION:
					myUrlConditions.put(
						bufferContent,
						myAttrBuffer.equals("signedIn") ? 
							OPDSLink.FeedCondition.SIGNED_IN : 
							OPDSLink.FeedCondition.NEVER
					);
					break;
			}
		}

		if (myState == READ_SEARCH_FIELD) {
			myState = READ_SEARCH_DESCRIPTION;
		} else if (myState == READ_FEEDS_CONDITION) {
			myState = READ_FEEDS;
		} else if (myState == READ_URL_REWRITING_RULES && TAG_RULE == tag) {
			//myState = myState;
		} else if (myState == READ_RELATION_ALIASES && TAG_ALIAS == tag) {
			//myState = myState;
		} else {
			myState = READ_NOTHING;
		}
		return false;
	}

	@Override
	public void characterDataHandler(char[] data, int start, int length) {
		switch (myState) {
			case READ_NOTHING:
			case READ_SEARCH_DESCRIPTION:
			case READ_FEEDS:
			case READ_URL_REWRITING_RULES:
			case READ_RELATION_ALIASES:
				break;
			case READ_SITENAME:
			case READ_TITLE:
			case READ_SUMMARY:
			case READ_ICON_NAME:
			case READ_LINK:
			case READ_SEARCH_FIELD:
			case READ_FEEDS_CONDITION:
				myBuffer.append(data, start, length);
				break;
		}
	}

}
