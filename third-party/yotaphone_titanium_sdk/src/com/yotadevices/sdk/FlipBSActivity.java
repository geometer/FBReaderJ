package com.yotadevices.sdk;

import android.widget.TextView;

public class FlipBSActivity extends NotificationBSActivity {
    private TextView mDescription;

    @Override
    protected void onBSCreate() {
        super.onBSCreate();
        setBSContentView(R.layout.flip_layout);

        mDescription = (TextView) findViewById(R.id.flip_popup_description);
    }

    protected void setDescription(String description) {
        mDescription.setText(description);
    }

    protected void setDescription(int descriptionResId) {
        mDescription.setText(getString(descriptionResId));
    }
}
