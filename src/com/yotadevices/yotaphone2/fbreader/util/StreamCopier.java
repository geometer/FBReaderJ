package com.yotadevices.yotaphone2.fbreader.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class StreamCopier
{
	private static class StreamWriter {
		private byte[] buffer;

		public StreamWriter(int buffersize) {
			buffer = new byte[buffersize];
		}

		public int write(InputStream in, OutputStream out) throws IOException {
			Arrays.fill(buffer, (byte) 0);
			int readed = in.read(buffer);
			if (readed > 0) {
				out.write(buffer, 0, readed);
			}
			return readed;
		}

		@SuppressWarnings("unused")
		@Deprecated
		private StreamWriter() {}
	}

	public static void copy(InputStream in, OutputStream out) throws IOException
	{
		copy(in, out, 1024);
	}
	public static void copy(InputStream in, OutputStream out, final int bufsize) throws IOException
	{
		StreamWriter writer = new StreamWriter(bufsize);
		while(writer.write(in, out) >= 0) {}
		out.flush();
	}
}
