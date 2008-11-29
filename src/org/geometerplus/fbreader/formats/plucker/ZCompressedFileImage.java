package org.geometerplus.fbreader.formats.plucker;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.geometerplus.zlibrary.core.image.ZLSingleImage;
import org.geometerplus.zlibrary.core.library.ZLibrary;

public class ZCompressedFileImage extends ZLSingleImage {
	private final String myPath;
	private final int myOffset;
	private final int myCompressedSize;
	
	public ZCompressedFileImage(String mimeType, final String path, final int offset, final int compressedSize) {
		super(mimeType);
		myPath = path;
		myOffset = offset;
		myCompressedSize = compressedSize;
	}

	public byte[] byteData() {
final InputStream stream = ZLibrary.Instance().getInputStream(myPath);
		
		if (stream == null) {
			return new byte[0];
		}
		
		final ArrayList data = new ArrayList();
		byte[] buffer;
		int sizeOfBufferData;
		try {
			stream.skip(myOffset);
			byte [] targetBuffer = new byte[myCompressedSize];
			stream.read(targetBuffer, 0, myCompressedSize);
			Inflater decompressor = new Inflater();
			decompressor.setInput(targetBuffer, 0, myCompressedSize);
			do {
				buffer = new byte[4096];
				sizeOfBufferData = decompressor.inflate(buffer);
				data.add(buffer);
			} while (sizeOfBufferData == 4096);
			decompressor.end();
			final int dataSizeMinusOne = data.size() - 1;
			buffer = new byte[dataSizeMinusOne * 4096 + sizeOfBufferData];
			for (int i = 0; i < dataSizeMinusOne; ++i) {
				System.arraycopy(data.get(i), 0, buffer, i * 4096, 4096);
			}
			System.arraycopy(data.get(dataSizeMinusOne), 0, buffer, dataSizeMinusOne * 4096, sizeOfBufferData);
			return buffer;
		} catch (IOException e) {
		} catch (DataFormatException e) {
		}
		
		return new byte[0];
	}

}
