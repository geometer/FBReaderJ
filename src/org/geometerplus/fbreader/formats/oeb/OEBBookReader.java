/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.oeb;

import java.util.*;
import java.io.IOException;

import org.geometerplus.zlibrary.core.constants.XMLNamespaces;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLFileImage;
import org.geometerplus.zlibrary.core.util.MimeType;
import org.geometerplus.zlibrary.core.xml.*;

import org.geometerplus.fbreader.bookmodel.*;
import org.geometerplus.fbreader.formats.xhtml.XHTMLReader;
import org.geometerplus.fbreader.formats.util.MiscUtil;

class Reference {
	public final String Title;
	public final String HRef;

	public Reference(String title, String href) {
		Title = title;
		HRef = href;
	}
}

class OEBBookReader extends ZLXMLReaderAdapter implements XMLNamespaces {
	private static final char[] Dots = new char[] {'.', '.', '.'};

	private final BookReader myModelReader;
	private final HashMap<String,String> myIdToHref = new HashMap<String,String>();
	private final ArrayList<String> myHtmlFileNames = new ArrayList<String>();
	private final ArrayList<Reference> myTourTOC = new ArrayList<Reference>();
	private final ArrayList<Reference> myGuideTOC = new ArrayList<Reference>();

	private String myOPFSchemePrefix;
	private String myFilePrefix;
	private String myNCXTOCFileName;
	private String myCoverFileName;

	OEBBookReader(BookModel model) {
		myModelReader = new BookReader(model);
		model.setLabelResolver(new BookModel.LabelResolver() {
			public List<String> getCandidates(String id) {
				final int index = id.indexOf("#");
				return index > 0
					? Collections.<String>singletonList(id.substring(0, index))
					: Collections.<String>emptyList();
			}
		});
	}

	private HashMap<String,String> myFileNumbers = new HashMap<String,String>();
	private HashMap<String,Integer> myTOCLabels = new HashMap<String,Integer>();

	void readBook(ZLFile file) throws BookReadingException {
		myFilePrefix = MiscUtil.htmlDirectoryPrefix(file);

		myIdToHref.clear();
		myHtmlFileNames.clear();
		myNCXTOCFileName = null;
		myTourTOC.clear();
		myGuideTOC.clear();
		myState = READ_NONE;

		try {
			read(file);
		} catch (IOException e) {
			throw new BookReadingException(e, file);
		}

		myModelReader.setMainTextModel();
		myModelReader.pushKind(FBTextKind.REGULAR);

		int count = 0;
		for (String name : myHtmlFileNames) {
			final ZLFile xhtmlFile = ZLFile.createFileByPath(myFilePrefix + name);
			if (xhtmlFile == null || !xhtmlFile.exists()) {
				continue;
			}
			if (count++ == 0 && xhtmlFile.getPath().equals(myCoverFileName)) {
				continue;
			}
			final XHTMLReader reader = new XHTMLReader(myModelReader, myFileNumbers);
			final String referenceName = reader.getFileAlias(MiscUtil.archiveEntryName(xhtmlFile.getPath()));

			myModelReader.addHyperlinkLabel(referenceName);
			myTOCLabels.put(referenceName, myModelReader.Model.BookTextModel.getParagraphsNumber());
			try {
				reader.readFile(xhtmlFile, referenceName + '#');
			} catch (IOException e) {
				throw new BookReadingException(e, xhtmlFile);
			}
			myModelReader.insertEndOfSectionParagraph();
		}

		generateTOC();
	}

	private BookModel.Label getTOCLabel(String id) {
		final int index = id.indexOf('#');
		final String path = (index >= 0) ? id.substring(0, index) : id;
		final String num = myFileNumbers.get(path);
		if (num == null) {
			return null;
		}
		if (index == -1) {
			final Integer para = myTOCLabels.get(num);
			if (para == null) {
				return null;
			}
			return new BookModel.Label(null, para);
		}
		return myModelReader.Model.getLabel(num + id.substring(index));
	}

	private boolean readNCX() throws BookReadingException {
		if (myNCXTOCFileName == null) {
			return false;
		}

		final ZLFile ncxFile = ZLFile.createFileByPath(myFilePrefix + myNCXTOCFileName);
		if (ncxFile == null || !ncxFile.exists()) {
			return false;
		}

		final NCXReader ncxReader = new NCXReader(myModelReader);
		ncxReader.readFile(ncxFile);
		final Map<Integer,NCXReader.NavPoint> navigationMap = ncxReader.navigationMap();
		if (navigationMap.isEmpty()) {
			return false;
		}

		int level = 0;
		for (NCXReader.NavPoint point : navigationMap.values()) {
			final BookModel.Label label = getTOCLabel(point.ContentHRef);
			int index = (label != null) ? label.ParagraphIndex : -1;
			while (level > point.Level) {
				myModelReader.endContentsParagraph();
				--level;
			}
			while (++level <= point.Level) {
				myModelReader.beginContentsParagraph(-2);
				myModelReader.addContentsData(Dots);
			}
			myModelReader.beginContentsParagraph(index);
			myModelReader.addContentsData(point.Text.toCharArray());
		}
		while (level > 0) {
			myModelReader.endContentsParagraph();
			--level;
		}

		return true;
	}

	private void generateTOC() throws BookReadingException {
		if (readNCX()) {
			return;
		}
		for (Reference ref : myTourTOC.isEmpty() ? myGuideTOC : myTourTOC) {
			final BookModel.Label label = getTOCLabel(ref.HRef);
			if (label != null) {
				final int index = label.ParagraphIndex;
				if (index != -1) {
					myModelReader.beginContentsParagraph(index);
					myModelReader.addContentsData(ref.Title.toCharArray());
					myModelReader.endContentsParagraph();
				}
			}
		}
	}

	private static final String MANIFEST = "manifest";
	private static final String SPINE = "spine";
	private static final String GUIDE = "guide";
	private static final String TOUR = "tour";
	private static final String SITE = "site";
	private static final String REFERENCE = "reference";
	private static final String ITEMREF = "itemref";
	private static final String ITEM = "item";

	private static final String COVER = "cover";
	private static final String COVER_IMAGE = "other.ms-coverimage-standard";

	private static final int READ_NONE = 0;
	private static final int READ_MANIFEST = 1;
	private static final int READ_SPINE = 2;
	private static final int READ_GUIDE = 3;
	private static final int READ_TOUR = 4;

	private int myState;

	@Override
	public boolean startElementHandler(String tag, ZLStringMap xmlattributes) {
		tag = tag.toLowerCase();
		if (myOPFSchemePrefix != null && tag.startsWith(myOPFSchemePrefix)) {
			tag = tag.substring(myOPFSchemePrefix.length());
		}
		tag = tag.intern();
		if (MANIFEST == tag) {
			myState = READ_MANIFEST;
		} else if (SPINE == tag) {
			myNCXTOCFileName = myIdToHref.get(xmlattributes.getValue("toc"));
			myState = READ_SPINE;
		} else if (GUIDE == tag) {
			myState = READ_GUIDE;
		} else if (TOUR == tag) {
			myState = READ_TOUR;
		} else if (myState == READ_MANIFEST && ITEM == tag) {
			final String id = xmlattributes.getValue("id");
			String href = xmlattributes.getValue("href");
			if ((id != null) && (href != null)) {
				href = MiscUtil.decodeHtmlReference(href);
				myIdToHref.put(id, href);
			}
		} else if (myState == READ_SPINE && ITEMREF == tag) {
			final String id = xmlattributes.getValue("idref");
			if (id != null) {
				final String fileName = myIdToHref.get(id);
				if (fileName != null) {
					myHtmlFileNames.add(fileName);
				}
			}
		} else if (myState == READ_GUIDE && REFERENCE == tag) {
			final String type = xmlattributes.getValue("type");
			final String title = xmlattributes.getValue("title");
			String href = xmlattributes.getValue("href");
			if (href != null) {
				href = MiscUtil.decodeHtmlReference(href);
				if (title != null) {
					myGuideTOC.add(new Reference(title, href));
				}
				if (COVER.equals(type)) {
					final ZLFile imageFile = ZLFile.createFileByPath(myFilePrefix + href);
					myCoverFileName = imageFile.getPath();
					final String imageName = imageFile.getLongName();
					final ZLFileImage image = XHTMLImageFinder.getCoverImage(imageFile);
					if (image != null) {
						myModelReader.setMainTextModel();
						myModelReader.addImageReference(imageName, (short)0, true);
						myModelReader.addImage(imageName, image);
						myModelReader.insertEndOfSectionParagraph();
					} else {
						myCoverFileName = null;
					}
				} else if (COVER_IMAGE.equals(type)) {
					final ZLFile imageFile = ZLFile.createFileByPath(myFilePrefix + href);
					myCoverFileName = imageFile.getPath();
					final String imageName = imageFile.getLongName();
					myModelReader.setMainTextModel();
					myModelReader.addImageReference(imageName, (short)0, true);
					myModelReader.addImage(imageName, new ZLFileImage(MimeType.IMAGE_AUTO, imageFile));
					myModelReader.insertEndOfSectionParagraph();
				}
			}
		} else if (myState == READ_TOUR && SITE == tag) {
			final String title = xmlattributes.getValue("title");
			String href = xmlattributes.getValue("href");
			if ((title != null) && (href != null)) {
				href = MiscUtil.decodeHtmlReference(href);
				myTourTOC.add(new Reference(title, href));
			}
		}
		return false;
	}

	@Override
	public boolean endElementHandler(String tag) {
		tag = tag.toLowerCase();
		if (myOPFSchemePrefix != null && tag.startsWith(myOPFSchemePrefix)) {
			tag = tag.substring(myOPFSchemePrefix.length());
		}
		tag = tag.intern();
		if (MANIFEST == tag || SPINE == tag || GUIDE == tag || TOUR == tag) {
			myState = READ_NONE;
		}
		return false;
	}

	@Override
	public boolean processNamespaces() {
		return true;
	}

	@Override
	public void namespaceMapChangedHandler(Map<String,String> namespaceMap) {
		myOPFSchemePrefix = null;
		for (Map.Entry<String,String> entry : namespaceMap.entrySet()) {
			if (OpenPackagingFormat.equals(entry.getValue())) {
				myOPFSchemePrefix = entry.getKey() + ":";
				break;
			}
		}
	}

	@Override
	public boolean dontCacheAttributeValues() {
		return true;
	}
}
