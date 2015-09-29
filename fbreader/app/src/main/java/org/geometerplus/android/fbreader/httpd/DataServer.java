/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.fbreader.httpd;

import java.io.*;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.*;
import org.geometerplus.zlibrary.core.util.MimeType;
import org.geometerplus.zlibrary.core.util.SliceInputStream;
import org.geometerplus.zlibrary.ui.android.image.ZLBitmapImage;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.book.CoverUtil;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.formats.PluginImage;

public class DataServer extends NanoHTTPD {
	private final DataService myService;

	DataServer(DataService service, int port) {
		super(port);
		myService = service;
	}

	@Override
	public Response serve(String uri, Method method, Map<String,String> headers, Map<String,String> params, Map<String,String> files) {
		if (uri.startsWith("/cover/")) {
			return serveCover(uri, method, headers, params, files);
		} else if (uri.startsWith("/video")) {
			return serveVideo(uri, method, headers, params, files);
		} else {
			return notFound(uri);
		}
	}

	private Response serveCover(String uri, Method method, Map<String,String> headers, Map<String,String> params, Map<String,String> files) {
		try {
			final ZLImage image = CoverUtil.getCover(
				DataUtil.fileFromEncodedPath(uri.substring(7)),
				PluginCollection.Instance(Paths.systemInfo(myService))
			);
			if (image instanceof ZLFileImageProxy) {
				final ZLFileImageProxy proxy = (ZLFileImageProxy)image;
				proxy.synchronize();
				final ZLStreamImage realImage = proxy.getRealImage();
				if (realImage == null) {
					return notFound(uri);
				}
				InputStream stream = realImage.inputStream();
				if (stream == null) {
					return notFound(uri);
				}
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				try {
					BitmapFactory.decodeStream(stream, null, options);
				} catch (Exception e) {
					return notFound(uri);
				}
				if (options.outWidth <= 0 || options.outHeight <= 0) {
					return notFound(uri);
				}
				stream.close();
				stream = realImage.inputStream();
				if (stream == null) {
					return notFound(uri);
				}
				final Response res =
					new Response(Response.Status.OK, MimeType.IMAGE_PNG.toString(), stream);
				res.addHeader("X-Width", String.valueOf(options.outWidth));
				res.addHeader("X-Height", String.valueOf(options.outHeight));
				return res;
			} else if (image instanceof PluginImage) {
				final PluginImage pluginImage = (PluginImage)image;
				if (pluginImage.isSynchronized()) {
					try {
						final Bitmap bitmap =
							((ZLBitmapImage)pluginImage.getRealImage()).getBitmap();
						final ByteArrayOutputStream os = new ByteArrayOutputStream();
						bitmap.compress(Bitmap.CompressFormat.JPEG, 85, os);
						final InputStream is = new ByteArrayInputStream(os.toByteArray());
						final Response res =
							new Response(Response.Status.OK, MimeType.IMAGE_JPEG.toString(), is);
						res.addHeader("X-Width", String.valueOf(bitmap.getWidth()));
						res.addHeader("X-Height", String.valueOf(bitmap.getHeight()));
						return res;
					} catch (Throwable t) {
						return noContent(uri);
					}
				} else {
					myService.ImageSynchronizer.synchronize(pluginImage, null);
					return noContent(uri);
				}
			} else {
				return notFound(uri);
			}
		} catch (Throwable t) {
			return forbidden(uri, t);
		}
	}

	private Response serveVideo(String uri, Method method, Map<String,String> headers, Map<String,String> params, Map<String,String> files) {
		String mime = null;
		for (MimeType mimeType : MimeType.TYPES_VIDEO) {
			final String m = mimeType.toString();
			if (uri.startsWith("/" + m + "/")) {
				mime = m;
				break;
			}
		}
		if (mime == null) {
			return notFound(uri);
		}
		try {
			return serveFile(DataUtil.fileFromEncodedPath(uri.substring(mime.length() + 2)), mime, headers);
		} catch (Exception e) {
			return forbidden(uri, e);
		}
	}

	private static final String BYTES_PREFIX = "bytes=";

	private Response serveFile(ZLFile file, String mime, Map<String,String> headers) throws IOException {
		final Response res;
		final InputStream baseStream = file.getInputStream();
		final int fileLength = baseStream.available();
		final String etag = '"' + Integer.toHexString(file.getPath().hashCode()) + '"';

		final String range = headers.get("range");
		if (range == null || !range.startsWith(BYTES_PREFIX)) {
			if (etag.equals(headers.get("if-none-match")))
				res = new Response(Response.Status.NOT_MODIFIED, mime, "");
			else {
				res = new Response(Response.Status.OK, mime, baseStream);
				res.addHeader("ETag", etag);
			}
		} else {
			int start = 0;
			int end = -1;
			final String bytes = range.substring(BYTES_PREFIX.length());
			final int minus = bytes.indexOf('-');
			if (minus > 0) {
				try {
					start = Integer.parseInt(bytes.substring(0, minus));
					final String endString = bytes.substring(minus + 1).trim();
					if (!"".equals(endString)) {
						end = Integer.parseInt(endString);
					}
				} catch (NumberFormatException e) {
				}
			}
			if (start >= fileLength) {
				res = new Response(
					Response.Status.RANGE_NOT_SATISFIABLE,
					MimeType.TEXT_PLAIN.toString(),
					""
				);
				res.addHeader("ETag", etag);
				res.addHeader("Content-Range", "bytes 0-0/" + fileLength);
			} else {
				if (end == -1 || end >= fileLength) {
					end = fileLength - 1;
				}
				res = new Response(
					Response.Status.PARTIAL_CONTENT,
					mime,
					new SliceInputStream(baseStream, start, end - start + 1)
				);
				res.addHeader("ETag", etag);
				res.addHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
			}
		}

		res.addHeader("Accept-Ranges", "bytes");
		return res;
	}

	private Response notFound(String uri) {
		return new Response(
			Response.Status.NOT_FOUND,
			MimeType.TEXT_HTML.toString(),
			"<html><body><h1>Not found: " + uri + "</h1></body></html>"
		);
	}

	private Response noContent(String uri) {
		return new Response(
			Response.Status.NO_CONTENT,
			MimeType.TEXT_HTML.toString(),
			"<html><body><h1>No content: " + uri + "</h1></body></html>"
		);
	}

	private Response forbidden(String uri, Throwable t) {
		t.printStackTrace();
		return new Response(
			Response.Status.FORBIDDEN,
			MimeType.TEXT_HTML.toString(),
			"<html><body><h1>" + t.getMessage() + "</h1>\n(" + uri + ")</body></html>"
		);
	}
}
