/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

import java.util.Map;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import org.geometerplus.zlibrary.core.util.MimeType;
import org.geometerplus.zlibrary.text.view.ZLTextVideoElement;
import org.geometerplus.zlibrary.text.view.ZLTextVideoRegionSoul;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

import org.geometerplus.android.util.UIUtil;

class OpenVideoAction extends FBAndroidAction {
	OpenVideoAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		if (params.length != 1 || !(params[0] instanceof ZLTextVideoRegionSoul)) {
			return;
		}

		final ZLTextVideoElement element = ((ZLTextVideoRegionSoul)params[0]).VideoElement;
		final String path = element.Sources.get(MimeType.VIDEO_WEBM.toString());
		if (path != null) {
			final int port = BaseActivity.DataConnection.getPort();
			if (port == -1) {
				UIUtil.showErrorMessage(BaseActivity, "videoServiceNotWorking");
				return;
			}
			final StringBuilder url =
				new StringBuilder("http://127.0.0.1:").append(port).append("/video/");
			for (int i = 0; i < path.length(); ++i) {
				url.append(String.format("X%X", (short)path.charAt(i)));
			}
			final Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse(url.toString()), MimeType.VIDEO_WEBM.toString());
			try {
				BaseActivity.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				UIUtil.showErrorMessage(BaseActivity, "videoPlayerNotFound");
			}
		}	
	}
}
