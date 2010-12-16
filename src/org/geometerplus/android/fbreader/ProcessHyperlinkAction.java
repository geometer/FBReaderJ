/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlink;

import org.geometerplus.fbreader.fbreader.FBAction;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.bookmodel.FBHyperlinkType;
import org.geometerplus.fbreader.network.NetworkLibrary;

import org.geometerplus.android.fbreader.network.BookDownloader;
import org.geometerplus.android.fbreader.network.BookDownloaderService;

class ProcessHyperlinkAction extends FBAction {
	private final FBReader myBaseActivity;

	ProcessHyperlinkAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(fbreader);
		myBaseActivity = baseActivity;
	}

	public boolean isEnabled() {
		final ZLTextView view = Reader.getTextView();
		return
			view.getSelectedText() != null ||
			view.getCurrentHyperlink() != null;
	}

	public void run() {
		final ZLTextHyperlink hyperlink = Reader.getTextView().getCurrentHyperlink();
		if (hyperlink != null) {
			switch (hyperlink.Type) {
				case FBHyperlinkType.EXTERNAL:
					openInBrowser(hyperlink.Id);
					break;
				case FBHyperlinkType.INTERNAL:
					Reader.tryOpenFootnote(hyperlink.Id);
					break;
			}
			return;
		}

		final String text = Reader.getTextView().getSelectedText();
		if (text != null) {
			int start = 0;
			int end = text.length();
			for (; start < end && !Character.isLetterOrDigit(text.charAt(start)); ++start);
			for (; start < end && !Character.isLetterOrDigit(text.charAt(end - 1)); --end);
			if (start == end) {
				return;
			}
			Intent intent = new Intent(Intent.ACTION_SEARCH);
			intent.setComponent(new ComponentName(
				"com.socialnmobile.colordict",
				"com.socialnmobile.colordict.activity.Main"
			));
			intent.putExtra(SearchManager.QUERY, text.substring(start, end));
			try {
				myBaseActivity.startActivity(intent);
			} catch(ActivityNotFoundException e){
				Toast.makeText(
					myBaseActivity,
					ZLResource.resource("errorMessage").getResource("dictNotInstalled").getValue(),
					Toast.LENGTH_LONG
				).show();
			}
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
		myBaseActivity.startActivity(intent);
	}
}
