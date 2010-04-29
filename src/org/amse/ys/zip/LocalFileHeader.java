package org.amse.ys.zip;

/**
 * Class consists of constants, describing a compressed file. Contains only
 * construcor, all fields are final.
 */

public class LocalFileHeader {
    /**
     * Initilization of constants. Implements: versions, ...
     */
    static final int FILE_HEADER_SIGNATURE = 0x04034b50;
    static final int FOLDER_HEADER_SIGNATURE = 0x02014b50;
    static final int DATA_DESCRIPTOR_SIGNATURE = 0x504b0708;

    final int VersionNeededToExtract;
    final int GeneralPurposeFlag;
    final int CompressionMethod;
    private int myCompressedSize; // not final!
    private int myUncompressedSize; // not final!
    final int OffsetOfLocalData;
    public final String FileName;
    private boolean mySizeIsKnown;

    LocalFileHeader(int versionNeededToExtract, int generalPurposeFlag,
            int compressionMethod, int compressedSize, int uncompressedSize,
            int offsetOfLocalData, String fileName) {

        VersionNeededToExtract = versionNeededToExtract;
        GeneralPurposeFlag = generalPurposeFlag;
        CompressionMethod = compressionMethod;
        myCompressedSize = compressedSize;
        myUncompressedSize = uncompressedSize;
        OffsetOfLocalData = offsetOfLocalData;
        FileName = fileName;
        mySizeIsKnown = ((GeneralPurposeFlag & 8) == 0);
    }

    boolean sizeIsKnown() {
        return mySizeIsKnown;
    }

    int getCompressedSize() {
        if (mySizeIsKnown) {
            return myCompressedSize;
        } else {
            throw new RuntimeException(
                    "Error in getCompressedSize: file size is not known yet");
        }
    }

    int getUncompressedSize() {
        if (mySizeIsKnown) {
            return myUncompressedSize;
        } else {
            throw new RuntimeException(
                    "Error in getUncompressedSize: file size is not known yet");
        }
    }

    void setSizes(int compressedSize, int uncompressedSize) {
        if (!mySizeIsKnown) {
            myCompressedSize = compressedSize;
            myUncompressedSize = uncompressedSize;
            mySizeIsKnown = true;
        }
    }
}
