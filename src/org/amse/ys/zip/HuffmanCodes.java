package org.amse.ys.zip;

public final class HuffmanCodes {
    private final int[] myCode;
    private final int[] myCodeLength;
    private final int myLength;
    final int[] myTable = new int[1 << 15];

    HuffmanCodes(CodeBuilder builder) {
        myCode = builder.returnArrayOfCodes();
        myCodeLength = builder.returnArrayOfLengths();
        myLength = myCode.length;

        for (int i = 0; i < myLength; i++) {
	    final int codeLength = myCodeLength[i];
	    if (codeLength > 0) {
		int revertedCode = myCode[i];
		int code = 0;
		for (int j = 0; j < codeLength; ++j) {
		    code = (code << 1) + (revertedCode & 1);
		    revertedCode >>= 1;
		}
		final int value = (codeLength << 16) + i;
		for (int j = 0; j < (1 << (15 - codeLength)); ++j) {
		    myTable[(j << codeLength) + code] = value;
		}
	    }
        }
        //this.print();
    }

    public void print() {
        for (int i = 0; i < myLength; i++) {
            System.out.println((char)(i) + " symbol " + i + " " + this.myCode[i] + " with length "
                    + this.myCodeLength[i]);
        }
    }
    
    /**
     * requires int with at least 15 bits ready
     * returns (bitsRead<<16) + huffmanCode
     */
    
    public int readCode(int source) {
	return myTable[source & 0x7FFF];
    }
}
