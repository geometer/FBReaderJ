package org.geometerplus.fbreader.formats.plucker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.geometerplus.fbreader.formats.pdb.PdbStream;
import org.geometerplus.fbreader.formats.pdb.PdbUtil;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

public class PluckerTextStream extends PdbStream {
	public PluckerTextStream(ZLFile file) {
		super(file);
		myFullBuffer = null;
	}
	
	public int read() {
		return 0;
	}
	
	public	boolean open() throws IOException {
		if (!super.open()) {
			return false;
		}

		myCompressionVersion = (short)PdbUtil.readUnsignedShort(myBase);

		myBuffer = new byte[65536];
		myFullBuffer = new byte[65536];

		myRecordIndex = 0;

		return true;
	}
	
	public	void close() throws IOException {
		if (myFullBuffer != null) {
			myFullBuffer = null;
		}
		super.close();
	}

	protected boolean fillBuffer() {
		while (myBufferOffset == myBufferLength) {
			if (myRecordIndex + 1 > myHeader.Offsets.size() - 1) {
				return false;
			}
			++myRecordIndex;
			int currentOffset = (Integer)myHeader.Offsets.get(myRecordIndex);
			if (currentOffset < ((PdbStream)myBase).offset()) {
				return false;
			}
			//((PdbStream)myBase).seek(currentOffset, true);
			int nextOffset =
				(myRecordIndex + 1 < myHeader.Offsets.size()) ?
						(Integer)myHeader.Offsets.get(myRecordIndex + 1) : ((PdbStream)myBase).sizeOfOpened();
			if (nextOffset < currentOffset) {
				return false;
			}
			try {
				processRecord(nextOffset - currentOffset);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	private void processRecord(int recordSize) throws IOException {
		myBase.skip(2);

		short paragraphs;
		paragraphs = (short)PdbUtil.readUnsignedShort(myBase);

		short size = (short)PdbUtil.readUnsignedShort(myBase);
		
		char type = 0; 
		//myBase.read((char*)&type, 0, 1);
		if (type > 1) { // this record is not text record
			return;
		}

		myBase.skip(1);

		ArrayList/*<Integer>*/ pars = new ArrayList();
		for (int i = 0; i < paragraphs; ++i) {
			short pSize = (short)PdbUtil.readUnsignedShort(myBase);
			pars.add(pSize);
			myBase.skip(2);
		}

		boolean doProcess = false;
		if (type == 0) {
			doProcess = myBase.read(myFullBuffer, 0, size) == size;
		} else if (myCompressionVersion == 1) {
			//doProcess =
				//DocDecompressor().decompress(myBase, myFullBuffer, recordSize - 8 - 4 * paragraphs, size) == size;
		} else if (myCompressionVersion == 2) {
			myBase.skip(2);
			//doProcess =
				//ZLZDecompressor(recordSize - 10 - 4 * paragraphs).decompress(myBase, myFullBuffer, size) == size;
		}
		if (doProcess) {
			myBufferLength = 0;
			myBufferOffset = 0;

			int start = 0;
			int end = 0;

			for (Iterator it = pars.iterator(); it.hasNext();) {
				start = end;
				end = start + (Integer)it.next();
				if (end > myFullBuffer[size]) {
					break;
				}
				processTextParagraph(myFullBuffer.toString().toCharArray(), start, end);
			}
		}
	}
	
	private	void processTextParagraph(char[] data, int start, int end) {
		int textStart = start;
		boolean functionFlag = false;
		for (int ptr = start; ptr < end; ++ptr) {
			if (data[ptr] == 0) {
				functionFlag = true;
				if (ptr != textStart) {
					//memcpy(myBuffer + myBufferLength, textStart, ptr - textStart);
					myBufferLength += ptr - textStart;
				}
			} else if (functionFlag) {
				int paramCounter = (data[ptr]) % 8;
				if (end - ptr > paramCounter + 1) {
					ptr += paramCounter;
				} else {
					ptr = end - 1;
				}
				functionFlag = false;
				textStart = ptr + 1;
			}
		}
		if (end != textStart) {
			//memcpy(myBuffer + myBufferLength, textStart, end - textStart);
			myBufferLength += end - textStart;
		}
	}

	private short myCompressionVersion;
	private	byte[] myFullBuffer;
	private	int myRecordIndex;

}
