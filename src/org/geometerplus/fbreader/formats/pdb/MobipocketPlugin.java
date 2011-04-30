/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.pdb;

import java.io.*;

import org.geometerplus.zlibrary.core.encoding.ZLEncodingCollection;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLFileImage;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.language.ZLLanguageUtil;
import org.geometerplus.zlibrary.core.util.MimeType;

import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.bookmodel.BookModel;

public class MobipocketPlugin extends PdbPlugin {
	@Override
	public boolean acceptsFile(ZLFile file) {
		return super.acceptsFile(file) && (fileType(file) == "BOOKMOBI");
	}

	@Override
	public boolean readMetaInfo(Book book) {
		InputStream stream = null;
		try {
			stream = book.File.getInputStream();
			final PdbHeader header = new PdbHeader(stream);
			PdbUtil.skip(stream, header.Offsets[0] + 16 - header.length());
			if (PdbUtil.readInt(stream) != 0x4D4F4249) /* "MOBI" */ {
				return false;
			}
			final int length = (int)PdbUtil.readInt(stream);
			PdbUtil.skip(stream, 4);
			final int encodingCode = (int)PdbUtil.readInt(stream);
			String encodingName = ZLEncodingCollection.Instance().getEncodingName(encodingCode);
			if (encodingName == null) {
				encodingName = "utf-8";
			}
			book.setEncoding(encodingName);
			PdbUtil.skip(stream, 52);
			final int fullNameOffset = (int)PdbUtil.readInt(stream);
			final int fullNameLength = (int)PdbUtil.readInt(stream);
			final int languageCode = (int)PdbUtil.readInt(stream);
			book.setLanguage(ZLLanguageUtil.languageByIntCode(languageCode & 0xFF, (languageCode >> 8) & 0xFF));
			PdbUtil.skip(stream, 32);
			int offset = 132;
			if ((PdbUtil.readInt(stream) & 0x40) != 0) {
				PdbUtil.skip(stream, length - 116);
				offset = length + 20;
				if (PdbUtil.readInt(stream) == 0x45585448) /* "EXTH" */ {
					PdbUtil.skip(stream, 4);
					final int recordsNumber = (int)PdbUtil.readInt(stream);
					offset += 8;
					for (int i = 0; i < recordsNumber; ++i) {
						final int type = (int)PdbUtil.readInt(stream);
						final int size = (int)PdbUtil.readInt(stream);
						offset += size;
						if (size <= 8) {
							continue;
						}
						switch (type) {
							default:
								PdbUtil.skip(stream, size - 8);
								break;
							case 100:
							{
								final byte[] buffer = new byte[size - 8];
								stream.read(buffer);
								String author = new String(buffer, encodingName);
								final int index = author.indexOf(',');
								if (index != -1) {
									author = author.substring(index + 1).trim() +
											 ' ' +
											 author.substring(0, index).trim(); 
								} else {
									author = author.trim();
								}
								book.addAuthor(author);
								break;
							}
							case 105:
							{
								final byte[] buffer = new byte[size - 8];
								stream.read(buffer);
								book.addTag(new String(buffer, encodingName));
								break;
							}
						}
					}
				}
			}
			PdbUtil.skip(stream, fullNameOffset - offset);
			final byte[] titleBuffer = new byte[fullNameLength];
			stream.read(titleBuffer);
			book.setTitle(new String(titleBuffer, encodingName));
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	@Override
	public boolean readModel(BookModel model) {
		try {
			return new MobipocketHtmlBookReader(model).readBook();
		} catch (IOException e) {
			//e.printStackTrace();
			return false;
		}
	}

	@Override
	public ZLImage readCover(ZLFile file) {
		InputStream stream = null;
		try {
			stream = file.getInputStream();
			final PdbHeader header = new PdbHeader(stream);
			PdbUtil.skip(stream, header.Offsets[0] + 16 - header.length());
			if (PdbUtil.readInt(stream) != 0x4D4F4249) /* "MOBI" */ {
				return null;
			}
			final int length = (int)PdbUtil.readInt(stream);
			PdbUtil.skip(stream, 104);

			final int exthFlags = (int)PdbUtil.readInt(stream);
			int coverIndex = -1;
			int thumbIndex = -1;

			int offset = 132;
			if ((exthFlags & 0x40) != 0) {
				PdbUtil.skip(stream, length - 116);
				offset = length + 20;
				if (PdbUtil.readInt(stream) != 0x45585448) /* "EXTH" */ {
					return null;
				}
				PdbUtil.skip(stream, 4);
				final int recordsNumber = (int)PdbUtil.readInt(stream);
				offset += 8;
				for (int i = 0; i < recordsNumber; ++i) {
					final int type = (int)PdbUtil.readInt(stream);
					final int size = (int)PdbUtil.readInt(stream);
					offset += size;
					if (size <= 8) {
						continue;
					}
					switch (type) {
						default:
							PdbUtil.skip(stream, size - 8);
							break;
						case 201:
						{
							if (size == 12) {
								coverIndex = (int)PdbUtil.readInt(stream);
							} else {
								PdbUtil.skip(stream, size - 8);
							}
							break;
						}
						case 202:
						{
							if (size == 12) {
								thumbIndex = (int)PdbUtil.readInt(stream);
							} else {
								PdbUtil.skip(stream, size - 8);
							}
							break;
						}
					}
				}
			}
			final InputStream tempStream = stream;
			stream = null;
			tempStream.close();

			if (coverIndex == -1) {
				if (thumbIndex == -1) {
					return null;
				}
				coverIndex = thumbIndex;
			}

			MobipocketStream myMobipocketStream = new MobipocketStream(file);
			int start = myMobipocketStream.getImageOffset(coverIndex);
			if (start >= 0) {
				int len = myMobipocketStream.getImageLength(coverIndex);
				if (len > 0) {
					return new ZLFileImage(MimeType.IMAGE_AUTO, file, start, len);
				}
			}
			return null; 
		} catch (IOException e) {
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

	@Override
	public String readAnnotation(ZLFile file) {
		return null;
	}
}
