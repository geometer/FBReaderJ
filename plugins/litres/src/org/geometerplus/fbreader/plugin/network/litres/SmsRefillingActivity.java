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

package org.geometerplus.fbreader.plugin.network.litres;

import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.telephony.TelephonyManager;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.network.*;
import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

public class SmsRefillingActivity extends Activity {
	private ZLResource myResource;

	private Button findButton(int resourceId) {
		return (Button)findViewById(resourceId);
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		System.err.println("smsDialog");

		ZLResourceFile.init(getApplicationContext());
		myResource = ZLResource.resource("smsDialog");

		setContentView(R.layout.sms_dialog);
		setTitle(myResource.getResource("title").getValue());

		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");

		final Button okButton = findButton(R.id.sms_ok_button);
		okButton.setText(buttonResource.getResource("ok").getValue());
		okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			}
		});
		final Button cancelButton = findButton(R.id.sms_cancel_button);

		cancelButton.setText(buttonResource.getResource("cancel").getValue());
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();

		final ListView listView = (ListView)findViewById(R.id.sms_list);
		// TODO: add list items
		final TelephonyManager manager = (TelephonyManager)getSystemService(Activity.TELEPHONY_SERVICE);
		if (manager != null) {
			final String operator = manager.getNetworkOperator();
			// TODO: compare with SIM operator
			// TODO: Error if there is no operator
			if (operator != null && operator.length() > 3) {
				String url = "http://data.fbreader.org/catalogs/litres/sms/smsinfo.php";
				url = ZLNetworkUtil.appendParameter(url, "mcc", operator.substring(0, 3));
				url = ZLNetworkUtil.appendParameter(url, "mnc", operator.substring(3));
				final String name = manager.getNetworkOperatorName();
				if (name != null) {
					url = ZLNetworkUtil.appendParameter(url, "name", name);
				}
				try {
					ZLNetworkManager.Instance().perform(new ZLNetworkRequest(url) {
						public void handleStream(URLConnection connection, InputStream inputStream) throws IOException, ZLNetworkException {
							// TODO: implement
							final SmsInfoXMLReader reader = new SmsInfoXMLReader();
							reader.read(inputStream);
						}
					});
				} catch (ZLNetworkException e) {
					// TODO: implement
				}
				System.err.println(manager.getSimOperator());
				System.err.println(manager.getSimOperatorName());
			}
		}
	}
}

class SmsInfo {
	final String PhoneNumber;
	final String Text;
	final String Sum;
	final String Cost;

	SmsInfo(String phoneNumber, String text, String sum, String cost) {
		PhoneNumber = phoneNumber;
		Text = text;
		Sum = sum;
		Cost = cost;
	}
}

class SmsInfoXMLReader extends ZLXMLReaderAdapter {
	private final String myUserId = "0001";

	final ArrayList<SmsInfo> Infos = new ArrayList<SmsInfo>();
	String ErrorMessage;

	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		if ("info".equals(tag)) {
			final SmsInfo info = new SmsInfo(
				attributes.getValue("phoneNumber"),
				attributes.getValue("smsPrefix") + " " + myUserId,
				attributes.getValue("sum"),
				attributes.getValue("cost")
			);
		} else if ("error".equals(tag)) {
			ErrorMessage = attributes.getValue("text");
		}
		return false;
	}
}
