/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.plucker;

import java.io.IOException;
import java.util.*;
import java.util.zip.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.*;
import org.geometerplus.zlibrary.core.util.MimeType;
import org.geometerplus.zlibrary.core.util.ZLInputStreamWithOffset;

import org.geometerplus.zlibrary.text.model.*;

import org.geometerplus.fbreader.bookmodel.*;
import org.geometerplus.fbreader.formats.pdb.*;

public class PluckerBookReader extends BookReader {
	private final ZLFile myFile;
	private final int myFileSize;
	private ZLInputStreamWithOffset myStream;
	private	int myFont;
	private	char[] myCharBuffer;
	private	String myConvertedTextBuffer;
	private	boolean myParagraphStarted = false;
	private	boolean myBufferIsEmpty;
	//private	ZLTextForcedControlEntry myForcedEntry;
	private	final ArrayList/*<std::pair<FBTextKind,bool> >*/ myDelayedControls = new ArrayList();
	private	final ArrayList/*<std::string> */myDelayedHyperlinks = new ArrayList();
	private	short myCompressionVersion;
	private	char myBytesToSkip;

	private	final ArrayList/*<std::pair<int, int> >*/ myReferencedParagraphs = new ArrayList();
	private	final HashMap/*<int, std::vector<int> >*/ myParagraphMap = new HashMap(); /*<int, vector<pair<int, int>>>*/
	private	ArrayList/*<Integer, Integer>*/ myParagraphVector = new ArrayList();
	private	boolean myParagraphStored;
	
	public PluckerBookReader(ZLFile file, BookModel model, String encoding) {
		super(model);
		//myConverter = new EncodedTextReader(encoding).getConverter(); 
		myFile = file; 
		myFileSize = (int)file.size();
		myFont = FontType.FT_REGULAR;
		myCharBuffer = new char[65535];
		//myForcedEntry = null;
	}

	public boolean readDocument() {
		try {
			myStream = new ZLInputStreamWithOffset(myFile.getInputStream());
        
			PdbHeader header = new PdbHeader(myStream);
        
			setMainTextModel();
			myFont = FontType.FT_REGULAR;
        
			for (int index = 0; index < header.Offsets.length; ++index) {
				int currentOffset = myStream.offset();
				int pit = header.Offsets[index];
				if (currentOffset > pit) {
					break;
				}
				//myStream.seek(pit - currentOffset, false);
				myStream.skip(pit - currentOffset);
				
				if (myStream.offset() != pit) {
					break;
				}
				int recordSize = ((index != header.Offsets.length - 1) ? header.Offsets[index + 1] : myFileSize) - pit;
				readRecord(recordSize);
			}
			myStream.close();
		} catch (IOException e) {
			return false;
		}

		for (Iterator it = myReferencedParagraphs.iterator(); it.hasNext();) {
			Pair pair = (Pair)it.next();
			int first = (Integer)pair.myFirst;
			int second = (Integer)pair.mySecond;
			ArrayList/*<Integer>*/ list = (ArrayList)myParagraphMap.get(first);
			if (list != null) {
				for(int k = second; k < list.size(); ++k) {
					if (((Integer) ((Pair)list.get(k)).myFirst) != -1) {
						//addHyperlinkLabel(fromNumber(first) + '#' + fromNumber(second), (Integer)list.get(k));
						final Pair p = (Pair)list.get(k);
						//addHyperlinkLabel(fromNumber(first) + '#' + fromNumber(second), (Integer) p.mySecond, (Integer) p.myFirst);
						break;						
					}
				}
			}
		}
		myReferencedParagraphs.clear();
		myParagraphMap.clear();
		return true;
	}

	private class FontType {
			public static final int FT_REGULAR = 0;
			public static final int FT_H1 = 1;
			public static final int FT_H2 = 2;
			public static final int FT_H3 = 3;
			public static final int FT_H4 = 4;
			public static final int FT_H5 = 5;
			public static final int FT_H6 = 6;
			public static final int FT_BOLD = 7;
			public static final int FT_TT = 8;
			public static final int FT_SMALL = 9;
			public static final int FT_SUB = 10;
			public static final int FT_SUP = 11;
		};

	private	void readRecord(int recordSize) throws IOException {
		int uid = PdbUtil.readShort(myStream);
		if (uid == 1) {
			myCompressionVersion = (short) PdbUtil.readShort(myStream );
		} else {
			int paragraphs = PdbUtil.readShort(myStream);

			int size = PdbUtil.readShort(myStream);
            //TODO ??????  
			int type = myStream.read();

			int flags = myStream.read();

			switch (type) {
				case 0: // text (TODO: found sample file and test this code)
				case 1: // compressed text
				{
					ArrayList/*<Integer>*/ pars = new ArrayList();
					for (int i = 0; i < paragraphs; ++i) {
						int pSize = PdbUtil.readShort(myStream);
						pars.add(pSize);
						myStream.skip(2);
					}

					boolean doProcess = false;
					if (type == 0) {//?
						byte[] buf = new byte[size];
						doProcess = myStream.read(buf, 0, (int)size) == size;
						if (doProcess) {
							// TODO: use encoding!!!!
							// TODO: don't create any new objects!!!!
							myCharBuffer = new String(buf).toCharArray();
						}
					} else if (myCompressionVersion == 1) {
						byte[] buf = new byte[size];
						doProcess =
							DocDecompressor.decompress(myStream, buf, recordSize - 8 - 4 * paragraphs) == size;
						if (doProcess) {
							myCharBuffer = new String(buf).toCharArray();
						}
					} else if (myCompressionVersion == 2) {
						byte input [] = new byte[(int) (recordSize - 10 - 4 * paragraphs)];
						final int inputSize = myStream.read(input);
						Inflater decompressor = new Inflater();
						decompressor.setInput(input, 0, inputSize);
						byte output [] = new byte[size];
						try {
							doProcess = decompressor.inflate(output) == size;
							decompressor.end();
							myCharBuffer = new String(output, 0, size).toCharArray();
						} catch (DataFormatException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						}
						//doProcess =
							//ZLZDecompressor(recordSize - 10 - 4 * paragraphs).
								//decompress(myStream, myCharBuffer, size) == size;
					}
					if (doProcess) {
						addHyperlinkLabel(fromNumber(uid));
						myParagraphMap.put(uid, new ArrayList());
						myParagraphVector = (ArrayList)myParagraphMap.get(uid);
						processTextRecord(size, pars);
						if ((flags & 0x1) == 0) {
//							insertEndOfTextParagraph();
							//setNewTextModel();
						}
					}
					break;
				}
				case 2: // image
				case 3: // compressed image
				{
					ZLImage image = null;
					if (type == 2) {
						image = new PluckerFileImage(MimeType.IMAGE_PALM, myFile, myStream.offset(), recordSize - 8);
					} else if (myCompressionVersion == 1) {
						image = new DocCompressedFileImage(MimeType.IMAGE_PALM, myFile, myStream.offset(), recordSize - 8);
					} else if (myCompressionVersion == 2) {
						image = new ZCompressedFileImage(MimeType.IMAGE_PALM, myFile, myStream.offset() + 2, recordSize - 10);
					}
					if (image != null) {
						addImage(fromNumber(uid), image);
					}
					break;
				}
				case 9: // category record is ignored
					break;
				case 10:
					short typeCode = (short) PdbUtil.readShort(myStream);
					break;
				case 11: // style sheet record is ignored
					break;
				case 12: // font page record is ignored
					break;
				case 13: // TODO: process tables
				case 14: // TODO: process tables
					break;
				case 15: // multiimage
				{
					short columns = (short) PdbUtil.readShort(myStream);
					short rows = (short) PdbUtil.readShort(myStream);
					/*PluckerMultiImage image = new PluckerMultiImage(rows, columns, Model.getImageMap());
					for (int i = 0; i < size / 2 - 2; ++i) {
						short us = (short)myStream.read();
						PdbUtil.readShort(myStream, us);
						image.addId(fromNumber(us));
					}
					addImage(fromNumber(uid), image);
					*/break;
				}
				default:
					//std::cerr << "type = " << (int)type << "\n";
					break;
			}
		}	
	}
	
    private	void processTextRecord(int size, ArrayList/*<Integer>*/ pars) {
    	int start = 0;
    	int end = 0;

    	for (Iterator it = pars.iterator(); it.hasNext();) {
    		start = end;
    		end = start + (Integer)it.next();
    		if (end > size) {
    			return;
    		}
    		myParagraphStored = false;
    		processTextParagraph(myCharBuffer, start, end);
    		if (!myParagraphStored) {
    			myParagraphVector.add(new Pair(-1, -1));
    		}
    	}
    }
    
    private	void processTextParagraph(char[] data, int start, int end) {
    	changeFont(FontType.FT_REGULAR);
    	while (popKind()) {}

    	myParagraphStarted = false;
    	myBytesToSkip = 0;

    	int textStart = start;
    	boolean functionFlag = false;
    	for (int ptr = start; ptr < end; ++ptr) {
    		if (data[ptr] == 0) {
    			functionFlag = true;
    			if (ptr > textStart) {
    				safeBeginParagraph();
    	//			myConvertedTextBuffer = "";//.erase();
    				myConvertedTextBuffer = "";//myConverter.convert(data, textStart, ptr);
    				addData(myConvertedTextBuffer.toCharArray());
    				myBufferIsEmpty = false;
    			}
    		} else if (functionFlag) {
    			int paramCounter = (data[ptr]) % 8;
    			if (end - ptr > paramCounter) {
    				processTextFunction(data, ptr);
    				ptr += paramCounter;
    			} else {
    				ptr = end - 1;
    			}
    			functionFlag = false;
    			if (myBytesToSkip > 0) {
    				ptr += myBytesToSkip;
    				myBytesToSkip = 0;
    			}
    			textStart = ptr + 1;
    		} else {
    			if (data[ptr] == 0xA0) {
    				data[ptr] = 0x20;
    			}
    			if (!myParagraphStarted && (textStart == ptr) && (data[ptr] == ' ')) {
    				++textStart;
    			}
    		}
    	}
    	if (end > textStart) {
    		safeBeginParagraph();
    	//	myConvertedTextBuffer = "";//erase();
    		myConvertedTextBuffer = "";//myConverter.convert(data, textStart, end);
    		addData(myConvertedTextBuffer.toCharArray());
    		myBufferIsEmpty = false;
    	}
    	safeEndParagraph();
    	//if (myForcedEntry != null) {
    	//	myForcedEntry = null;
    	//}
    	myDelayedControls.clear();
    }
    
    private	void processTextFunction(char[] ptr, int cur) {
			switch (ptr[cur]) {
				case 0x08:
					safeAddControl(FBTextKind.INTERNAL_HYPERLINK, false);
					break;
				case 0x0A:
					safeAddHyperlinkControl(fromNumber(twoBytes(ptr, cur+ 1)));
					break;
				case 0x0C:
				{
					int sectionNum = twoBytes(ptr, cur + 1);
					int paragraphNum = twoBytes(ptr, cur + 3);
					safeAddHyperlinkControl(fromNumber(sectionNum) + '#' + fromNumber(paragraphNum));
					myReferencedParagraphs.add(new Pair(sectionNum, paragraphNum));
					break;
				}
				case 0x11:
					changeFont((ptr[cur + 1]));
					break;
				case 0x1A:
					safeBeginParagraph();
					addImageReference(fromNumber(twoBytes(ptr, cur + 1)), (short) 0);
					break;
				case 0x22:
					if (!myParagraphStarted) {
						//if (myForcedEntry == null) {
						//	myForcedEntry = new ZLTextForcedControlEntry();
						//}
						//myForcedEntry.setLeftIndent((short)ptr[cur + 1]);
						//myForcedEntry.setRightIndent((short)ptr[cur + 2]);
					}
					break;
				case 0x29:
					if (!myParagraphStarted) {
						//if (myForcedEntry == null) {
						//	myForcedEntry = new ZLTextForcedControlEntry();
						//}
						//switch (ptr[cur + 1]) {
						//	case 0: myForcedEntry.setAlignmentType(ZLTextAlignmentType.ALIGN_LEFT); break;
						//	case 1: myForcedEntry.setAlignmentType(ZLTextAlignmentType.ALIGN_RIGHT); break;
						//	case 2: myForcedEntry.setAlignmentType(ZLTextAlignmentType.ALIGN_CENTER); break;
						//	case 3: myForcedEntry.setAlignmentType(ZLTextAlignmentType.ALIGN_JUSTIFY); break;
						//}
					}
					break;
				case 0x33: // just break line instead of horizontal rule (TODO: draw horizontal rule?)
					safeEndParagraph();
					break;
				case 0x38:
					safeEndParagraph();
					break;
				case 0x40: 
					safeAddControl(FBTextKind.EMPHASIS, true);
					break;
				case 0x48:
					safeAddControl(FBTextKind.EMPHASIS, false);
					break;
				case 0x53: // color setting is ignored
					break;
				case 0x5C:
					addImageReference(fromNumber(twoBytes(ptr, cur + 3)), (short) 0);
					break;
				case 0x60: // underlined text is ignored
					break;
				case 0x68: // underlined text is ignored
					break;
				case 0x70: // strike-through text is ignored
					break;
				case 0x78: // strike-through text is ignored
					break;
				case 0x83: 
				{
					safeBeginParagraph();
					addData(new char[] { (char)twoBytes(ptr, cur + 2) });
					myBufferIsEmpty = false;
					myBytesToSkip = ptr[cur+1];
					break;
				}
				case 0x85: // TODO: process 4-byte unicode character
					break;
				case 0x8E: // custom font operations are ignored
				case 0x8C:
				case 0x8A:
				case 0x88:
					break;
				case 0x90: // TODO: add table processing
				case 0x92: // TODO: process table
				case 0x97: // TODO: process table
					break;
				default: // this should be impossible
					//std::cerr << "Oops... function #" << (int)(unsigned char)*ptr << "\n";
					break;
	}	
    }
    
    private	void setFont(int font, boolean start) {
			switch (font) {
				case FontType.FT_REGULAR:
					break;
				case FontType.FT_H1:
				case FontType.FT_H2:
				case FontType.FT_H3:
				case FontType.FT_H4:
				case FontType.FT_H5:
				case FontType.FT_H6:
					processHeader(font, start);
					break;
				case FontType.FT_BOLD:
					safeAddControl(FBTextKind.BOLD, start);
					break;
				case FontType.FT_TT:
					safeAddControl(FBTextKind.CODE, start);
					break;
				case FontType.FT_SMALL:
					break;
				case FontType.FT_SUB:
					safeAddControl(FBTextKind.SUB, start);
					break;
				case FontType.FT_SUP:
					safeAddControl(FBTextKind.SUP, start);
					break;
	    }
    }

    private	void changeFont(int font) {
    	if (myFont == font) {
    		return;
    	}
    	setFont(myFont, false);
    	myFont = font;
    	setFont(myFont, true);
    }

    private void safeAddControl(byte kind, boolean start) {
    	if (myParagraphStarted) {
    		addControl((Byte)kind, (Boolean)start);
    	} else {
    		myDelayedControls.add(new Pair(kind, start));
    	}
    }
    private void safeAddHyperlinkControl(String id) {
    	if (myParagraphStarted) {
    		addHyperlinkControl(FBTextKind.INTERNAL_HYPERLINK, id);
    	} else {
    		myDelayedHyperlinks.add(id);
    	}
    }
    
    private void safeBeginParagraph() {
    	if (!myParagraphStarted) {
    		myParagraphStarted = true;
    		myBufferIsEmpty = true;
    		beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
    		if (!myParagraphStored) {
    			//final ArrayList models = Model.getBookTextModels();
    			//myParagraphVector.add(new Pair(((ZLTextPlainModel) models.get(models.size()-1)/*BookTextModel*/).getParagraphsNumber() - 1, models.size() - 1));
    			myParagraphStored = true;
    		}
    		for (Iterator it = myDelayedControls.iterator(); it.hasNext(); ) {
    			Pair pit = (Pair)it.next();
    			addControl((Byte)pit.myFirst, (Boolean)pit.mySecond);
    		}
    		//if (myForcedEntry != null) {
    		//	addControl(myForcedEntry);
    		//} else {
    			addControl(FBTextKind.REGULAR, true);
    		//}
    		for (Iterator it = myDelayedHyperlinks.iterator(); it.hasNext(); ) {
    			addHyperlinkControl(FBTextKind.INTERNAL_HYPERLINK, (String)it.next());
    		}
    		myDelayedHyperlinks.clear();
    	}
    }
    
    private void safeEndParagraph() {
    	if (myParagraphStarted) {
    		if (myBufferIsEmpty) {
    			final String SPACE = " ";
    			addData(SPACE.toCharArray());
    		}
    		endParagraph();
    		myParagraphStarted = false;
    	}
    }

    private void processHeader(int font, boolean start) {
    	if (start) {
    		enterTitle();
    		int kind;
    		switch (font) {
    			case FontType.FT_H1:
    				kind = FBTextKind.H1;
    				break;
    			case FontType.FT_H2:
    				kind = FBTextKind.H2;
    				break;
    			case FontType.FT_H3:
    				kind = FBTextKind.H3;
    				break;
    			case FontType.FT_H4:
    				kind = FBTextKind.H4;
    				break;
    			case FontType.FT_H5:
    				kind = FBTextKind.H5;
    				break;
    			case FontType.FT_H6:
    			default:
    				kind = FBTextKind.H6;
    				break;
    		}
    		pushKind((byte)kind);
    	} else {
    		popKind();
    		exitTitle();
    	}
    }
	
	static private class Pair {
		public Object myFirst;
		public Object mySecond;
		Pair(Object first, Object second) {
			this.myFirst = first;
			this.mySecond = second;
		}
	}

	static private int twoBytes(char[] ptr, int offset) {
		return 256 * ptr[offset] + ptr[offset+1];
	}

	static String fromNumber(int num) {
		String str = "";
		str += num;
		//ZLStringUtil.appendNumber(str, num);
		return str;
	}
}
