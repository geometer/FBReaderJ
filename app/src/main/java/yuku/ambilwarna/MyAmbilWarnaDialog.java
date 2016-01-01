package yuku.ambilwarna;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;

public class MyAmbilWarnaDialog extends AmbilWarnaDialog {

    public MyAmbilWarnaDialog(Context context, int color, OnAmbilWarnaListener listener) {

        this(context, color, false, listener, context.getString(android.R.string.ok),
                context.getString(android.R.string.cancel));
    }

    public MyAmbilWarnaDialog(final Context context, int color, boolean supportsAlpha,
                                     OnAmbilWarnaListener listener) {
        this(context, color, false, listener, context.getString(android.R.string.ok),
                context.getString(android.R.string.cancel));
    }

    public MyAmbilWarnaDialog(Context context, int color, OnAmbilWarnaListener listener,
                                     String positiveButtonText, String negativeButtonText) {
        this(context, color, false, listener, positiveButtonText, negativeButtonText);
    }

    public MyAmbilWarnaDialog(final Context context, int color, boolean supportsAlpha,
                                     OnAmbilWarnaListener listener, String positiveButtonText,
                                     String negativeButtonText) {
        super(context, color, supportsAlpha, listener);

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, positiveButtonText,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (MyAmbilWarnaDialog.this.listener != null) {
                    MyAmbilWarnaDialog.this.listener.onOk(MyAmbilWarnaDialog.this, getColor());
                }
            }
        });

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, negativeButtonText,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (MyAmbilWarnaDialog.this.listener != null) {
                    MyAmbilWarnaDialog.this.listener.onCancel(MyAmbilWarnaDialog.this);
                }
            }
        });

    }

    private int getColor() {
        final int argb = Color.HSVToColor(currentColorHsv);
        return alpha << 24 | (argb & 0x00ffffff);
    }
}
