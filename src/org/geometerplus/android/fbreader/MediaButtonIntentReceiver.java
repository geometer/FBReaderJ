/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
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

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class MediaButtonIntentReceiver extends BroadcastReceiver {

    private static String myTtsActionId;
    static void setTtsActionId(String actionId) {
        myTtsActionId = actionId;
    }

    public MediaButtonIntentReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String intentAction = intent.getAction();

        FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();
        if (fbreader == null || myTtsActionId == null || !Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            return;
        }
        KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event == null) {
            return;
        }
        int keycode = event.getKeyCode();
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            switch (keycode) {
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                case 127: // KeyEvent.KEYCODE_MEDIA_PAUSE: - not available under Gingerbread API
                case 126: // KeyEvent.KEYCODE_MEDIA_PLAY:
                    fbreader.runAction(myTtsActionId);
                    break;
                default:
                    break;
            }
        }
        abortBroadcast();
    }
}