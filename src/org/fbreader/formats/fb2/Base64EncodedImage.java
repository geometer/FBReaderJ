package org.fbreader.formats.fb2;

import java.util.ArrayList;

import org.zlibrary.core.image.ZLImage;

class Base64EncodedImage implements ZLImage {
	private ArrayList<char[]> myEncodedData;
	private byte [] myData;
	
	public Base64EncodedImage(String contentType) {
		// TODO: use contentType
	}

	private void decode() {
		if ((myEncodedData == null) || (myData != null)) {
			return;
		}

		int dataLength = 0;
		for (char[] part : myEncodedData) {
			dataLength += part.length;
		}
		char[] encodedData = new char[dataLength];
		{
			int pos = 0;
			for (char[] part : myEncodedData) {
				System.arraycopy(part, 0, encodedData, pos, part.length);
				pos += part.length;
			}
		}
		myEncodedData = null;
		
		int newLength = dataLength / 4 * 3;
		myData = new byte[newLength];
		byte [] number = new byte[4];
		for (int pos = 0, dataPos = 0; pos < dataLength; dataPos += 3) {
			for (int i = 0; (i < 4) && (pos < dataLength); ++pos) {
				char encodedByte = encodedData[pos];
				number[i] = 0;
				if (('A' <= encodedByte) && (encodedByte <= 'Z')) {
					number[i] = (byte) (encodedByte - 'A');
				} else if (('a' <= encodedByte) && (encodedByte <= 'z')) {
					number[i]= (byte) (encodedByte - 'a' + 26);
				} else if (('0' <= encodedByte) && (encodedByte <= '9')) {
					number[i] = (byte) (encodedByte - '0' + 52);
				} else if (encodedByte == '+') {
					number[i] = 62;
				} else if (encodedByte == '/') {
					number[i] = 63;
				} else if (encodedByte == '=') {
					number[i] = 64;
				} else {
					continue;
				}
				++i;
			}
			myData[dataPos] = (byte) (number[0] <<2 | number[1] >>4 );
			myData[dataPos + 1] = (byte) (((number[1] & 0xf)<<4 ) |( (number[2] >>2) & 0xf) );
			myData[dataPos + 2] = (byte) ( number[2] <<6 | number[3]);
		}
/*		if (number[2] == 64) {
			byte [] tmp = new byte[newLength - 2];
			System.arraycopy(myData, 0, tmp, 0, newLength - 2);
			myData = tmp;
		} else if (number[3] == 64) {
			byte [] tmp = new byte[newLength - 1];
			System.arraycopy(myData, 0, tmp, 0, newLength - 1);
			myData = tmp;
		}
			
*/			
	}
	
	public byte [] byteData() {
		decode();
		return myData;
	}
	
	void addData(ArrayList<char[]> data) {
		myEncodedData = new ArrayList<char[]>(data);
	}
}
