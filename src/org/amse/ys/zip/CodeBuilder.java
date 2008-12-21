package org.amse.ys.zip;

public class CodeBuilder {
    public static final int MAX_HUFFMAN_CODE_LENGTH = 15;

    final private int[] myCodeArray;
    final private int[] myLengthArray;
    private int myNumberOfCodes;
    private boolean myCodesAreBuild;

    public CodeBuilder(int maxNumberOfCodes) {
	myCodeArray = new int[maxNumberOfCodes];
	myLengthArray = new int[maxNumberOfCodes];
	myNumberOfCodes = maxNumberOfCodes;
    }

    public void addCodeLength(int codeNumber, int codeLen) {
	if (!myCodesAreBuild) {
	    myLengthArray[codeNumber] = codeLen;
	} else {
	    throw new RuntimeException(
		    "Trying to addCodeLength after biulding huffman codes");
	}
    }

    private void buildCodes() {
	// counting number of codes for definite length
	int[] b1_count = new int[MAX_HUFFMAN_CODE_LENGTH + 1];
	for (int i = 0; i < myNumberOfCodes; i++) {	    
		b1_count[myLengthArray[i]]++;	    
	}
	b1_count[0] = 0;

	// step 2
	int[] next_code = new int[MAX_HUFFMAN_CODE_LENGTH + 1];
	int code = 0;
	for (int bits = 1; bits <= MAX_HUFFMAN_CODE_LENGTH; bits++) {
	    code = ((code + b1_count[bits - 1]) << 1);
	    next_code[bits] = code;
	}

	// step 3
	for (int i = 0; i < myNumberOfCodes; i++) {
	    int len = myLengthArray[i];
	    if (len != 0) {
		myCodeArray[i] = next_code[len];
		next_code[len]++;
	    }
	}
	myCodesAreBuild = true;
    }

    public int[] returnArrayOfCodes() {
	if (!myCodesAreBuild) {
	    buildCodes();
	}
	return myCodeArray;
    }

    public int[] returnArrayOfLengths() {
	return myLengthArray;
    }

    public int[] buildTable() {
	final int[] table = new int[1 << 15];

	if (!myCodesAreBuild) {
	    buildCodes();
	}
        final int length = myCodeArray.length;

        for (int i = 0; i < length; i++) {
	    final int codeLength = myLengthArray[i];
	    if (codeLength > 0) {
		int revertedCode = myCodeArray[i];
		int code = 0;
		for (int j = 0; j < codeLength; ++j) {
		    code = (code << 1) + (revertedCode & 1);
		    revertedCode >>= 1;
		}
		final int value = (codeLength << 16) + i;
		for (int j = 0; j < (1 << (15 - codeLength)); ++j) {
		    table[(j << codeLength) + code] = value;
		}
	    }
        }
	return table;
    }

    private void loadFixed() {
	for (int i = 0; i <= 143; i++) {
	    // 
	    myCodeArray[i] = 48 + i;
	    myLengthArray[i] = 8;
	}
	for (int i = 144; i <= 255; i++) {
	    myLengthArray[i] = 9;
	    myCodeArray[i] = 256 + i;
	}
	for (int i = 256; i <= 279; i++) {
	    myLengthArray[i] = 7;
	    myCodeArray[i] = 0 + i - 256;
	}
	for (int i = 280; i <= 287; i++) {
	    myCodeArray[i] = 192 + i - 280;
	    myLengthArray[i] = 8;
	}
    }

    public static CodeBuilder buildFixedHuffmanCodes() {
	CodeBuilder builder = new CodeBuilder(288);
	builder.loadFixed();
	return builder;
    }

    public static CodeBuilder buildFixedDistanceCodes() {
	CodeBuilder builder = new CodeBuilder(32);
	for (int i = 0; i < 32; i++) {
	    builder.myCodeArray[i] = i;
	    builder.myLengthArray[i] = 5;
	}
	return builder;
    }
}
