package org.amse.ys.zip;

import java.io.*;
import java.util.*;

import org.geometerplus.zlibrary.core.util.InputStreamHolder;

public final class ZipFile {
	private final static Comparator<String> ourIgnoreCaseComparator = new Comparator<String>() {
		@Override
		public final int compare(String s0, String s1) {
			return s0.compareToIgnoreCase(s1);
		}
	};

	private final InputStreamHolder myStreamHolder;
	private final Map<String,LocalFileHeader> myFileHeaders =
		new TreeMap<String,LocalFileHeader>(ourIgnoreCaseComparator);

	private boolean myAllFilesAreRead;

	public ZipFile(final String fileName) {
		this(new InputStreamHolder() {
			public InputStream getInputStream() throws IOException {
				return new FileInputStream(fileName);
			}
		});
	}

	public ZipFile(final File file) {
		this(new InputStreamHolder() {
			public InputStream getInputStream() throws IOException {
				return new FileInputStream(file);
			}
		});
	}

	public ZipFile(InputStreamHolder streamHolder) {
		myStreamHolder = streamHolder;
	}

	public Collection<LocalFileHeader> headers() {
		try {
			readAllHeaders();
		} catch (IOException e) {
		}
		return myFileHeaders.values();
	}

	private boolean readFileHeader(MyBufferedInputStream baseStream, String fileToFind) throws IOException {
		LocalFileHeader header = new LocalFileHeader();
		header.readFrom(baseStream);

		if (header.Signature != LocalFileHeader.FILE_HEADER_SIGNATURE) {
			return false;
		}
		if (header.FileName != null) {
			myFileHeaders.put(header.FileName, header);
			if (header.FileName.equalsIgnoreCase(fileToFind)) {
				return true;
			}
		}
		if ((header.Flags & 0x08) == 0) {
			baseStream.skip(header.CompressedSize);
		} else {
			findAndReadDescriptor(baseStream, header);
		}
		return false;
	}

	private void readAllHeaders() throws IOException {
		if (myAllFilesAreRead) {
			return;
		}
		myAllFilesAreRead = true;

		MyBufferedInputStream baseStream = getBaseStream();
		baseStream.setPosition(0);
		myFileHeaders.clear();

		try {
			while (baseStream.available() > 0) {
				readFileHeader(baseStream, null);
			}
		} finally {
			storeBaseStream(baseStream);
		}
	}

	/**
	 * Finds descriptor of the last header and installs sizes of files
	 */
	private void findAndReadDescriptor(MyBufferedInputStream baseStream, LocalFileHeader header) throws IOException {
		final Decompressor decompressor = Decompressor.init(baseStream, header);
		int uncompressedSize = 0;
		while (true) {
			int blockSize = decompressor.read(null, 0, 2048);
			if (blockSize <= 0) {
				break;
			}
			uncompressedSize += blockSize;
		}
		header.UncompressedSize = uncompressedSize;
		Decompressor.storeDecompressor(decompressor);
	}

	private final Queue<MyBufferedInputStream> myStoredStreams = new LinkedList<MyBufferedInputStream>();

	synchronized void storeBaseStream(MyBufferedInputStream baseStream) {
		myStoredStreams.add(baseStream);
	}

	synchronized MyBufferedInputStream getBaseStream() throws IOException {
		final MyBufferedInputStream stored = myStoredStreams.poll();
		if (stored != null) {
			return stored;
		}
		return new MyBufferedInputStream(myStreamHolder);
	}

	private ZipInputStream createZipInputStream(LocalFileHeader header) throws IOException {
		return new ZipInputStream(this, header);
	}

	public boolean entryExists(String entryName) {
		try {
			return getHeader(entryName) != null;
		} catch (IOException e) {
			return false;
		}
	}

	public int getEntrySize(String entryName) throws IOException {
		return getHeader(entryName).UncompressedSize;
	}

	public InputStream getInputStream(String entryName) throws IOException {
		return createZipInputStream(getHeader(entryName));
	}

	public LocalFileHeader getHeader(String entryName) throws IOException {
		if (!myFileHeaders.isEmpty()) {
			LocalFileHeader header = myFileHeaders.get(entryName);
			if (header != null) {
				return header;
			}
			if (myAllFilesAreRead) {
				throw new ZipException("Entry " + entryName + " is not found");
			}
		}
		// ready to read file header
		MyBufferedInputStream baseStream = getBaseStream();
		baseStream.setPosition(0);
		try {
			while (baseStream.available() > 0 && !readFileHeader(baseStream, entryName)) {
			}
			final LocalFileHeader header = myFileHeaders.get(entryName);
			if (header != null) {
				return header;
			}
		} finally {
			storeBaseStream(baseStream);
		}
		throw new ZipException("Entry " + entryName + " is not found");
	}
}
