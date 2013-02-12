/*
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.book;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.filetypes.FileType;
import org.geometerplus.zlibrary.core.filetypes.FileTypeCollection;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.MiscUtil;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;

import org.geometerplus.zlibrary.text.view.ZLTextPosition;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.formats.*;

public class Book {
	static Book getByFile(ZLFile bookFile) {
		System.err.println("getByFile");
		if (bookFile == null) {
			System.err.println("bookFile == null");
			return null;
		}

		final ZLPhysicalFile physicalFile = bookFile.getPhysicalFile();
		if (physicalFile != null && !physicalFile.exists()) {
			System.err.println("!physicalFile.exists()");
			return null;
		}

		final BooksDatabase database = BooksDatabase.Instance();
		final FileInfoSet fileInfos = new FileInfoSet(database, bookFile);

		Book book = database.loadBookByFile(fileInfos.getId(bookFile), bookFile);
		if (book != null) {
			book.loadLists(database);
		}

		if (book != null && fileInfos.check(physicalFile, physicalFile != bookFile)) {
			System.err.println("return book (1)");
			return book;
		}
		fileInfos.save();

		try {
			if (book == null) {
				book = new Book(bookFile);
			} else {
				book.readMetaInfo();
			}
		} catch (BookReadingException e) {
			e.printStackTrace();
			return null;
		}

		book.save(database, false);
		System.err.println("return book (2)");
		return book;
	}

	public final ZLFile File;

	private volatile long myId;

	private volatile String myEncoding;
	private volatile String myLanguage;
	private volatile String myTitle;
	private volatile List<Author> myAuthors;
	private volatile List<Tag> myTags;
	private volatile SeriesInfo mySeriesInfo;

	private volatile boolean myIsSaved;

	private static final WeakReference<ZLImage> NULL_IMAGE = new WeakReference<ZLImage>(null);
	private WeakReference<ZLImage> myCover;

	Book(long id, ZLFile file, String title, String encoding, String language) {
		myId = id;
		File = file;
		myTitle = title;
		myEncoding = encoding;
		myLanguage = language;
		myIsSaved = true;
	}

	Book(ZLFile file) throws BookReadingException {
		myId = -1;
		final FormatPlugin plugin = getPlugin(file);
		File = plugin.realBookFile(file);
		readMetaInfo(plugin);
		myIsSaved = false;
	}

	public void updateFrom(Book book) {
		if (myId != book.myId) {
			return;
		}
		myTitle = book.myTitle;
		myEncoding = book.myEncoding;
		myLanguage = book.myLanguage;
		myAuthors = book.myAuthors != null ? new ArrayList<Author>(book.myAuthors) : null;
		myTags = book.myTags != null ? new ArrayList<Tag>(book.myTags) : null;
		mySeriesInfo = book.mySeriesInfo;
	}

	public void reloadInfoFromFile() {
		try {
			readMetaInfo();
		} catch (BookReadingException e) {
			// ignore
		}
	}

	private static FormatPlugin getPlugin(ZLFile file) throws BookReadingException {
		final FormatPlugin plugin = PluginCollection.Instance().getPlugin(file);
		if (plugin == null) {
			throw new BookReadingException("pluginNotFound", file);
		}
		return plugin;
	}

	public FormatPlugin getPlugin() throws BookReadingException {
		return getPlugin(File);
	}

	void readMetaInfo() throws BookReadingException {
		readMetaInfo(getPlugin());
	}

	private static class Reader extends ZLXMLReaderAdapter {
		public Book book = null;
		@Override
		public boolean startElementHandler(String tag, ZLStringMap attributes) {
			try {
				if ("MetaInfo".equals(tag)) {
					book.setTitle(attributes.getValue("title"));
					book.addAuthor(attributes.getValue("author"));
					book.addTag(attributes.getValue("subject"));
				}
			} catch (Throwable e) {
			}
			return false;
		}
	}
	
	private void readMetaInfo(FormatPlugin plugin) throws BookReadingException {
		myEncoding = null;
		myLanguage = null;
		myTitle = null;
		myAuthors = null;
		myTags = null;
		mySeriesInfo = null;

		myIsSaved = false;


		final FileType fileType = FileTypeCollection.Instance.typeForFile(File);
		final FormatPlugin fplugin = PluginCollection.Instance().getPlugin(fileType, FormatPlugin.Type.PLUGIN);
		if (fplugin != null) {
			try {
				String meta = MetaInfoUtil.PMIReader.readMetaInfo(File, ((PluginFormatPlugin)fplugin).getPackage());
				Reader r = new Reader();
				r.book = this;
				try {
					r.read(new ByteArrayInputStream(meta.getBytes("UTF-8")));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		} else {
			plugin.readMetaInfo(this);
		}


		if (myTitle == null || myTitle.length() == 0) {
			final String fileName = File.getShortName();
			final int index = (plugin.type() == FormatPlugin.Type.EXTERNAL ? -1 : fileName.lastIndexOf('.'));
			setTitle(index > 0 ? fileName.substring(0, index) : fileName);
		}
		final String demoPathPrefix = Paths.mainBookDirectory() + "/Demos/";
		if (File.getPath().startsWith(demoPathPrefix)) {
			final String demoTag = ZLResource.resource("library").getResource("demo").getValue();
			setTitle(getTitle() + " (" + demoTag + ")");
			addTag(demoTag);
		}
	}

	void loadLists(BooksDatabase database) {
		myAuthors = database.listAuthors(myId);
		myTags = database.listTags(myId);
		mySeriesInfo = database.getSeriesInfo(myId);
		myIsSaved = true;
	}

	public List<Author> authors() {
		return (myAuthors != null) ? Collections.unmodifiableList(myAuthors) : Collections.<Author>emptyList();
	}

	public List<String> getAuthors() {
		if (myAuthors == null) {
			return Collections.<String>emptyList();
		}
		List<String> result = new ArrayList<String>();
		for (Author a : myAuthors) {
			result.add(a.DisplayName);
		}
		return result;
	}

	public void setAuthors(List<String> list) {
		myAuthors = null;
		for (String s : list) {
			addAuthor(s);
		}
	}

	void addAuthorWithNoCheck(Author author) {
		if (myAuthors == null) {
			myAuthors = new ArrayList<Author>();
		}
		myAuthors.add(author);
	}

	public void removeAllAuthors() {
		if (myAuthors != null) {
			myAuthors = null;
			myIsSaved = false;
		}
	}

	public void addAuthor(Author author) {
		if (author == null) {
			return;
		}
		if (myAuthors == null) {
			myAuthors = new ArrayList<Author>();
			myAuthors.add(author);
			myIsSaved = false;
		} else if (!myAuthors.contains(author)) {
			myAuthors.add(author);
			myIsSaved = false;
		}
	}

	public void addAuthor(String name) {
		addAuthor(name, "");
	}

	public void addAuthor(String name, String sortKey) {
		String strippedName = name;
		strippedName.trim();
		if (strippedName.length() == 0) {
			return;
		}

		String strippedKey = sortKey;
		strippedKey.trim();
		if (strippedKey.length() == 0) {
			int index = strippedName.lastIndexOf(' ');
			if (index == -1) {
				strippedKey = strippedName;
			} else {
				strippedKey = strippedName.substring(index + 1);
				while ((index >= 0) && (strippedName.charAt(index) == ' ')) {
					--index;
				}
				strippedName = strippedName.substring(0, index + 1) + ' ' + strippedKey;
			}
		}

		addAuthor(new Author(strippedName, strippedKey));
	}

	public long getId() {
		return myId;
	}

	public String getTitle() {
		return myTitle;
	}

	public void setTitle(String title) {
		if (!MiscUtil.equals(myTitle, title)) {
			myTitle = title;
			myIsSaved = false;
		}
	}

	public SeriesInfo getSeriesInfo() {
		return mySeriesInfo;
	}

	void setSeriesInfoWithNoCheck(String name, String index) {
		mySeriesInfo = SeriesInfo.createSeriesInfo(name, index);
	}

	public void setSeriesInfo(String name, String index) {
		setSeriesInfo(name, SeriesInfo.createIndex(index));
	}

	public void setSeriesInfo(String name, BigDecimal index) {
		if (mySeriesInfo == null) {
			if (name != null) {
				mySeriesInfo = new SeriesInfo(name, index);
				myIsSaved = false;
			}
		} else if (name == null) {
			mySeriesInfo = null;
			myIsSaved = false;
		} else if (!name.equals(mySeriesInfo.Title) || mySeriesInfo.Index != index) {
			mySeriesInfo = new SeriesInfo(name, index);
			myIsSaved = false;
		}
	}

	public String getLanguage() {
		return myLanguage;
	}

	public void setLanguage(String language) {
		if (!MiscUtil.equals(myLanguage, language)) {
			myLanguage = language;
			myIsSaved = false;
		}
	}

	public String getEncoding() {
		if (myEncoding == null) {
			try {
				getPlugin().detectLanguageAndEncoding(this);
			} catch (BookReadingException e) {
			}
			if (myEncoding == null) {
				setEncoding("utf-8");
			}
		}
		return myEncoding;
	}

	public String getEncodingNoDetection() {
		return myEncoding;
	}

	public void setEncoding(String encoding) {
		if (!MiscUtil.equals(myEncoding, encoding)) {
			myEncoding = encoding;
			myIsSaved = false;
		}
	}

	public List<Tag> tags() {
		return myTags != null ? Collections.unmodifiableList(myTags) : Collections.<Tag>emptyList();
	}

	void addTagWithNoCheck(Tag tag) {
		if (myTags == null) {
			myTags = new ArrayList<Tag>();
		}
		myTags.add(tag);
	}

	public void removeAllTags() {
		if (myTags != null) {
			myTags = null;
			myIsSaved = false;
		}
	}

	public void addTag(Tag tag) {
		if (tag != null) {
			if (myTags == null) {
				myTags = new ArrayList<Tag>();
			}
			if (!myTags.contains(tag)) {
				myTags.add(tag);
				myIsSaved = false;
			}
		}
	}

	public void addTag(String tagName) {
		addTag(Tag.getTag(null, tagName));
	}

	public boolean matches(String pattern) {
		if (myTitle != null && MiscUtil.matchesIgnoreCase(myTitle, pattern)) {
			return true;
		}
		if (mySeriesInfo != null && MiscUtil.matchesIgnoreCase(mySeriesInfo.Title, pattern)) {
			return true;
		}
		if (myAuthors != null) {
			for (Author author : myAuthors) {
				if (MiscUtil.matchesIgnoreCase(author.DisplayName, pattern)) {
					return true;
				}
			}
		}
		if (myTags != null) {
			for (Tag tag : myTags) {
				if (MiscUtil.matchesIgnoreCase(tag.Name, pattern)) {
					return true;
				}
			}
		}
		if (MiscUtil.matchesIgnoreCase(File.getLongName(), pattern)) {
			return true;
		}
		return false;
	}

	boolean save(final BooksDatabase database, boolean force) {
		if (!force && myId != -1 && myIsSaved) {
			return false;
		}

		database.executeAsTransaction(new Runnable() {
			public void run() {
				if (myId >= 0) {
					final FileInfoSet fileInfos = new FileInfoSet(database, File);
					database.updateBookInfo(myId, fileInfos.getId(File), myEncoding, myLanguage, myTitle);
				} else {
					myId = database.insertBookInfo(File, myEncoding, myLanguage, myTitle);
					if (myId != -1 && myVisitedHyperlinks != null) {
						for (String linkId : myVisitedHyperlinks) {
							database.addVisitedHyperlink(myId, linkId);
						}
					}
				}

				long index = 0;
				database.deleteAllBookAuthors(myId);
				for (Author author : authors()) {
					database.saveBookAuthorInfo(myId, index++, author);
				}
				database.deleteAllBookTags(myId);
				for (Tag tag : tags()) {
					database.saveBookTagInfo(myId, tag);
				}
				database.saveBookSeriesInfo(myId, mySeriesInfo);
			}
		});

		myIsSaved = true;
		return true;
	}

//	public ZLTextPosition getStoredPosition() {
//		return BooksDatabase.Instance().getStoredPosition(myId);
//	}

//	public void storePosition(ZLTextPosition position) {
//		if (myId != -1) {
//			BooksDatabase.Instance().storePosition(myId, position);
//		}
//	}

	private Set<String> myVisitedHyperlinks;
	private void initHyperlinkSet(BooksDatabase database) {
		if (myVisitedHyperlinks == null) {
			myVisitedHyperlinks = new TreeSet<String>();
			if (myId != -1) {
				myVisitedHyperlinks.addAll(database.loadVisitedHyperlinks(myId));
			}
		}
	}

	boolean isHyperlinkVisited(BooksDatabase database, String linkId) {
		initHyperlinkSet(database);
		return myVisitedHyperlinks.contains(linkId);
	}

	void markHyperlinkAsVisited(BooksDatabase database, String linkId) {
		initHyperlinkSet(database);
		if (!myVisitedHyperlinks.contains(linkId)) {
			myVisitedHyperlinks.add(linkId);
			if (myId != -1) {
				database.addVisitedHyperlink(myId, linkId);
			}
		}
	}

	public String getContentHashCode() {
		InputStream stream = null;

		try {
			final MessageDigest hash = MessageDigest.getInstance("SHA-256");
			stream = File.getInputStream();

			final byte[] buffer = new byte[2048];
			while (true) {
				final int nread = stream.read(buffer);
				if (nread == -1) {
					break;
				}
				hash.update(buffer, 0, nread);
			}

			final Formatter f = new Formatter();
			for (byte b : hash.digest()) {
				f.format("%02X", b & 0xFF);
			}
			return f.toString();
		} catch (IOException e) {
			return null;
		} catch (NoSuchAlgorithmException e) {
			return null;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	synchronized ZLImage getCover() {
		if (myCover == NULL_IMAGE) {
			return null;
		} else if (myCover != null) {
			final ZLImage image = myCover.get();
			if (image != null) {
				return image;
			}
		}
		ZLImage image = null;
		
		try {
			final FileType fileType = FileTypeCollection.Instance.typeForFile(File);
			final FormatPlugin plugin = PluginCollection.Instance().getPlugin(fileType, FormatPlugin.Type.PLUGIN);
			if (plugin != null) {
				try {
					image = MetaInfoUtil.PMIReader.readImage(File, ((PluginFormatPlugin)plugin).getPackage());
					if (image != null) {
						myCover = new WeakReference<ZLImage>(image);
					}
					return image;
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			} else {
				image = getPlugin().readCover(File);
			}
		} catch (BookReadingException e) {
			// ignore
		}
		myCover = image != null ? new WeakReference<ZLImage>(image) : NULL_IMAGE;
		return image;
	}

	@Override
	public int hashCode() {
		return (int)myId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Book)) {
			return false;
		}
		return File.equals(((Book)o).File);
	}
}
