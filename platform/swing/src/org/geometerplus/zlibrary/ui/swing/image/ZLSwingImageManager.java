/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.ui.swing.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLImageManager;
import org.geometerplus.zlibrary.core.image.ZLSingleImage;

public final class ZLSwingImageManager extends ZLImageManager {
	public ZLSwingImageData getImageData(ZLImage image) {
		if (image instanceof ZLSingleImage) {
			if ("image/palm".equals(((ZLSingleImage) image).mimeType())) {
				return new ZLSwingImageData(
						convertFromPalmImageFormat(((ZLSingleImage) image).byteData()));
			}
			try {
				final BufferedImage awtImage = ImageIO.read(new ByteArrayInputStream(((ZLSingleImage) image).byteData()));
				return new ZLSwingImageData(awtImage);
			} catch (IOException e) {
				return null;
			}
		} else {
			//TODO
			return null;
		}
	}
	
	private static BufferedImage convertFromPalmImageFormat(byte [] byteData) {
		if (byteData.length >= 16) {
			PalmImageHeader header = new PalmImageHeader(byteData);
			switch (header.CompressionType) {
				case 0x00: // scanline
					//std::cerr << "scanline encoded images are not supported yet\n";
					break;
				case 0x01: // rle
					//std::cerr << "rle encoded images are not supported yet\n";
					break;
				case 0x02: //packbits
					//std::cerr << "packbits encoded images are not supported yet\n";
					break;
				case (byte) 0xFF: // none
					if (byteData.length >= header.BytesPerRow * header.Height + 16) {
						if ((header.BitsPerPixel != 1) &&
								(header.BitsPerPixel != 2) &&
								(header.BitsPerPixel != 4) &&
								(header.BitsPerPixel != 8) &&
								(header.BitsPerPixel != 16)) {
							//std::cerr << "images with bpp = " << (int)header.BitsPerPixel << " are not supported\n";
							break;
						}
						
						BufferedImage image =  new BufferedImage(header.Width, header.Height, BufferedImage.TYPE_INT_RGB);
						
						if (header.BitsPerPixel == 16) {
							final byte redBits = byteData[16];
							final byte greenBits = byteData[17];
							final byte blueBits = byteData[18];
							final int redMask = (1 << redBits) - 1;
							final int greenMask = (1 << greenBits) - 1;
							final int blueMask = (1 << blueBits) - 1;	
						
							for (int i = 0; i < header.Height; ++i) {
								int from_ptr = 24;
								final int to_ptr = from_ptr + header.BytesPerRow;
								int j = 0;
								for (; from_ptr < to_ptr; from_ptr += 2) {
									int color = 256 * byteData[from_ptr] + byteData[from_ptr + 1];
									setPixel(image, j++, i, (color >> (16 - redBits)) * 255 / redMask,
											((color >> blueBits) & greenMask) * 255 / greenMask,
											(color & blueMask) * 255 / blueMask
									);
								}
							}
						} else {
							final byte from = 16;
							for (int i = 0; i < header.Height; ++i) {
								int from_ptr = from + header.BytesPerRow * i;
								int x = 0;
								for (int j = 0; j < (int)header.Width; j += 8 / header.BitsPerPixel, ++from_ptr) {
									switch (header.BitsPerPixel) {
										case 1:
											{
												byte len = (byte) Math.min(8, (int)header.Width - j);
												for (byte k = 0; k < len; ++k) {
													setGrayPixel(image, x++, i, (((byteData[from_ptr]) & (128 >> k)) != 0) ? 0 : 255);
												}
											}
											break;
										case 2:
											{
												byte len = (byte) Math.min(4, (int)header.Width - j);
												for (byte k = 0; k < len; ++k) {
													setGrayPixel(image, x++, i, 85 * (3 - (byteData[from_ptr] >> (6 - 2 * k)) & 0x3));
												}
											}
											break;
										case 4:
											{
												setGrayPixel(image, x++, i, 17 * (15 - (byteData[from_ptr] >> 4)));
												if (j != (int)header.Width - 1) {
													setGrayPixel(image, x++, i, 17 * (15 - (byteData[from_ptr] & 0xF)));
												}
											}
											break;
										case 8:
											{
												final int [] col = PalmImage8bitColormap[byteData[from_ptr] & 0xFF];
												setPixel(image, x++, i, col[0], col[1], col[2]);
											}
											break;
									}
								}
							}
						}
				//		System.out.println("Bits per pix " + header.BitsPerPixel);
						return image;
					}
					break;
				default: // unknown
					//std::cerr << "unknown image encoding: " << (int)header.CompressionType << "\n";
					break;
			}
		}
		
		return null;
	}
	
	private static void setPixel(BufferedImage image, int x, int y, int red, int green, int blue) {
		image.setRGB(x, y, new Color(red, green, blue).getRGB());
	}
	
	private static void setGrayPixel(BufferedImage image, int x, int y, int color) {
		setPixel(image, x, y, color, color, color);
	}
}
