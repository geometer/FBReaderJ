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

package org.geometerplus.android.fbreader.network.litres;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.network.ZLNetworkAuthenticationException;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.network.authentication.litres.*;

/*
 * Algorithm:
 *    step 0. Select first e-mail from the list
 *       a. Success =} 1
 *       b. E-mails list is empty =} 4
 *    step 1. try to login with auto-generated username/password
 *       a. Success =} 'login successful' dilaog =} finish
 *       b. Authentication failure =} 2
 *       c. Any other problem =} show error message =} finish
 *    step 2. try to register with given e-mail + auto-generated username/password
 *       a. Success =} 'registration successful' dialog =} finish
 *       b. Username already in used =} 3
 *       c. E-address already in use =} 3
 *       d. Any other problem =} show error message =} finish
 *    step 3. 'e-address already in use' dialog, choices:
 *       a. Sign in =} standard sign in dialog
 *       b. Send password =} 5
 *       c. Select other email =} 4
 *    step 4. 'email selection' dialog
 *       a. Ok =} 1
 *       b. Cancel =} finish
 *    step 5. send password
 *       a. Success =} 'password sent dialog' =} finish
 *       b. Failure =} show error message =} finish
 */

public class AutoRegistrationActivity extends RegistrationActivity {
	private final RegistrationUtils myUtil = new RegistrationUtils(this);

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");

		myResource = dialogResource.getResource("litresAutoSignIn");
		setContentView(R.layout.lr_auto_registration);
		setTitle(myResource.getResource("title").getValue());

		getOkButton().setText(buttonResource.getResource("ok").getValue());
		getCancelButton().setText(buttonResource.getResource("cancel").getValue());
		getTextArea().setVisibility(View.GONE);
		getActionSignIn().setVisibility(View.GONE);
		getActionAnotherEmail().setVisibility(View.GONE);
		getActionRecover().setVisibility(View.GONE);
		getEmailControl().setVisibility(View.GONE);
		getButtons().setVisibility(View.GONE);

		startAutoRegistration();
	}

	// step 0
	private void startAutoRegistration() {
		final String email = myUtil.firstEMail();
		if (email != null) {
			runAutoLogin(email);
		} else {
			runEmailSelectionDialog(null);
		}
	}

	// step 1
	private void runAutoLogin(final String email) {
		final String username = myUtil.getAutoLogin(email);
		final String password = myUtil.getAutoPassword();

		final SignInNetworkRunnable runnable = new SignInNetworkRunnable(username, password);
		final PostRunnable post = new PostRunnable() {
			public void run(ZLNetworkException e) {
				if (e == null) {
					reportSuccess(username, password, runnable.XmlReader.Sid);
					showFinalMessage(
						myResource.getResource("signedIn").getValue()
							.replace("%s", email)
					);
				} else if (e instanceof ZLNetworkAuthenticationException) {
					runAutoRegistraion(email);
				} else {
					showErrorMessage(e);
				}
			}
		};
		runWithMessage("autoSignIn", runnable, post);
	}

	// step 2
	private void runAutoRegistraion(final String email) {
		final String username = myUtil.getAutoLogin(email);
		final String password = myUtil.getAutoPassword();

		final RegistrationNetworkRunnable runnable =
			new RegistrationNetworkRunnable(username, password, email);
		final PostRunnable post = new PostRunnable() {
			public void run(ZLNetworkException e) {
				if (e == null) {
					reportSuccess(username, password, runnable.XmlReader.Sid);
					showFinalMessage(
						myResource.getResource("registrationSuccessful").getValue()
							.replace("%s", email)
					);
				} else if (e instanceof LitResRegisterUserXMLReader.AlreadyInUseException) {
					runEmailAlreadyInUseDialog(email);
				} else {
					showErrorMessage(e);
				}
			}
		};
		runWithMessage("autoSignIn", runnable, post);
	}

	// step 3
	private void runEmailAlreadyInUseDialog(final String email) {
		final ZLResource actionResource = myResource.getResource("actions");
		getTextArea().setVisibility(View.VISIBLE);
		getTextArea().setText(actionResource.getResource("title").getValue().replace("%s", email));
		final View.OnClickListener rbListener = new View.OnClickListener() {
			public void onClick(View view) {
				final RadioButton rb = (RadioButton)view;
				getActionSignIn().setChecked(false);
				getActionAnotherEmail().setChecked(false);
				getActionRecover().setChecked(false);
				rb.setChecked(true);
				getOkButton().setEnabled(true);
			}
		};
		getActionSignIn().setVisibility(View.GONE);

		/*
		getActionSignIn().setVisibility(View.VISIBLE);
		getActionSignIn().setText(actionResource.getResource("signIn").getValue());
		getActionSignIn().setOnClickListener(rbListener);
		*/
		getActionAnotherEmail().setVisibility(View.VISIBLE);
		getActionAnotherEmail().setText(actionResource.getResource("anotherEmail").getValue());
		getActionAnotherEmail().setOnClickListener(rbListener);
		getActionRecover().setVisibility(View.VISIBLE);
		getActionRecover().setText(actionResource.getResource("recover").getValue());
		getActionRecover().setOnClickListener(rbListener);
		getEmailControl().setVisibility(View.GONE);
		getButtons().setVisibility(View.VISIBLE);
		getOkButton().setVisibility(View.VISIBLE);
		getOkButton().setEnabled(false);
		getOkButton().setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (getActionSignIn().isChecked()) {
					// TODO: implement
				} else if (getActionAnotherEmail().isChecked()) {
					runEmailSelectionDialog(email);
				} else if (getActionRecover().isChecked()) {
					recoverAccountInformation(email);
				}
			}
		});
		getCancelButton().setVisibility(View.VISIBLE);
		getCancelButton().setOnClickListener(myFinishListener);
	}

	// step 4
	private void runEmailSelectionDialog(String email) {
		getTextArea().setVisibility(View.VISIBLE);
		getTextArea().setText(myResource.getResource("email").getValue());
		getActionSignIn().setVisibility(View.GONE);
		getActionAnotherEmail().setVisibility(View.GONE);
		getActionRecover().setVisibility(View.GONE);
		getEmailControl().setVisibility(View.VISIBLE);
		setupEmailControl(getEmailControl(), email);
		getButtons().setVisibility(View.VISIBLE);
		getOkButton().setVisibility(View.VISIBLE);
		getOkButton().setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				runAutoLogin(getEmailTextView().getText().toString().trim());
			}
		});
		getCancelButton().setVisibility(View.VISIBLE);
		getCancelButton().setOnClickListener(myFinishListener);
	}

	// step 5
	private void recoverAccountInformation(final String email) {
		System.err.println("recoverAccountInformation 0");
		final LitResPasswordRecoveryXMLReader xmlReader = new LitResPasswordRecoveryXMLReader();

		final NetworkRunnable runnable = new NetworkRunnable() {
			public void run() throws ZLNetworkException {
		System.err.println("recoverAccountInformation 1");
				final LitResNetworkRequest request = new LitResNetworkRequest(myRecoverPasswordURL, xmlReader);
				request.addPostParameter("mail", email);
				myNetworkContext.perform(request);
			}
		};
		final PostRunnable post = new PostRunnable() {
			public void run(ZLNetworkException e) {
		System.err.println("recoverAccountInformation 2");
				if (e == null) {
		System.err.println("recoverAccountInformation 3");
					showFinalMessage(
						myResource.getResource("passwordSent").getValue().replace("%s", email)
					);
				} else {
		System.err.println("recoverAccountInformation 4");
					showErrorMessage(e);
				}
			}
		};
		System.err.println("recoverAccountInformation 5");
		runWithMessage("recoverPassword", runnable, post);
		System.err.println("recoverAccountInformation 6");
	}

	private TextView getTextArea() {
		return (TextView)findViewById(R.id.lr_auto_registration_text);
	}

	private RadioButton getActionSignIn() {
		return (RadioButton)findViewById(R.id.lr_auto_registration_action_signin);
	}

	private RadioButton getActionAnotherEmail() {
		return (RadioButton)findViewById(R.id.lr_auto_registration_action_change_email);
	}

	private RadioButton getActionRecover() {
		return (RadioButton)findViewById(R.id.lr_auto_registration_action_recover);
	}

	private View getEmailControl() {
		return findViewById(R.id.lr_auto_registration_email_control);
	}

	private TextView getEmailTextView() {
		return (TextView)getEmailControl().findViewById(R.id.lr_email_edit);
	}

	private View getButtons() {
		return findViewById(R.id.lr_auto_registration_buttons);
	}

	private Button getOkButton() {
		return (Button)getButtons().findViewById(R.id.ok_button);
	}

	private Button getCancelButton() {
		return (Button)getButtons().findViewById(R.id.cancel_button);
	}

	private final View.OnClickListener myFinishListener = new View.OnClickListener() {
		public void onClick(View view) {
			AutoRegistrationActivity.this.finish();
		}
	};

	private void showFinalMessage(String message) {
		getTextArea().setVisibility(View.VISIBLE);
		getTextArea().setText(message);
		getActionSignIn().setVisibility(View.GONE);
		getActionAnotherEmail().setVisibility(View.GONE);
		getActionRecover().setVisibility(View.GONE);
		getEmailControl().setVisibility(View.GONE);
		getButtons().setVisibility(View.VISIBLE);
		getOkButton().setVisibility(View.VISIBLE);
		getOkButton().setOnClickListener(myFinishListener);
		getCancelButton().setVisibility(View.GONE);
	}

	private void showErrorMessage(ZLNetworkException exception) {
		exception.printStackTrace();
		showFinalMessage(exception.getMessage());
	}
}
