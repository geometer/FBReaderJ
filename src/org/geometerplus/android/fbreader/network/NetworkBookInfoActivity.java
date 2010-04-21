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

package org.geometerplus.android.fbreader.network;

import java.util.Set;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.net.Uri;
import android.content.Intent;
import android.graphics.Bitmap;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.image.ZLImage;

import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;

import org.geometerplus.fbreader.network.*;


public class NetworkBookInfoActivity extends Activity implements NetworkLibraryActivity.EventListener {

	private NetworkBookItem myBook;

	private final ZLResource myResource = ZLResource.resource("networkBookView");

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		myBook = NetworkLibraryActivity.Instance.getBookItem();
		if (myBook == null) {
			finish();
			return;
		}

		setTitle(myBook.Title);
		setContentView(R.layout.network_book);

		setupDescription();
		setupCover();
		setupButtons();
	}

	private final void setupDescription() {
		final TextView descriptionViewTitle = (TextView) findViewById(R.id.network_book_description_title);
		descriptionViewTitle.setText(myResource.getResource("description").getValue());

		final TextView descriptionView = (TextView) findViewById(R.id.network_book_description);
		final String description;
		if (myBook.Summary != null) {
			description = myBook.Summary;
		} else {
			description = myResource.getResource("noDescription").getValue();
		}
		descriptionView.setText(description);
	}

	private final void setupCover() {
		final View rootView = findViewById(R.id.network_book_root);
		final ImageView coverView = (ImageView) findViewById(R.id.network_book_cover);

		final int maxHeight = 300; // FIXME: hardcoded constant
		final int maxWidth = maxHeight * 2 / 3;
		Bitmap coverBitmap = null;
		final ZLImage cover = NetworkTree.createCover(myBook);
		if (cover != null) {
			ZLAndroidImageData data = null;
			final ZLAndroidImageManager mgr = (ZLAndroidImageManager) ZLAndroidImageManager.Instance();
			if (cover instanceof NetworkImage) {
				final NetworkImage img = (NetworkImage) cover;
				if (NetworkLibraryActivity.Instance.isCoverLoading(img.Url)) {
					NetworkLibraryActivity.Instance.setOnCoverSyncRunnable(img.Url, new Runnable() {
						public void run() {
							img.synchronizeFast();
							final ZLAndroidImageData data = mgr.getImageData(img);
							if (data != null) {
								final Bitmap coverBitmap = data.getBitmap(maxWidth, maxHeight);
								if (coverBitmap != null) {
									coverView.setImageBitmap(coverBitmap);
									coverView.setVisibility(View.VISIBLE);
									rootView.invalidate();
									rootView.requestLayout();
								}
							}
						}
					});
				} else {
					img.synchronizeFast();
					data = mgr.getImageData(img);
				}
			} else {
				data = mgr.getImageData(cover);
			}
			if (data != null) {
				coverBitmap = data.getBitmap(maxWidth, maxHeight);
			}
		}
		if (coverBitmap != null) {
			coverView.setImageBitmap(coverBitmap);
			coverView.setVisibility(View.VISIBLE);
		} else {
			coverView.setVisibility(View.GONE);
		}
	}

	private final void setupButtons() {
		final int buttons[] = new int[] {
			R.id.network_book_button0,
			R.id.network_book_button1,
			R.id.network_book_button2,
		};
		int buttonNumber = 0;
		Set<NetworkBookActions.Action> actions = NetworkBookActions.getContextMenuActions(myBook);
		for (final NetworkBookActions.Action a: actions) {
			if (buttonNumber >= buttons.length) {
				break;
			}

			ZLResource resource = ZLResource.resource("networkView");
			final String text;
			if (a.Arg == null) {
				text = resource.getResource(a.Key).getValue();
			} else {
				text = resource.getResource(a.Key).getValue().replace("%s", a.Arg);
			}

			final int buttonId = buttons[buttonNumber++];
			Button button = (Button) findViewById(buttonId);
			button.setText(text);
			button.setVisibility(View.VISIBLE);
			button.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					NetworkBookActions.runAction(myBook, a.Id);
					NetworkBookInfoActivity.this.updateView();
				}
			});
			button.setEnabled(a.Id != NetworkTreeActions.TREE_NO_ACTION);
		}
		boolean showSpacers = buttonNumber == 1;
		findViewById(R.id.network_book_left_spacer).setVisibility(showSpacers ? View.VISIBLE : View.GONE);
		findViewById(R.id.network_book_right_spacer).setVisibility(showSpacers ? View.VISIBLE : View.GONE);
		while (buttonNumber < buttons.length) {
			final int buttonId = buttons[buttonNumber++];
			View button = findViewById(buttonId);
			button.setVisibility(View.GONE);
			button.setOnClickListener(null);
		}
	}

	private void updateView() {
		setupButtons();
		final View rootView = findViewById(R.id.network_book_root);
		rootView.invalidate();
		rootView.requestLayout();
	}

	protected void onStart() {
		super.onStart();
		if (NetworkLibraryActivity.Instance != null) {
			NetworkLibraryActivity.Instance.setTopLevelActivity(this);
			NetworkLibraryActivity.Instance.addEventListener(this);
		}
	}

	protected void onStop() {
		if (NetworkLibraryActivity.Instance != null) {
			NetworkLibraryActivity.Instance.setTopLevelActivity(null);
			NetworkLibraryActivity.Instance.removeEventListener(this);
		}
		super.onStop();
	}

	public void onModelChanged() {
		updateView();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case NetworkLibraryActivity.DIALOG_AUTHENTICATION:
			dialog = AuthenticationDialog.Instance().createDialog(this);
			break;
		}
		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case NetworkLibraryActivity.DIALOG_AUTHENTICATION:
			AuthenticationDialog.Instance().prepareDialog(dialog);
			break;
		}		
	}
}
