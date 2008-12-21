package org.amse.ys.zip;

import java.io.*;
import java.util.ArrayList;

public class ZipInputStream extends InputStream {
    private final String myFileName;
    private MyBufferedReader myZipFile; // / CORRECT public to private!
    private ArrayList<LocalFileHeader> myFileHeaders;
    private int myCurrentPosition;

    private boolean myAllFilesAreRead;
    private boolean myFileIsOpened;
    private int myIndexOfFileOpened;
    private Decompressor myDecompressor;

    public int getCompressionMethod(int i) {
        return myFileHeaders.get(i).CompressionMethod;
    }

    public ZipInputStream(String fileName) throws IOException {
        myFileName = fileName;
        myFileHeaders = new ArrayList<LocalFileHeader>();
        myZipFile = new MyBufferedReader(myFileName);
    }

    public int getNumberOfFiles() {
        return myFileHeaders.size();
    }

    private int read2Bytes() throws IOException, WrongZipFormatException {
        int low = myZipFile.read();
        int high = myZipFile.read();
        myCurrentPosition += 2;
        if ((low < 0) || (high < 0)) {
            throw new WrongZipFormatException(
                    "read2bytes. unexpected end of file at position "
                            + myCurrentPosition);
        }
        return ((high << 8) + low);
    }

    private int read4Bytes() throws IOException, WrongZipFormatException {
        int firstByte = myZipFile.read();
        int secondByte = myZipFile.read();
        int thirdByte = myZipFile.read();
        int fouthByte = myZipFile.read();
        myCurrentPosition += 4;
        if (fouthByte < 0) {
            throw new WrongZipFormatException(
                    "read4bytes. unexpected end of file at position "
                            + myCurrentPosition);
        }
        int hiWord = ((fouthByte << 8) + thirdByte);
        int lowWord = (secondByte << 8) + firstByte;
        return ((hiWord << 16) + lowWord);
    }

    private String readString(int stringLength) throws IOException {
        char[] array = new char[stringLength];
        for (int i = 0; i < stringLength; i++) {
            array[i] = (char) myZipFile.read();
        }
        myCurrentPosition += stringLength;
        return new String(array);
    }

    public String getFileName(int index) {
        return myFileHeaders.get(index).FileName;
    }

    private boolean readFileHeader(boolean breakable, String fileToFind)
            throws IOException, WrongZipFormatException {
        int version2extract = read2Bytes();
        int generalFlag = read2Bytes();
        int compressionMethod = read2Bytes();
        myCurrentPosition += 8;
        myZipFile.skip(8);

        int compressedSize = read4Bytes();
        int uncompressedSize = read4Bytes();
        int fileNameSize = read2Bytes();
        int extraField = read2Bytes();

        String fileName = readString(fileNameSize);
        myZipFile.skip(extraField);
        myCurrentPosition += extraField;
        myFileHeaders.add(new LocalFileHeader(version2extract, generalFlag,
                compressionMethod, compressedSize, uncompressedSize,
                myCurrentPosition, fileName));
        if ((breakable) && (fileName.equals(fileToFind))) {
            return true;
        }
        if ((generalFlag & 8) == 0) {
            myZipFile.skip(compressedSize);
            myCurrentPosition += compressedSize;
        } else {
            System.out
                    .println("Warning: flag 3 is set! Trying to find header.");
            findAndReadDescriptor();
        }
        return false;
    }
    
    public int available() {
        return myDecompressor.available();
    }

    public void readHeaders() throws IOException, WrongZipFormatException {
        while (true) {
            int header = read4Bytes();
            if (header != LocalFileHeader.FILE_HEADER_SIGNATURE) {
                if (header == LocalFileHeader.FOLDER_HEADER_SIGNATURE) {
                    break; // central directory, no more files
                } else {
                    throw new WrongZipFormatException(
                            "readHeaders. Wrong signature found = " + header
                                    + " at position " + myCurrentPosition);
                }
            }
            readFileHeader(false, "");
        }
        myAllFilesAreRead = true;
        myCurrentPosition = 0;
        myZipFile.reset();
    }

    private int byteToLong(byte b) {
        return (b & 255);
    }

    /**
     * Finds descriptor of the last header and installs sizes of files
     */
    private void findAndReadDescriptor() throws IOException,
            WrongZipFormatException {
        byte[] tempArray = new byte[12];
        mainLoop: while (true) {
            int tempHeader = 0;
            do {
                int nextByte = myZipFile.read();
                myCurrentPosition += 1;
                if (nextByte < 0) {
                    throw new WrongZipFormatException(
                            "readFileHeaders. Unexpected end of file when looking for DataDescriptor");
                }
                tempHeader = ((tempHeader << 8) & (0x0ffffffff))
                        + (byte) nextByte;
            } while (tempHeader != LocalFileHeader.DATA_DESCRIPTOR_SIGNATURE);
            myZipFile.read(tempArray);
            myCurrentPosition += 12;
            long compressedSize = (byteToLong(tempArray[7]) << 24)
                    + (byteToLong(tempArray[6]) << 16)
                    + (byteToLong(tempArray[5]) << 8) + byteToLong(tempArray[4]);
            long uncompressedSize = ((byteToLong(tempArray[11]) << 24)
                    + (byteToLong(tempArray[10]) << 16) + (byteToLong(tempArray[9]) << 8) + byteToLong(tempArray[8]));
            LocalFileHeader lastHeader = myFileHeaders
                    .get(myFileHeaders.size() - 1);
            if ((myCurrentPosition - lastHeader.OffsetOfLocalData - 16) == compressedSize) {
                lastHeader.setSizes((int) compressedSize,
                        (int) uncompressedSize);
                break mainLoop;
            } else {
                myZipFile.backSkip(12);
                continue mainLoop;
            }
        }
    }

    public boolean openFile(String fileToOpen) throws IOException,
            WrongZipFormatException {
        if (!myFileHeaders.isEmpty()) {
            // trying to find in already ready array
            int i = 0;
            int maxI = myFileHeaders.size();
            for (i = 0; i < maxI; i++) {
                if (myFileHeaders.get(i).FileName.equals(fileToOpen)) {
                    // file is found
                    myFileIsOpened = true;
                    myIndexOfFileOpened = i;
                    // making nessesary shift
                    int offset = myFileHeaders.get(i).OffsetOfLocalData;
                    myZipFile.setPosition(offset);
                    myDecompressor = Decompressor.init(myZipFile, myFileHeaders
                            .get(myIndexOfFileOpened));
                    return true;
                }
            }
            // there is no file like we look for in our array
            if (myAllFilesAreRead) {
                return false;
            }
            // Устанавливаем указатель чтения на хэдер, далее - запускаем цикл
            LocalFileHeader lastHeader = myFileHeaders.get(i);
            if (lastHeader.sizeIsKnown()) {
                int nextHeaderShift = lastHeader.OffsetOfLocalData
                        + lastHeader.getCompressedSize();
                myZipFile.setPosition(nextHeaderShift);
            } else {
                findAndReadDescriptor();
            }
        }
        // ready to read fileheader
        do {
            int header = read4Bytes();
            if (header != LocalFileHeader.FILE_HEADER_SIGNATURE) {
                if (header == LocalFileHeader.FOLDER_HEADER_SIGNATURE) {
                    break; // central directory, no more files
                } else {
                    throw new WrongZipFormatException(
                            "readHeaders. Wrong signature found = " + header
                                    + " at position " + myCurrentPosition);
                }
            }
        } while (!readFileHeader(true, fileToOpen));
        if (myFileHeaders.get(myFileHeaders.size() - 1).FileName
                .equals(fileToOpen)) {
            myFileIsOpened = true;
            myIndexOfFileOpened = myFileHeaders.size() - 1;
            myDecompressor = Decompressor.init(myZipFile, myFileHeaders
                    .get(myIndexOfFileOpened));
            return true;
        } else {
            return false;
        }
    }

    public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                   ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        if (!myFileIsOpened) {
            throw new RuntimeException("There is no file open for reading");
        }
        if (!myFileHeaders.get(myIndexOfFileOpened).sizeIsKnown()) {
            throw new RuntimeException("Reading of files with flag 3 is not implemented yet");
        }

	try {
	    return myDecompressor.read(b, off, len);
        } catch (WrongZipFormatException e) {
            throw new IOException("when reading occured exception " + e.getMessage());
	}
    }

    public int read() throws IOException {
        if (!myFileIsOpened) {
            throw new RuntimeException("There is no file open for reading");
        }
        if (!myFileHeaders.get(myIndexOfFileOpened).sizeIsKnown()) {
            throw new RuntimeException("Reading of files with flag 3 is not implemented yet");
        }
        try {
            return myDecompressor.read();
        } catch (WrongZipFormatException e) {
            throw new IOException("when reading occured exception "
                    + e.getMessage());
        }
    }

    public LocalFileHeader getFileHeader(int index) {
        if (index >= myFileHeaders.size()) {
            return null;
        } else {
            return myFileHeaders.get(index);
        }
    }

    public void close() throws IOException {
        myZipFile.close();
        myFileIsOpened = false;
        myIndexOfFileOpened = -1;
        myDecompressor = null;
    }
}
