package com.paragon.dictionary.fbreader;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.paragon.open.dictionary.api.*;
import org.geometerplus.android.fbreader.DictionaryUtil;
import org.geometerplus.zlibrary.ui.android.R;

import java.io.FileOutputStream;
import java.util.ArrayList;

public class OpenDictionaryFlyout {
    private final Dictionary myDictionary;

    public OpenDictionaryFlyout(Dictionary dictionary) {
        myDictionary = dictionary;
    }

    private static PopupWindow popupFrame = null;
    private static WebView articleView = null;
    private static View root = null;
    private static TextView titleLabel = null;
    private static ImageButton openDictionaryButton = null;

    private static PopupWindow createPopup(Activity activity) {
        final FrameLayout layout = new FrameLayout(activity.getBaseContext());

        final DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final PopupWindow frame = new PopupWindow(layout, metrics.widthPixels, metrics.heightPixels / 3);
        root = activity.getLayoutInflater().inflate(R.layout.dictionary_flyout, layout);
        articleView = (WebView) root.findViewById(R.id.dictionary_article_view);
        titleLabel = (TextView) root.findViewById(R.id.dictionary_title_label);
        openDictionaryButton = (ImageButton) root.findViewById(R.id.dictionary_open_button);

        activity.getCurrentFocus().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && frame.isShowing()) {
                    frame.dismiss();
                    return true;
                }
                return false;
            }
        });

        return frame;
    }

    private static void showFrame(Activity activity, DictionaryUtil.PopupFrameMetric frameMetrics) {
        final DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final int height = frameMetrics.height;
        final int width = Math.min(metrics.widthPixels, metrics.heightPixels);
        popupFrame.showAtLocation(activity.getCurrentFocus(), frameMetrics.gravity | Gravity.CENTER_HORIZONTAL, 0, 0);
        popupFrame.update(width, height);
    }

    void openTextInDictionary(String text) {
        myDictionary.showTranslation(text);
    }

    String saveArticle(String data, Context context) {
        final String filename = "open_dictionary_article.html";
        final FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return "file://" + context.getFilesDir().getAbsolutePath() + "/" + filename;
    }

    public void showTranslation(final Activity activity, final String text, DictionaryUtil.PopupFrameMetric frameMetrics) {
        if (!myDictionary.isTranslationAsTextSupported())
            openTextInDictionary(text);

        if (popupFrame == null)
            popupFrame = createPopup(activity);

        titleLabel.setText(myDictionary.getName());
        openDictionaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTextInDictionary(text);
                popupFrame.dismiss();
            }
        });

        activity.getCurrentFocus().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupFrame.dismiss();
            }
        });
        activity.getCurrentFocus().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (popupFrame.isShowing()) {
                    popupFrame.dismiss();
                    return true;
                }
                return false;
            }
        });

        articleView.loadData("", "text/text", "UTF-8");
        showFrame(activity, frameMetrics);

        myDictionary.getTranslationAsText(text, TranslateMode.SHORT, TranslateFormat.HTML, new Dictionary.TranslateAsTextListener() {
            @Override
            public void onComplete(String s, TranslateMode translateMode) {
                final String url = saveArticle(s.replace("</BODY>", "<br><br></BODY>"), activity.getApplicationContext());
                if (url == null || url.isEmpty())
                    openTextInDictionary(text);
                else
                    articleView.loadUrl(url);
            }

            @Override
            public void onWordNotFound(ArrayList<String> similarWords) {
                popupFrame.dismiss();
                openTextInDictionary(text);
            }

            @Override
            public void onError(com.paragon.open.dictionary.api.Error error) {
                popupFrame.dismiss();
            }

            @Override
            public void onIPCError(String s) {
                popupFrame.dismiss();
            }
        });
    }
}