/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.fbreader.network;

import android.text.*;
import android.text.style.URLSpan;

public abstract class HtmlUtil {
	public static CharSequence getHtmlText(NetworkLibrary library, String text) {
		final Spanned htmlText = Html.fromHtml(text);
		if (htmlText.getSpans(0, htmlText.length(), URLSpan.class).length == 0) {
			return htmlText;
		}
		final Spannable newHtmlText = Spannable.Factory.getInstance().newSpannable(htmlText);
		for (URLSpan span : newHtmlText.getSpans(0, newHtmlText.length(), URLSpan.class)) {
			final int start = newHtmlText.getSpanStart(span);
			final int end = newHtmlText.getSpanEnd(span);
			final int flags = newHtmlText.getSpanFlags(span);
			final String url = library.rewriteUrl(span.getURL(), true);
			newHtmlText.removeSpan(span);
			newHtmlText.setSpan(new URLSpan(url), start, end, flags);
		}
		return newHtmlText;
	}
}
