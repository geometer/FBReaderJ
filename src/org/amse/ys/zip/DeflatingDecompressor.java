package org.amse.ys.zip;

import java.io.*;

public class DeflatingDecompressor extends AbstractDeflatingDecompressor {
    private static final int ST_HEADER = 1;
    private static final int ST_NO_COMPRESSION = 2;
    private static final int ST_FIXED_CODES = 3;
    private static final int ST_DYNAMIC_CODES = 4;
    private static final int ST_END_OF_FILE = 5;

    // common variables
    private MyBufferedInputStream myStream;
    private LocalFileHeader myHeader;
    private int myState;
    private int myTotalLength;
    private int myBytesRead;
    private int myCurrentPosition;
    private boolean myTheBlockIsFinal;

    // for bit reader
    private int myBitsInBuffer;
    private int myTempInt; // should contain 16 bit available for reading

    // output buffer
    private final CircularBuffer myOutputBuffer = new CircularBuffer();

    // for no compression method
    private int myCurrentBlockLength;
    private int myReadInBlock;

    // for Huffman codes
    private final int[] myHuffmanCodes = new int[1 << 15];
    private final int[] myDistanceCodes = new int[1 << 15];
    private final int[] myAuxCodes = new int[1 << 15];

    public DeflatingDecompressor(MyBufferedInputStream inputStream, LocalFileHeader header) {
        super();
        reset(inputStream, header);
    }

    void reset(MyBufferedInputStream inputStream, LocalFileHeader header) {
        myStream = inputStream;
        myHeader = header;
        myTotalLength = header.getCompressedSize();
        myBytesRead = 0;
        myCurrentPosition = 0;
        myTheBlockIsFinal = false;
        myState = ST_HEADER;
        myOutputBuffer.reset();
        myBitsInBuffer = 0;
        myTempInt = 0;
        myCurrentBlockLength = 0;
        myReadInBlock = 0;
    }

	@Override
    public int available() {
        return myHeader.getUncompressedSize() - myCurrentPosition;
    }
    
    private void ensure16BitsInBuffer() throws IOException {
        do {
            int tmp = myStream.read();
            if (tmp < 0) {
                throw new ZipException("getBit: read after end of file");
            }
            myTempInt += tmp << myBitsInBuffer;
            myBitsInBuffer += 8;
            myBytesRead++;
        } while (myBitsInBuffer <= 16);
    }

    /**
     * 
     * This code is potentially dangerous, because can read 3 bytes after end of block
     * 
     */
    private int getBit() throws IOException {
        if (myBitsInBuffer < 16) {
            ensure16BitsInBuffer();
        }
        
        myBitsInBuffer--;
        int result = (myTempInt & 1);
        myTempInt = (myTempInt >> 1);
        return result;
    }

    private int readIntegerByBit(int length) throws IOException {
        if (myBitsInBuffer < 16) {
            ensure16BitsInBuffer();
        }
        final int result = myTempInt & ((1 << length) - 1);
        myTempInt >>>= length;
        myBitsInBuffer -= length;
        return result;
    }

    private static final int MAX_LEN = CircularBuffer.DICTIONARY_LENGTH / 2;

	@Override
    public int read(byte b[], int off, int len) throws IOException {
        int i = 0;
        int available = myOutputBuffer.available();
        while (i < len) {
            int toRead = Math.min(MAX_LEN, len - i);
            while (available < toRead) {
                if (myState == ST_END_OF_FILE) {
                    break;
                }
                if (myState == ST_HEADER) {
                    readHeader();
                }            
                available += pushNextSymbolToDictionary();            
            }
            if (available == 0) {
                break;
            }
            if (toRead > available) {
                toRead = available;
            }
            myOutputBuffer.read(b, off + i, toRead);
            i += toRead;
            available -= toRead;
        }
        myCurrentPosition += i;
        return (i > 0) ? i : -1;
    }

	@Override
    public int read() throws IOException {
        myCurrentPosition++;
        
        while (myOutputBuffer.available() == 0) {
            if (myState == ST_HEADER) {
                readHeader();
            }            
            pushNextSymbolToDictionary();            
            if (myState == ST_END_OF_FILE) {
                return -1;
            }
        }
        return myOutputBuffer.read() & 0xFF;
    }

    private int pushNextSymbolToDictionary() throws IOException {
        if (myState == ST_NO_COMPRESSION) {
            myOutputBuffer.writeByte((byte)readIntegerByBit(8));
            myReadInBlock++;
            if (myCurrentBlockLength == myReadInBlock) {
                if (myTheBlockIsFinal) {
                    myState = ST_END_OF_FILE;
                } else {
                    myState = ST_HEADER;
                }
            }
            return 1;
        } else {
            int currentHuffmanCode = readHuffmanCode(myHuffmanCodes);
            int length;
            switch (currentHuffmanCode) {
                default:
                    myOutputBuffer.writeByte((byte)currentHuffmanCode);
                    return 1;
                case 256:  
                    myState = myTheBlockIsFinal ? ST_END_OF_FILE : ST_HEADER;
                    return 0;
                case 257:  
                case 258:  
                case 259:  
                case 260:  
                case 261:  
                case 262:  
                case 263:  
                case 264:  
                    length = currentHuffmanCode + 3 - 257;
                    break;
                case 265:  
                case 266:  
                case 267:  
                case 268:  
                    length = ((currentHuffmanCode - 265) << 1) + 11 + getBit();
                    break;
                case 269:  
                case 270:  
                case 271:  
                case 272:  
                    length = ((currentHuffmanCode - 269) << 2) + 19 + readIntegerByBit(2);
                    break;
                case 273:  
                case 274:  
                case 275:  
                case 276:  
                    length = ((currentHuffmanCode - 273) << 3) + 35 + readIntegerByBit(3);
                    break;
                case 277:  
                case 278:  
                case 279:  
                case 280:  
                    length = ((currentHuffmanCode - 277) << 4) + 67 + readIntegerByBit(4);
                    break;
                case 281:  
                case 282:  
                case 283:  
                case 284:  
                    length = ((currentHuffmanCode - 281) << 5) + 131 + readIntegerByBit(5);
                    break;
                case 285:  
                    length = 258;
                    break;
            }

            // reading distanse
            final int huffmanCode = readHuffmanCode(myDistanceCodes);
            final int distance;
            if (huffmanCode <= 3) {
                distance = huffmanCode + 1;
                
            } else if (huffmanCode <= 29) {
                final int extraBits = (huffmanCode / 2) - 1;
                int previousCode = (1 << (huffmanCode / 2));
                if ((huffmanCode % 2) != 0) {
                    previousCode += (1 << extraBits);
                }
                distance = previousCode + 1 + readIntegerByBit(extraBits);
            } else {
                throw new RuntimeException("distance code > 29 found");
            }
            myOutputBuffer.repeat(length, distance);
            return length;
        }
    }

    private int readHuffmanCode(int[] table) throws IOException {
        int bitsInBuffer = myBitsInBuffer;
        int buffer = myTempInt;

        while (bitsInBuffer <= 16) {
            buffer += myStream.read() << bitsInBuffer;
            bitsInBuffer += 8;
            myBytesRead++;
        }

        final int tmp = table[buffer & 0x7FFF];

        final int len = tmp >> 16;
        myTempInt = buffer >>> len;
        myBitsInBuffer = bitsInBuffer - len;

        return tmp & 0x0FFFF;
    }

    private void readHeader() throws IOException {
        if ((myState != ST_HEADER) || (myBytesRead >= myTotalLength)) {
            throw new RuntimeException("unexpected case of readheader call");
        }
        myTheBlockIsFinal = (getBit() != 0);
        switch (readIntegerByBit(2)) {
        case 0:
            myState = ST_NO_COMPRESSION;
            readIntegerByBit(myBitsInBuffer % 8);            
            myCurrentBlockLength = readIntegerByBit(16);
            readIntegerByBit(16);
            myReadInBlock = 0;
            break;
        case 1:
            myState = ST_FIXED_CODES;
            CodeBuilder.buildFixedHuffmanCodes(myHuffmanCodes);
            CodeBuilder.buildFixedDistanceCodes(myDistanceCodes);
            break;
        case 2:
            myState = ST_DYNAMIC_CODES;
            readCodes();
            break;
        case 3:
            throw new ZipException(
                    "Code 11 found in header of delflated block. (means error according to specification)");
        }
        
        //myHuffmanCodes.print();
    }

    private void readCodes() throws IOException {
        //int headersFound = 0;

        final int[] codeLenSequence = { 16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11,
                4, 12, 3, 13, 2, 14, 1, 15 };

        final int numberOfLiteralCodes = readIntegerByBit(5);
        final int numberOfDistanceCodes = readIntegerByBit(5);
        final int numberOfLengthCodes = readIntegerByBit(4);

        // reading HCLEN codes
        final CodeBuilder headerReadingCoder = new CodeBuilder(19);
        
        for (int i = 0; i < (numberOfLengthCodes + 4); i++) {
            headerReadingCoder.addCodeLength(codeLenSequence[i],
                    readIntegerByBit(3));
        }

        headerReadingCoder.buildTable(myAuxCodes);
        
        CodeBuilder usualCodeBuilder = new CodeBuilder(288);
        int previousNumber = 0;
        for (int i = 0; i < (numberOfLiteralCodes + 257); i++) {
            int currentHuffmanCode = readHuffmanCode(myAuxCodes);
            //headersFound++;
            if (currentHuffmanCode <= 15) {
                usualCodeBuilder.addCodeLength(i, currentHuffmanCode);
                previousNumber = currentHuffmanCode;
            } else {
                // repeating previous codes
                boolean previous;
                int repeatNumber = 0;
                switch (currentHuffmanCode) {
                    case 16:
                        repeatNumber = 3 + readIntegerByBit(2);
                        previous = true;
                        break;
                    case 17:
                        repeatNumber = 3 + readIntegerByBit(3);
                        previous = false;
                        break;
                    case 18:
                        repeatNumber = 11 + readIntegerByBit(7);
                        previous = false;
                        break;
                    default:
                        throw new RuntimeException("error when reading dynamic Huffman codes");
                }
                previousNumber = previous ? previousNumber : 0;
                for (int j = 0; j < repeatNumber; j++) {
                    usualCodeBuilder.addCodeLength(i + j, previousNumber);
                }
                i += repeatNumber - 1;
            }
        }
        // we can build huffman codes for charset
        usualCodeBuilder.buildTable(myHuffmanCodes);

        // building distance codes
        CodeBuilder distanceCodeBuilder = new CodeBuilder(32);
        previousNumber = 0;
        for (int i = 0; i < (numberOfDistanceCodes + 1); i++) {
            int currentHuffmanCode = readHuffmanCode(myAuxCodes);
            //headersFound++;
            if (currentHuffmanCode <= 15) {
                distanceCodeBuilder.addCodeLength(i, currentHuffmanCode);
                previousNumber = currentHuffmanCode;
            } else {
                // repeating previous codes
                boolean previous;
                int repeatNumber = 0;
                switch (currentHuffmanCode) {
                case 16:
                    repeatNumber = 3 + readIntegerByBit(2);
                    previous = true;
                    break;
                case 17:
                    repeatNumber = 3 + readIntegerByBit(3);
                    previous = false;
                    break;
                case 18:
                    repeatNumber = 11 + readIntegerByBit(7);
                    previous = false;
                    break;
                default:
                    throw new RuntimeException("error when reading dynamic Huffman codes");
                }
                previousNumber = (previous ? previousNumber : 0);
                for (int j = 0; j < repeatNumber; j++) {
                    distanceCodeBuilder.addCodeLength(i + j, previousNumber);
                }
                i += (repeatNumber - 1);
            }
        }
        distanceCodeBuilder.buildTable(myDistanceCodes);
    }
}
