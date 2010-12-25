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
import java.util.HashMap;
import java.util.Locale;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.DialogInterface;
import android.view.*;
import android.widget.*;
import android.telephony.TelephonyManager;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.network.*;
import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

public class SmsRefillingActivity extends Activity {
	private ZLResource myResource;
	private Button myOkButton;
	private SmsInfo mySelectedInfo;

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
		okButton.setText(buttonResource.getResource("sendSms").getValue());
		okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				final ZLResource dialogResource = ZLResource.resource("dialog");
				final ZLResource resource = dialogResource.getResource("sendSmsDialog");
				final ZLResource buttonResource = dialogResource.getResource("button");
				new AlertDialog.Builder(SmsRefillingActivity.this)
					.setTitle(resource.getResource("title").getValue())
					.setMessage(resource.getResource("message").getValue()
						.replace("%s0", mySelectedInfo.Cost)
						.replace("%s1", mySelectedInfo.Sum)
						.replace("..", ".") // a hack to avoid strings like "100.00 р.."
											// at end of the sentence
					)
					.setPositiveButton(
						buttonResource.getResource("sendSms").getValue(),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								// TODO: send SMS
								finish();
							}
						}
					)
					.setNegativeButton(buttonResource.getResource("cancel").getValue(), null)
					.create().show();
			}
		});
		okButton.setEnabled(false);

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

		setupListView();
	}

	private void setError(String text) {
		((TextView)findViewById(R.id.sms_error)).setText(text);
		findViewById(R.id.sms_list).setVisibility(View.GONE);
		findViewById(R.id.sms_ok_button).setVisibility(View.GONE);
	}

	private void setupListView() {
		final TelephonyManager manager = (TelephonyManager)getSystemService(Activity.TELEPHONY_SERVICE);
		if (manager == null) {
			setError(myResource.getResource("noPhoneInfo").getValue());
			return;
		}

		final String operator = manager.getNetworkOperator();
		if (operator == null || operator.length() <= 3) {
			setError(myResource.getResource("noCellularNetwork").getValue());
			return;
		}

		// TODO: compare with SIM operator
		System.err.println(manager.getSimOperator());
		System.err.println(manager.getSimOperatorName());
		String url = "http://data.fbreader.org/catalogs/litres/sms/smsinfo.php";
		url = ZLNetworkUtil.appendParameter(url, "mcc", operator.substring(0, 3));
		url = ZLNetworkUtil.appendParameter(url, "mnc", operator.substring(3));
		url = ZLNetworkUtil.appendParameter(url, "name", manager.getNetworkOperatorName());
		url = ZLNetworkUtil.appendParameter(url, "lang", Locale.getDefault().getLanguage());
		try {
			ZLNetworkManager.Instance().perform(new ZLNetworkRequest(url) {
				public void handleStream(URLConnection connection, InputStream inputStream) throws IOException, ZLNetworkException {
					final SmsInfoXMLReader reader = new SmsInfoXMLReader();
					reader.read(inputStream);
					if (reader.Infos.size() > 0) {
						((ListView)findViewById(R.id.sms_list)).setAdapter(
							new SmsListAdapter(reader.Infos)
						);
						findViewById(R.id.sms_error).setVisibility(View.GONE);
					} else {
						setError(reader.ErrorMessage);
					}
				}
			});
		} catch (ZLNetworkException e) {
			setError(e.getMessage());
		}
	}

	private class SmsListAdapter extends BaseAdapter implements RadioButton.OnCheckedChangeListener {
		private final ArrayList<SmsInfo> myInfos;
		private final HashMap<RadioButton,SmsInfo> myButtons = new HashMap<RadioButton,SmsInfo>();

		SmsListAdapter(ArrayList<SmsInfo> infos) {
			myInfos = infos;
		}

		@Override
		public final int getCount() {
			return myInfos.size();
		}

		@Override
		public final SmsInfo getItem(int position) {
			return myInfos.get(position);
		}

		@Override
		public final long getItemId(int position) {
			return position;
		}

		private View.OnClickListener myItemViewListener = new View.OnClickListener() {
			public void onClick(View view) {
				((RadioButton)view.findViewById(R.id.sms_info_button)).toggle();
			}
		};

		@Override
		public View getView(int position, View convertView, final ViewGroup parent) {
			final View view = (convertView != null) ?  convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.sms_info, parent, false);
			final SmsInfo info = getItem(position);
			((TextView)view.findViewById(R.id.sms_info_text)).setText(info.Sum);
			((TextView)view.findViewById(R.id.sms_info_text2)).setText(
				myResource.getResource("youPay").getValue().replace("%s", info.Cost)
			);
			final RadioButton button = (RadioButton)view.findViewById(R.id.sms_info_button);
			myButtons.put(button, info);
			button.setOnCheckedChangeListener(this);
			button.setChecked(info == mySelectedInfo);
			view.setOnClickListener(myItemViewListener);
			return view;
		}

		public void onCheckedChanged(CompoundButton button, boolean isChecked) {
			if (isChecked) {
				for (RadioButton b : myButtons.keySet()) {
					if (b != button) {
						b.setChecked(false);
					}
				}
				mySelectedInfo = myButtons.get((RadioButton)button);
				findButton(R.id.sms_ok_button).setEnabled(true);
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
			Infos.add(new SmsInfo(
				attributes.getValue("phoneNumber"),
				attributes.getValue("smsPrefix") + " " + myUserId,
				attributes.getValue("sum"),
				attributes.getValue("cost")
			));
		} else if ("error".equals(tag)) {
			ErrorMessage = attributes.getValue("text");
		}
		return false;
	}
}
