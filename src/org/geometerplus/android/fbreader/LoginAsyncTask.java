/*
 * Copyright (c) 2010 Evenflow, Inc.
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package org.geometerplus.android.fbreader;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client.DropboxAPI;


public class LoginAsyncTask extends AsyncTask<Void, Void, Integer> {
    private static final String TAG = "LoginAsyncTask";
	
	boolean tokenavailable;
    String mUser;
    String mPassword;
    String mErrorMessage="";
    DropBoxSyncer mDropBoxSyncer;
    DropboxAPI.Config mConfig;
    DropboxAPI.Account mAccount;
    
    // Will just log in
    public LoginAsyncTask(DropBoxSyncer act, String user, String password, DropboxAPI.Config config, boolean hastoken) {
        super();

        mDropBoxSyncer = act;
        mUser = user;
        mPassword = password;
        mConfig = config;
		tokenavailable = hastoken;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        try {
        	DropboxAPI api = mDropBoxSyncer.getAPI();
        	
        	int success = DropboxAPI.STATUS_NONE;
        	if (!api.isAuthenticated()) {
				if(!tokenavailable) {
	            	mConfig = api.authenticate(mConfig, mUser, mPassword);
				}
				else {
	            	mConfig = api.authenticateToken(mUser, mPassword, mConfig);
				}
	            mDropBoxSyncer.setConfig(mConfig);
            
	            success = mConfig.authStatus;

	            if (success != DropboxAPI.STATUS_SUCCESS) {
	            	return success;
	            }
        	}
        	mAccount = api.accountInfo();

        	if (!mAccount.isError()) {
        		return DropboxAPI.STATUS_SUCCESS;
        	} else {
        		Log.e(TAG, "Account info error: " + mAccount.httpCode + " " + mAccount.httpReason);
				mDropBoxSyncer.clearDropBoxCredentials();
				DropBoxSyncer.IN_PROGRESS = false;
        		return DropboxAPI.STATUS_FAILURE;
        	}
        } catch (Exception e) {
            Log.e(TAG, "Error in logging in.", e);
			DropBoxSyncer.IN_PROGRESS = false;
            return DropboxAPI.STATUS_NETWORK_ERROR;
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (result == DropboxAPI.STATUS_SUCCESS) {
        	if (mConfig != null && mConfig.authStatus == DropboxAPI.STATUS_SUCCESS) {
				if(!tokenavailable) {
            		mDropBoxSyncer.storeKeys(mConfig.accessTokenKey, mConfig.accessTokenSecret);
				}
            	mDropBoxSyncer.setLoggedIn(true);
				//callback
				mDropBoxSyncer.bookSync();
				mDropBoxSyncer.clearDropBoxCredentials();
            }
        } else {
        	if (result == DropboxAPI.STATUS_NETWORK_ERROR) {
				DropBoxSyncer.IN_PROGRESS = false;
        		mDropBoxSyncer.showToast("Network error: " + mConfig.authDetail);
        	} else {
        		mDropBoxSyncer.showToast("Unsuccessful login.");
				mDropBoxSyncer.clearDropBoxCredentials();
				DropBoxSyncer.IN_PROGRESS = false;
        	}
        }
    }

}
