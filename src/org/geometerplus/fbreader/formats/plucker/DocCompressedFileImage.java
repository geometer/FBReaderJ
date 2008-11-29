package org.geometerplus.fbreader.formats.plucker;

import java.io.IOException;
import java.io.InputStream;

import org.geometerplus.fbreader.formats.pdb.DocDecompressor;
import org.geometerplus.zlibrary.core.image.ZLSingleImage;
import org.geometerplus.zlibrary.core.library.ZLibrary;

public class DocCompressedFileImage extends ZLSingleImage {
	private final String myPath;
	private final int myOffset;
	private final int myCompressedSize;
	
	public DocCompressedFileImage(String mimeType, final String path, final int offset, final int compressedSize) {
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
		try {
			stream.skip(myOffset);
			byte [] targetBuffer = new byte[65535];
			final int size = DocDecompressor.decompress(stream, targetBuffer, myCompressedSize);
			if (size > 0 && size != 65535) {
				byte [] buffer = new byte[size];
				System.arraycopy(targetBuffer, 0, buffer, 0, size);
				return buffer;
			}
			return targetBuffer;
		} catch (IOException e) {}
		
		return new byte[0];
	}

}
