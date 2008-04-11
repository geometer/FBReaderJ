package org.geometerplus.fbreader.formats.pdb;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class PdbUtil {
	public static int readUnsignedShort(InputStream stream) {
		byte []tmp = new byte[2];
		try {
			stream.read(tmp, 0, 2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tmp[1] + tmp[0] * 256;
		
	}
	public static long readUnsignedLong(InputStream stream) {
		byte []readBuffer = new byte[8];
		try {
			stream.read(readBuffer, 0, 8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return (((long)readBuffer[0] << 56) +
                ((long)(readBuffer[1] & 255) << 48) +
          ((long)(readBuffer[2] & 255) << 40) +
                ((long)(readBuffer[3] & 255) << 32) +
                ((long)(readBuffer[4] & 255) << 24) +
                ((readBuffer[5] & 255) << 16) +
                ((readBuffer[6] & 255) <<  8) +
                ((readBuffer[7] & 255) <<  0));
		
		/*byte []tmp = new byte[4];
		try {
			stream.read(tmp, 0, 4);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tmp[3] + tmp[2]*256 +tmp[1] * 256^2 +tmp[0] * 256^3;*/
	}
		
	public static class PdbHeader {
		static public String DocName;
		static public short Flags;
		static public String Id;
		static public ArrayList/*<Integer>*/ Offsets;

		public boolean read(InputStream stream) throws IOException {
			int startOffset = ((PdbStream)stream).offset();

			DocName = "";
			DocName += "32'\0'";
			stream.read(DocName.getBytes(), 0, 32);
			Flags = (short)stream.read();

			stream.skip(26);
			
			Id = "";
			Id+= 8+'\0';
			stream.read(Id.getBytes(), 0, 8);

			stream.skip(8);

			Offsets.clear();
			short numRecords = (short)stream.read();
			Offsets.ensureCapacity(numRecords);

			for (int i = 0; i < numRecords; ++i) {
				long recordOffset = stream.read();
				Offsets.add(recordOffset);
				stream.skip(4);
			}

			return ((PdbStream)stream).offset() == startOffset + 78 + 8 * numRecords;
		}
	};

}
