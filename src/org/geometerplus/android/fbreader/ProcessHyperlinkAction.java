/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader;

import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.net.Uri;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.zlibrary.text.view.*;

import org.geometerplus.fbreader.fbreader.FBAction;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.bookmodel.FBHyperlinkType;
import org.geometerplus.fbreader.network.NetworkLibrary;

import org.geometerplus.android.fbreader.network.BookDownloader;
import org.geometerplus.android.fbreader.network.BookDownloaderService;
import org.geometerplus.android.fbreader.image.ImageViewActivity;

class ProcessHyperlinkAction extends FBAction {
	private final FBReader myBaseActivity;

	ProcessHyperlinkAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(fbreader);
		myBaseActivity = baseActivity;
	}

	public boolean isEnabled() {
		return Reader.getTextView().getSelectedRegion() != null;
	}

	public void run() {
		final ZLTextElementRegion region = Reader.getTextView().getSelectedRegion();
		if (region instanceof ZLTextHyperlinkRegion) {
			final ZLTextHyperlink hyperlink = ((ZLTextHyperlinkRegion)region).Hyperlink;
			switch (hyperlink.Type) {
				case FBHyperlinkType.EXTERNAL:
					openInBrowser(hyperlink.Id);
					break;
				case FBHyperlinkType.INTERNAL:
					Reader.tryOpenFootnote(hyperlink.Id);
					break;
			}
			return;
		} else if (region instanceof ZLTextImageRegion) {
			final String uriString = ((ZLTextImageRegion)region).ImageElement.URI;
			if (uriString != null) {
				try {
					final Intent intent = new Intent();
					intent.setClass(myBaseActivity, ImageViewActivity.class);
					intent.setData(Uri.parse(uriString));
					myBaseActivity.startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (region instanceof ZLTextWordRegion) {
			DictionaryUtil.openWordInDictionary(
				myBaseActivity, (ZLTextWordRegion)region
			);
		}
	}

	private void openInBrowser(String urlString) {
		final Intent intent = new Intent(Intent.ACTION_VIEW);
		boolean externalUrl = true;
		if (BookDownloader.acceptsUri(Uri.parse(urlString))) {
			intent.setClass(myBaseActivity, BookDownloader.class);
			intent.putExtra(BookDownloaderService.SHOW_NOTIFICATIONS_KEY, BookDownloaderService.Notifications.ALL);
			externalUrl = false;
		}
		final NetworkLibrary nLibrary = NetworkLibrary.Instance();
		try {
			nLibrary.initialize();
		} catch (ZLNetworkException e) {
		}
		intent.setData(Uri.parse(NetworkLibrary.Instance().rewriteUrl(urlString, externalUrl)));
		try {
			myBaseActivity.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			// TODO: show an error message
		}
	}
}
