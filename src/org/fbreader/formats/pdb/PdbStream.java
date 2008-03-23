package org.fbreader.formats.pdb;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.fbreader.formats.pdb.PdbUtil.PdbHeader;
import org.zlibrary.core.filesystem.ZLFile;

public abstract class PdbStream extends InputStream {
	
	public PdbStream(ZLFile file) {
		myBuffer = null;
	}
	public  int read(byte[] buffer,int offset, int maxSize) {
        int realSize = 0;
        while (realSize < maxSize) {
             if (!fillBuffer()) {
                  break;
             }
             int size = Math.min((maxSize - realSize), (myBufferLength - myBufferOffset));
             if (size > 0) {
                  System.arraycopy(myBuffer, myBufferOffset, buffer, offset + realSize, size);
                  realSize += size;
                  myBufferOffset += size;
             }
        }
        myOffset += realSize;
        return realSize;
   }
	/*public	int read(byte[] buffer,int offset, int maxSize) {
		int realSize = 0;
		while (realSize < maxSize) {
			if (!fillBuffer()) {
				break;
			}
			int size = Math.min((maxSize - realSize), (myBufferLength - myBufferOffset));
			if (size > 0) {
				if (buffer != null) {
					for (int i = 0; i < size; i++) {
						myBuffer[myBufferOffset+i] = buffer[realSize+i]; 
					}
					//memcpy(buffer + realSize, myBuffer + myBufferOffset, size);
				}
				realSize += size;
				myBufferOffset += size;
			}
		}
		myOffset += realSize;
		return realSize;
	}*/
	
	public	boolean open() throws IOException {
		close();
		if (myBase==null /*|| !myBase.open()*/ || !myHeader.read(myBase)) {
			return false;
		}

		myBase.skip(((Integer)myHeader.Offsets.get(0))/*, true*/);

		myBufferLength = 0;
		myBufferOffset = 0;

		myOffset = 0;

		return true;
	}
	
	public  void close() throws IOException {
		if (myBase != null) {
				myBase.close();
		}
		if (myBuffer != null) {
			myBuffer = null;
		}
	}

	public	void skip(int offset) throws IOException {
		if (offset > 0) {
			read(null,0, offset);
		} else if (offset < 0) {
			offset += this.offset();
			open();
			if (offset >= 0) {
				read(null, 0, offset);
			}
		}
	}
	
	public	int offset() {
		return myOffset;
	}
	
	public	int sizeOfOpened() {
		// TODO: implement
		return 0;
	}

	protected abstract boolean fillBuffer();

	protected   InputStream myBase;
	protected	int myOffset;
	protected	PdbHeader myHeader;
	protected	byte[] myBuffer;

	protected	short myBufferLength;
	protected	short myBufferOffset;
}
