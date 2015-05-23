/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.io.InputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.image.ZLImage;

import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.fbreader.formats.PluginCollection;

public abstract class BookUtil {
	private static final WeakReference<ZLImage> NULL_IMAGE = new WeakReference<ZLImage>(null);
	private static final WeakHashMap<ZLFile,WeakReference<ZLImage>> ourCovers =
		new WeakHashMap<ZLFile,WeakReference<ZLImage>>();

	public static ZLImage getCover(AbstractBook book) {
		if (book == null) {
			return null;
		}
		synchronized (book) {
			return getCover(fileByBook(book));
		}
	}

	public static ZLImage getCover(ZLFile file) {
		WeakReference<ZLImage> cover = ourCovers.get(file);
		if (cover == NULL_IMAGE) {
			return null;
		} else if (cover != null) {
			final ZLImage image = cover.get();
			if (image != null) {
				return image;
			}
		}
		ZLImage image = null;
		try {
			image = PluginCollection.Instance().getPlugin(file).readCover(file);
		} catch (Exception e) {
			// ignore
		}
		ourCovers.put(file, image != null ? new WeakReference<ZLImage>(image) : NULL_IMAGE);
		return image;
	}

	public static String getAnnotation(AbstractBook book) {
		try {
			return getPlugin(book).readAnnotation(fileByBook(book));
		} catch (BookReadingException e) {
			return null;
		}
	}

	public static ZLResourceFile getHelpFile() {
		final Locale locale = Locale.getDefault();

		ZLResourceFile file = ZLResourceFile.createResourceFile(
			"data/intro/intro-" + locale.getLanguage() + "_" + locale.getCountry() + ".epub"
		);
		if (file.exists()) {
			return file;
		}

		file = ZLResourceFile.createResourceFile(
			"data/intro/intro-" + locale.getLanguage() + ".epub"
		);
		if (file.exists()) {
			return file;
		}

		return ZLResourceFile.createResourceFile("data/intro/intro-en.epub");
	}

	public static UID createUid(AbstractBook book, String algorithm) {
		return createUid(fileByBook(book), algorithm);
	}

	public static UID createUid(ZLFile file, String algorithm) {
		InputStream stream = null;

		try {
			final MessageDigest hash = MessageDigest.getInstance(algorithm);
			stream = file.getInputStream();

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
			return new UID(algorithm, f.toString());
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

	public static FormatPlugin getPlugin(AbstractBook book) throws BookReadingException {
		final ZLFile file = fileByBook(book);
		final FormatPlugin plugin = PluginCollection.Instance().getPlugin(file);
		if (plugin == null) {
			throw new BookReadingException("pluginNotFound", file);
		}
		return plugin;
	}

	public static String getEncoding(AbstractBook book) {
		if (book.getEncodingNoDetection() == null) {
			try {
				BookUtil.getPlugin(book).detectLanguageAndEncoding(book);
			} catch (BookReadingException e) {
			}
			if (book.getEncodingNoDetection() == null) {
				book.setEncoding("utf-8");
			}
		}
		return book.getEncodingNoDetection();
	}

	public static void reloadInfoFromFile(AbstractBook book) {
		try {
			readMetainfo(book);
		} catch (BookReadingException e) {
			// ignore
		}
	}

	static void readMetainfo(AbstractBook book) throws BookReadingException {
		readMetainfo(book, getPlugin(book));
	}

	static void readMetainfo(AbstractBook book, FormatPlugin plugin) throws BookReadingException {
		book.myEncoding = null;
		book.myLanguage = null;
		book.setTitle(null);
		book.myAuthors = null;
		book.myTags = null;
		book.mySeriesInfo = null;
		book.myUids = null;

		book.myIsSaved = false;

		plugin.readMetainfo(book);
		if (book.myUids == null || book.myUids.isEmpty()) {
			plugin.readUids(book);
		}

		if (book.isTitleEmpty()) {
			final String fileName = fileByBook(book).getShortName();
			final int index = fileName.lastIndexOf('.');
			book.setTitle(index > 0 ? fileName.substring(0, index) : fileName);
		}
	}

	public static ZLFile fileByBook(AbstractBook book) {
		if (book instanceof DbBook) {
			return ((DbBook)book).File;
		} else {
			return ZLFile.createFileByPath(book.getPath());
		}
	}
}
