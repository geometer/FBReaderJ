package org.amse.ys.zip;

import java.io.*;
import java.util.Collection;
import java.util.Queue;
import java.util.LinkedList;
import java.util.LinkedHashMap;

public final class ZipFile {
    private final String myFileName;
    private final LinkedHashMap<String,LocalFileHeader> myFileHeaders = new LinkedHashMap<String,LocalFileHeader>();

    private boolean myAllFilesAreRead;

    public ZipFile(String fileName) {
        myFileName = fileName;
    }

    public Collection<LocalFileHeader> headers() {
        try {
            readAllHeaders();
        } catch (IOException e) {
        }
        return myFileHeaders.values();
    }

    private boolean readFileHeader(MyBufferedInputStream baseStream, String fileToFind) throws IOException {
        int version2extract = baseStream.read2Bytes();
        int generalFlag = baseStream.read2Bytes();
        int compressionMethod = baseStream.read2Bytes();
        baseStream.skip(8);

        int compressedSize = baseStream.read4Bytes();
        int uncompressedSize = baseStream.read4Bytes();
        int fileNameSize = baseStream.read2Bytes();
        int extraField = baseStream.read2Bytes();

        final String fileName = baseStream.readString(fileNameSize);
        baseStream.skip(extraField);
        LocalFileHeader header = new LocalFileHeader(version2extract, generalFlag,
                compressionMethod, compressedSize, uncompressedSize,
                baseStream.offset(), fileName);
        myFileHeaders.put(fileName, header);
        if (header.sizeIsKnown()) {
            baseStream.skip(compressedSize);
        } else {
            findAndReadDescriptor(baseStream, header);
        }
        return fileName.equals(fileToFind);
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
                int header = baseStream.read4Bytes();
                if (header != LocalFileHeader.FILE_HEADER_SIGNATURE) {
                    if (header == LocalFileHeader.FOLDER_HEADER_SIGNATURE) {
                        break; // central directory, no more files
                    } else {
                        throw new ZipException(
                                "readHeaders. Wrong signature found = " + header
                                        + " at position " + baseStream.offset());
                    }
                }
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
        while (true) {
            int signature = 0;
            do {
                int nextByte = baseStream.read();
                if (nextByte < 0) {
                    throw new ZipException(
                            "readFileHeaders. Unexpected end of file when looking for DataDescriptor");
                }
                signature = ((signature << 8) & (0x0FFFFFFFF)) + (byte) nextByte;
            } while (signature != LocalFileHeader.DATA_DESCRIPTOR_SIGNATURE);
			baseStream.skip(4);
			int compressedSize = baseStream.read4Bytes();
			int uncompressedSize = baseStream.read4Bytes();
            if ((baseStream.offset() - header.OffsetOfLocalData - 16) == compressedSize) {
                header.setSizes(compressedSize, uncompressedSize);
                break;
            } else {
                baseStream.backSkip(12);
                continue;
            }
        }
    }

	private final Queue<MyBufferedInputStream> myStoredStreams = new LinkedList<MyBufferedInputStream>();

	synchronized void storeBaseStream(MyBufferedInputStream baseStream) {
		myStoredStreams.add(baseStream);	
	}

	synchronized MyBufferedInputStream getBaseStream() throws IOException {
        MyBufferedInputStream baseStream = myStoredStreams.poll();
		return (baseStream != null) ? baseStream : new MyBufferedInputStream(myFileName);
	}

    private ZipInputStream createZipInputStream(LocalFileHeader header) throws IOException {
        return new ZipInputStream(this, header);
    }

    public InputStream getInputStream(String entryName) throws IOException {
        if (!myFileHeaders.isEmpty()) {
            LocalFileHeader header = myFileHeaders.get(entryName);
            if (header != null) {
                return createZipInputStream(header);
            }
            if (myAllFilesAreRead) {
                return null;
            }
        }
        // ready to read fileheader
		MyBufferedInputStream baseStream = getBaseStream();
		baseStream.setPosition(0);
		try {
            do {
                int signature = baseStream.read4Bytes();
                if (signature != LocalFileHeader.FILE_HEADER_SIGNATURE) {
                    if (signature == LocalFileHeader.FOLDER_HEADER_SIGNATURE) {
                        break; // central directory, no more files
                    } else {
                        throw new ZipException(
                                "Wrong signature " + signature
                                        + " found at position " + baseStream.offset());
                    }
                }
            } while (!readFileHeader(baseStream, entryName));
            LocalFileHeader header = myFileHeaders.get(entryName);
            if (header != null) {
                try {
                    return createZipInputStream(header);
                } catch (ZipException e) {
                }
            }
		} finally {
			storeBaseStream(baseStream);
		}
        return null;
    }
}
