package org.amse.ys.zip;

/**
 * Class consists of constants, describing a compressed file. Contains only
 * construcor, all fields are final.
 */

class LocalFileHeader {
    /**
     * Initilization of constants. Implements: versions, ...
     */
    public static final int FILE_HEADER_SIGNATURE = 0x04034b50;
    public static final int FOLDER_HEADER_SIGNATURE = 0x02014b50;
    public static final int DATA_DESCRIPTOR_SIGNATURE = 0x504b0708;

    public final int VersionNeededToExtract;
    public final int GeneralPurposeFlag;
    public final int CompressionMethod;
    private int myCompressedSize; // not final!
    private int myUncompressedSize; // not final!
    public final int OffsetOfLocalData;
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

    public boolean sizeIsKnown() {
	return mySizeIsKnown;
    }

    public int getCompressedSize() {
	if (mySizeIsKnown) {
	    return myCompressedSize;
	} else {
	    throw new RuntimeException(
		    "Error in getCompressedSize: file size is not known yet");
	}
    }

    public int getUncompressedSize() {
	if (mySizeIsKnown) {
	    return myUncompressedSize;
	} else {
	    throw new RuntimeException(
		    "Error in getUncompressedSize: file size is not known yet");
	}
    }

    public void setSizes(int compressedSize, int uncompressedSize) {
	if (mySizeIsKnown) {
	    throw new RuntimeException(
		    "Was attempt to change file sizes with use of setSizes");
	}
	myCompressedSize = compressedSize;
	myUncompressedSize = uncompressedSize;
	mySizeIsKnown = true;
    }
}