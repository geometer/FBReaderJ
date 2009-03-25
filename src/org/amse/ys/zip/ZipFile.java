package org.amse.ys.zip;

import java.io.*;
import java.util.Collection;
import java.util.Queue;
import java.util.LinkedList;
import java.util.TreeMap;

public class ZipFile {
    private final String myFileName;
    private final TreeMap<String,LocalFileHeader> myFileHeaders = new TreeMap<String,LocalFileHeader>();
    private final MyBufferedInputStream myBaseStream;

    private boolean myAllFilesAreRead;

    public ZipFile(String fileName) throws IOException {
        myFileName = fileName;
        myBaseStream = new MyBufferedInputStream(myFileName);
    }

    public Collection<LocalFileHeader> headers() {
        try {
            readAllHeaders();
        } catch (IOException e) {
        } catch (WrongZipFormatException e) {
        }
        return myFileHeaders.values();
    }

    private boolean readFileHeader(String fileToFind) throws IOException {
        int version2extract = myBaseStream.read2Bytes();
        int generalFlag = myBaseStream.read2Bytes();
        int compressionMethod = myBaseStream.read2Bytes();
        myBaseStream.skip(8);

        int compressedSize = myBaseStream.read4Bytes();
        int uncompressedSize = myBaseStream.read4Bytes();
        int fileNameSize = myBaseStream.read2Bytes();
        int extraField = myBaseStream.read2Bytes();

        final String fileName = myBaseStream.readString(fileNameSize);
        myBaseStream.skip(extraField);
        LocalFileHeader header = new LocalFileHeader(version2extract, generalFlag,
                compressionMethod, compressedSize, uncompressedSize,
                myBaseStream.offset(), fileName);
        myFileHeaders.put(fileName, header);
        if (header.sizeIsKnown()) {
            myBaseStream.skip(compressedSize);
        } else {
            findAndReadDescriptor(header);
        }
        return fileName.equals(fileToFind);
    }

    private void readAllHeaders() throws IOException, WrongZipFormatException {
        if (myAllFilesAreRead) {
            return;
        }
        myAllFilesAreRead = true;

        myBaseStream.reset();
        myFileHeaders.clear();

        while (true) {
            int header = myBaseStream.read4Bytes();
            if (header != LocalFileHeader.FILE_HEADER_SIGNATURE) {
                if (header == LocalFileHeader.FOLDER_HEADER_SIGNATURE) {
                    break; // central directory, no more files
                } else {
                    throw new WrongZipFormatException(
                            "readHeaders. Wrong signature found = " + header
                                    + " at position " + myBaseStream.offset());
                }
            }
            readFileHeader(null);
        }
        myBaseStream.reset();
    }

    /**
     * Finds descriptor of the last header and installs sizes of files
     */
    private void findAndReadDescriptor(LocalFileHeader header) throws IOException {
        while (true) {
            int signature = 0;
            do {
                int nextByte = myBaseStream.read();
                if (nextByte < 0) {
                    throw new IOException(
                            "readFileHeaders. Unexpected end of file when looking for DataDescriptor");
                }
                signature = ((signature << 8) & (0x0FFFFFFFF)) + (byte) nextByte;
            } while (signature != LocalFileHeader.DATA_DESCRIPTOR_SIGNATURE);
			myBaseStream.skip(4);
			int compressedSize = myBaseStream.read4Bytes();
			int uncompressedSize = myBaseStream.read4Bytes();
            if ((myBaseStream.offset() - header.OffsetOfLocalData - 16) == compressedSize) {
                header.setSizes(compressedSize, uncompressedSize);
                break;
            } else {
                myBaseStream.backSkip(12);
                continue;
            }
        }
    }

	private final Queue<MyBufferedInputStream> myStoredStreams = new LinkedList<MyBufferedInputStream>();

	synchronized void storeBaseStream(MyBufferedInputStream baseStream) {
		myStoredStreams.add(baseStream);	
	}

    synchronized private ZipInputStream createZipInputStream(LocalFileHeader header) throws IOException, WrongZipFormatException {
        MyBufferedInputStream baseStream =
			myStoredStreams.isEmpty() ?
				new MyBufferedInputStream(myFileName) :
				myStoredStreams.poll();
        return new ZipInputStream(this, baseStream, header);
    }

    public InputStream getInputStream(String entryName) throws IOException {
        if (!myFileHeaders.isEmpty()) {
            // trying to find in already ready array
            int i;
            final int listSize = myFileHeaders.size();
            LocalFileHeader header = myFileHeaders.get(entryName);
            if (header != null) {
                try {
                    return createZipInputStream(header);
                } catch (WrongZipFormatException e) {
                    return null;
                }
            }
            if (myAllFilesAreRead) {
                return null;
            }
        }
        // ready to read fileheader
        do {
            int signature = myBaseStream.read4Bytes();
            if (signature != LocalFileHeader.FILE_HEADER_SIGNATURE) {
                if (signature == LocalFileHeader.FOLDER_HEADER_SIGNATURE) {
                    break; // central directory, no more files
                } else {
                    throw new IOException(
                            "Wrong signature " + signature
                                    + " found at position " + myBaseStream.offset());
                }
            }
        } while (!readFileHeader(entryName));
        LocalFileHeader header = myFileHeaders.get(entryName);
        if (header != null) {
            try {
                return createZipInputStream(header);
            } catch (WrongZipFormatException e) {
            }
        }
        return null;
    }
}
