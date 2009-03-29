package org.amse.ys.zip;

public class CodeBuilder {
    public static final int MAX_HUFFMAN_CODE_LENGTH = 15;

    final private int[] myLengthArray;

    public CodeBuilder(int maxNumberOfCodes) {
        myLengthArray = new int[maxNumberOfCodes];
    }

    public void addCodeLength(int codeNumber, int codeLen) {
         myLengthArray[codeNumber] = codeLen;
    }

    public void buildTable(int[] table) {
		final int arrayLength = myLengthArray.length;
		int maxCodeLength = 0;

        // counting number of codes for definite length
        int[] b1_count = new int[MAX_HUFFMAN_CODE_LENGTH + 1];
        for (int i = 0; i < arrayLength; ++i) {            
			final int l = myLengthArray[i];
            ++b1_count[l];
			if (maxCodeLength < l) {
				maxCodeLength = l;
			}
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
        for (int i = 0; i < arrayLength; ++i) {
            final int codeLength = myLengthArray[i];
            if (codeLength > 0) {
                int revertedCode = next_code[codeLength]++;
                int c = 0;
                for (int j = 0; j < codeLength; ++j) {
                    c = (c << 1) + (revertedCode & 1);
                    revertedCode >>= 1;
                }
                final int value = (codeLength << 16) + i;
				final int num = 1 << maxCodeLength;
				final int step = 1 << codeLength;
                for (; c < num; c += step) {
                    table[c] = value;
                }
            }
        }

		for (int i = maxCodeLength; i < 15; ++i) {
			System.arraycopy(table, 0, table, 1 << i, 1 << i);
		}
    }

    private void buildTable(int[] codeArray, int[] table, int maxCodeLength) {
        final int length = codeArray.length;

        for (int i = 0; i < length; i++) {
            final int codeLength = myLengthArray[i];
            if (codeLength > 0) {
                int revertedCode = codeArray[i];
                int c = 0;
                for (int j = 0; j < codeLength; ++j) {
                    c = (c << 1) + (revertedCode & 1);
                    revertedCode >>= 1;
                }
                final int value = (codeLength << 16) + i;
				final int num = 1 << maxCodeLength;
				final int step = 1 << codeLength;
                for (; c < num; c += step) {
                    table[c] = value;
                }
            }
        }

		for (int i = maxCodeLength; i < 15; ++i) {
			System.arraycopy(table, 0, table, 1 << i, 1 << i);
		}
    }

    public static void buildFixedHuffmanCodes(int[] table) {
        CodeBuilder builder = new CodeBuilder(288);
		final int[] codeArray = new int[288];
        for (int i = 0; i <= 143; i++) {
            builder.myLengthArray[i] = 8;
            codeArray[i] = 48 + i;
        }
        for (int i = 144; i <= 255; i++) {
            builder.myLengthArray[i] = 9;
            codeArray[i] = 256 + i;
        }
        for (int i = 256; i <= 279; i++) {
            builder.myLengthArray[i] = 7;
            codeArray[i] = 0 + i - 256;
        }
        for (int i = 280; i <= 287; i++) {
            builder.myLengthArray[i] = 8;
            codeArray[i] = 192 + i - 280;
        }
        builder.buildTable(codeArray, table, 9);
    }

    public static void buildFixedDistanceCodes(int[] table) {
        CodeBuilder builder = new CodeBuilder(32);
		final int[] codeArray = new int[32];
        for (int i = 0; i < 32; i++) {
            codeArray[i] = i;
            builder.myLengthArray[i] = 5;
        }
        builder.buildTable(codeArray, table, 5);
    }
}
