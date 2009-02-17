package org.amse.ys.zip;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class ZipFile {
    private final String myFileName;
    private final ArrayList<LocalFileHeader> myFileHeaders = new ArrayList<LocalFileHeader>();
    private final MyBufferedInputStream myBaseStream;

    private boolean myAllFilesAreRead;

    public ZipFile(String fileName) throws IOException {
        myFileName = fileName;
        myBaseStream = new MyBufferedInputStream(myFileName);
    }

    public List<LocalFileHeader> headers() {
        try {
            readAllHeaders();
        } catch (IOException e) {
        } catch (WrongZipFormatException e) {
        }
        return Collections.unmodifiableList(myFileHeaders);
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
        myFileHeaders.add(new LocalFileHeader(version2extract, generalFlag,
                compressionMethod, compressedSize, uncompressedSize,
                myBaseStream.offset(), fileName));
        if (fileName.equals(fileToFind)) {
            return true;
        }
        if ((generalFlag & 8) == 0) {
            myBaseStream.skip(compressedSize);
        } else {
            System.out
                    .println("Warning: flag 3 is set! Trying to find header.");
            findAndReadDescriptor();
        }
        return false;
    }

    private void readAllHeaders() throws IOException, WrongZipFormatException {
        if (myAllFilesAreRead) {
            return;
        }
        myAllFilesAreRead = true;

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
    private void findAndReadDescriptor() throws IOException {
        byte[] tempArray = new byte[12];
        while (true) {
            int signature = 0;
            do {
                int nextByte = myBaseStream.read();
                if (nextByte < 0) {
                    throw new IOException(
                            "readFileHeaders. Unexpected end of file when looking for DataDescriptor");
                }
                signature = ((signature << 8) & (0x0ffffffff))
                        + (byte) nextByte;
            } while (signature != LocalFileHeader.DATA_DESCRIPTOR_SIGNATURE);
            myBaseStream.read(tempArray);
            int compressedSize =
                ((tempArray[7] & 0xff) << 24) +
                ((tempArray[6] & 0xff) << 16) +
                ((tempArray[5] & 0xff) << 8) +
                (tempArray[4] & 0xff);
            int uncompressedSize =
                ((tempArray[11] & 0xff) << 24) +
                ((tempArray[10] & 0xff) << 16) +
                ((tempArray[9] & 0xff) << 8) +
                (tempArray[8] & 0xff);
            LocalFileHeader lastHeader = myFileHeaders.get(myFileHeaders.size() - 1);
            if ((myBaseStream.offset() - lastHeader.OffsetOfLocalData - 16) == compressedSize) {
                lastHeader.setSizes(compressedSize, uncompressedSize);
                break;
            } else {
                myBaseStream.backSkip(12);
                continue;
            }
        }
    }

    public InputStream getInputStream(String entryName) throws IOException {
        if (!myFileHeaders.isEmpty()) {
            // trying to find in already ready array
            int i;
            final int listSize = myFileHeaders.size();
			LocalFileHeader header = null;
            for (i = 0; i < listSize; i++) {
				header = myFileHeaders.get(i);
                if (header.FileName.equals(entryName)) {
					try {
                    	return new ZipInputStream(myFileName, header);
					} catch (WrongZipFormatException e) {
						return null;
					}
                }
            }
            if (myAllFilesAreRead) {
                return null;
            }
            if (header.sizeIsKnown()) {
                myBaseStream.setPosition(header.OffsetOfLocalData + header.getCompressedSize());
            } else {
                findAndReadDescriptor();
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
		LocalFileHeader header = myFileHeaders.get(myFileHeaders.size() - 1);
        if (header.FileName.equals(entryName)) {
			try {
            	return new ZipInputStream(myFileName, header);
			} catch (WrongZipFormatException e) {
				return null;
			}
        }
        return null;
    }
}
