package org.amse.ys.zip;

import java.io.*;
import java.util.*;

public final class ZipFile {
	public static interface InputStreamHolder {
		InputStream getInputStream() throws IOException;
	}

	private static final class FileInputStreamHolder implements InputStreamHolder {
		private final String myFilePath;

		FileInputStreamHolder(String filePath) {
			myFilePath = filePath;
		}

		public InputStream getInputStream() throws IOException {
			return new FileInputStream(myFilePath);
		}
	}

    private final InputStreamHolder myStreamHolder;
    private final LinkedHashMap<String,LocalFileHeader> myFileHeaders = new LinkedHashMap<String,LocalFileHeader>();

    private boolean myAllFilesAreRead;

    public ZipFile(String filePath) {
		this(new FileInputStreamHolder(filePath));
    }

    public ZipFile(InputStreamHolder streamHolder) {
        myStreamHolder = streamHolder;
    }

    public Collection<LocalFileHeader> headers() {
        try {
            readAllHeaders();
        } catch (IOException e) {
        }
        return myFileHeaders.values();
    }

    private boolean readFileHeader(MyBufferedInputStream baseStream, String fileToFind) throws IOException {
		LocalFileHeader header = new LocalFileHeader();
		header.readFrom(baseStream);

		if (header.Signature != LocalFileHeader.FILE_HEADER_SIGNATURE) {
			return false;
		}
		if (header.FileName != null) {
        	myFileHeaders.put(header.FileName, header);
			if (header.FileName.equals(fileToFind)) {
				return true;
			}
		}
        if ((header.Flags & 0x08) == 0) {
            baseStream.skip(header.CompressedSize);
        } else {
            findAndReadDescriptor(baseStream, header);
        }
        return false;
    }

    private void readAllHeaders() throws IOException {
        if (myAllFilesAreRead) {
            return;
        }
        myAllFilesAreRead = true;

		MyBufferedInputStream baseStream = getBaseStream();
        baseStream.setPosition(0);
        myFileHeaders.clear();

		try {
            while (true) {
                readFileHeader(baseStream, null);
            }
        } finally {
			storeBaseStream(baseStream);
		}
    }

    /**
     * Finds descriptor of the last header and installs sizes of files
     */
    private void findAndReadDescriptor(MyBufferedInputStream baseStream, LocalFileHeader header) throws IOException {
		Decompressor decompressor = Decompressor.init(baseStream, header);
		int uncompressedSize = 0;
		while (true) {
			int blockSize = decompressor.read(null, 0, 2048);
			if (blockSize <= 0) {
				break;
			}
			uncompressedSize += blockSize;
		}
		header.UncompressedSize = uncompressedSize;
    }

	private final Queue<MyBufferedInputStream> myStoredStreams = new LinkedList<MyBufferedInputStream>();

	synchronized void storeBaseStream(MyBufferedInputStream baseStream) {
		myStoredStreams.add(baseStream);	
	}

	synchronized MyBufferedInputStream getBaseStream() throws IOException {
        MyBufferedInputStream baseStream = myStoredStreams.poll();
		return (baseStream != null) ? baseStream : new MyBufferedInputStream(myStreamHolder);
	}

    private ZipInputStream createZipInputStream(LocalFileHeader header) throws IOException {
        return new ZipInputStream(this, header);
    }

    public int getEntrySize(String entryName) throws IOException {
		return getHeader(entryName).UncompressedSize;
	}

    public InputStream getInputStream(String entryName) throws IOException {
		return createZipInputStream(getHeader(entryName));
    }

    public LocalFileHeader getHeader(String entryName) throws IOException {
        if (!myFileHeaders.isEmpty()) {
            LocalFileHeader header = myFileHeaders.get(entryName);
            if (header != null) {
                return header;
            }
            if (myAllFilesAreRead) {
				throw new ZipException("Entry " + entryName + " is not found");
            }
        }
        // ready to read file header
		MyBufferedInputStream baseStream = getBaseStream();
		baseStream.setPosition(0);
		try {
            while (!readFileHeader(baseStream, entryName)) {
			}
            LocalFileHeader header = myFileHeaders.get(entryName);
            if (header != null) {
            	return header;
            }
		} finally {
			storeBaseStream(baseStream);
		}
		throw new ZipException("Entry " + entryName + " is not found");
    }
}
