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

import java.util.*;
import java.lang.reflect.*;

import android.content.Context;
import android.telephony.TelephonyManager;

class RegistrationUtils {
	private final Context myContext;

	RegistrationUtils(Context context) {
		myContext = context;
	}

	String getAutoLogin() {
		collectEMails();
		if (myEMails.isEmpty()) {
			return null;
		}
		return "fbreader-auto-" + myEMails.get(0).replace(".", "-").replace("@", "-at-");
	}

	String getAutoPassword() {
		final TelephonyManager manager = (TelephonyManager)myContext.getSystemService(Context.TELEPHONY_SERVICE);
		final String id = manager.getDeviceId();
		return (id.length() > 9) ? "XXX" + id.substring(3, id.length() - 3) : id;
	}
	
	private List<String> myEMails;
	List<String> eMails() {
		collectEMails();
		return myEMails;
	}

	private void collectEMails() {
		if (myEMails != null) {
			return;
		} 
		try {
			final Class<?> cls$AccountManager = Class.forName("android.accounts.AccountManager");
			final Class<?> cls$Account = Class.forName("android.accounts.Account");

			final Method meth$AccountManager$get = cls$AccountManager.getMethod("get", Context.class);
			final Method meth$AccountManager$getAccountsByType = cls$AccountManager.getMethod("getAccountsByType", String.class);
			final Field fld$Account$name = cls$Account.getField("name");

			if (meth$AccountManager$get.getReturnType() == cls$AccountManager
					&& meth$AccountManager$getAccountsByType.getReturnType().getComponentType() == cls$Account
					&& fld$Account$name.getType() == String.class) {
				final Object mgr = meth$AccountManager$get.invoke(null, myContext);
				final Object[] accountsByType = (Object[]) meth$AccountManager$getAccountsByType.invoke(mgr, "com.google"); 
				myEMails = new ArrayList<String>(accountsByType.length);
				for (Object a: accountsByType) {
					final String value = (String) fld$Account$name.get(a);
					if (value != null && value.length() > 0) {
						myEMails.add(value);
					}
				}
				return;
			}
		} catch (ClassNotFoundException e) {
		} catch (NoSuchMethodException e) {
		} catch (NoSuchFieldException e) {
		} catch (IllegalAccessException e) {
		} catch (IllegalArgumentException e) {
		} catch (InvocationTargetException e) {
		}
		myEMails = Collections.<String>emptyList();
	}
}
