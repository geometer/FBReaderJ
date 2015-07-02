/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.zlibrary.ui.android.error;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.text.method.ScrollingMovementMethod;

import org.geometerplus.zlibrary.ui.android.R;

public class BugReportActivity extends Activity implements ErrorKeys {
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.bug_report_view);
		final StringBuilder reportText = new StringBuilder();

		reportText.append("Package:").append(getPackageName()).append("\n");
		reportText.append("Model:").append(Build.MODEL).append("\n");
		reportText.append("Device:").append(Build.DEVICE).append("\n");
		reportText.append("Product:").append(Build.PRODUCT).append("\n");
		reportText.append("Manufacturer:").append(Build.MANUFACTURER).append("\n");
		reportText.append("Version:").append(Build.VERSION.RELEASE).append("\n");
		reportText.append(getIntent().getStringExtra(STACKTRACE));

		final TextView reportTextView = (TextView)findViewById(R.id.report_text);
		reportTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
		reportTextView.setClickable(false);
		reportTextView.setLongClickable(false);

		final String versionName = new ErrorUtil(this).getVersionName();
		reportTextView.append("FBReader " + versionName + " has been crached. You can send the report to developers.\n\n");
		reportTextView.append(reportText);

		findViewById(R.id.send_report).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "exception@fbreader.org" });
				sendIntent.putExtra(Intent.EXTRA_TEXT, reportText.toString());
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, "FBReader " + versionName + " exception report");
				sendIntent.setType("message/rfc822");
				startActivity(sendIntent);
				finish();
			}
		});

		findViewById(R.id.cancel_report).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});
	}
}
