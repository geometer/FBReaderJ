package com.yotadevices.yotaphone2.fbreader;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.geometerplus.android.fbreader.OrientationUtil;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.library.BookInfoActivity;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.util.AndroidImageSynchronizer;
import org.geometerplus.fbreader.book.Author;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.book.SeriesInfo;
import org.geometerplus.fbreader.book.Tag;
import org.geometerplus.fbreader.network.HtmlUtil;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLImageProxy;
import org.geometerplus.zlibrary.core.language.Language;
import org.geometerplus.zlibrary.core.language.ZLLanguageUtil;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.w3c.dom.Text;

import java.util.HashSet;
import java.util.List;


public class YotaBookInfoActivity extends Activity implements IBookCollection.Listener{

    private Book mBook;
    private boolean mDontReloadBook;

    private final AndroidImageSynchronizer mImageSynchronizer = new AndroidImageSynchronizer(this);

    private final BookCollectionShadow mCollection = new BookCollectionShadow();

    private View.OnClickListener mOnAddBookAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            FBReaderIntents.putBookExtra(intent, mBook);
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(
                new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this)
        );

        final Intent intent = getIntent();
        mDontReloadBook = intent.getBooleanExtra(BookInfoActivity.FROM_READING_MODE_KEY, false);
        mBook = FBReaderIntents.getBookExtra(intent);

        setContentView(R.layout.yota_book_info);
        ActionBar ab = getActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        setResult(RESULT_CANCELED);
    }

    @Override
    protected void onStart() {
        super.onStart();
        OrientationUtil.setOrientation(this, getIntent());

        if (mBook != null) {
            // we do force language & encoding detection
            mBook.getEncoding();

            setupCover(mBook);
            setupBookInfo(mBook);
            setupAnnotation(mBook);
        }

        Button add = (Button)findViewById(R.id.yota_book_add_button);
        add.setOnClickListener(mOnAddBookAction);

        mCollection.bindToService(this, null);
        mCollection.addListener(this);
    }

    @Override
    protected void onDestroy() {
        mCollection.removeListener(this);
        mCollection.unbind();
        mImageSynchronizer.clear();

        super.onDestroy();
    }

    @Override
    public void onBookEvent(BookEvent event, Book book) {
        if (event == BookEvent.Updated && book.equals(mBook)) {
            mBook.updateFrom(book);
            setupBookInfo(book);
            mDontReloadBook = false;
        }
    }

    @Override
    public void onBuildEvent(IBookCollection.Status status) {

    }

    private void setupCover(Book book) {
        final ImageView coverView = (ImageView)findViewById(R.id.yota_book_cover);

        coverView.setVisibility(View.GONE);
        coverView.setImageDrawable(null);

        final ZLImage image = BookUtil.getCover(book);

        if (image == null) {
            return;
        }

        if (image instanceof ZLImageProxy) {
            ((ZLImageProxy)image).startSynchronization(mImageSynchronizer, new Runnable() {
                public void run() {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            setCover(coverView, image);
                        }
                    });
                }
            });
        } else {
            setCover(coverView, image);
        }
    }

    private void setCover(ImageView coverView, ZLImage image) {
        final ZLAndroidImageData data =
                ((ZLAndroidImageManager)ZLAndroidImageManager.Instance()).getImageData(image);
        if (data == null) {
            return;
        }

        final DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final int maxHeight = metrics.heightPixels * 2 / 3;
        final int maxWidth = maxHeight * 2 / 3;

        final Bitmap coverBitmap = data.getBitmap(2 * maxWidth, 2 * maxHeight);
        if (coverBitmap == null) {
            return;
        }

        coverView.setVisibility(View.VISIBLE);
        //coverView.getLayoutParams().width = maxWidth;
        //coverView.getLayoutParams().height = maxHeight;
        coverView.setImageBitmap(coverBitmap);
    }

    private void setupBookInfo(Book book) {

        TextView title = (TextView)findViewById(R.id.yota_book_title);
        title.setText(book.getTitle());

        final StringBuilder buffer = new StringBuilder();
        final List<Author> authors = book.authors();
        for (Author a : authors) {
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(a.DisplayName);
        }
        TextView author = (TextView)findViewById(R.id.yota_book_author);
        author.setText(buffer.toString());

        buffer.delete(0, buffer.length());
        final HashSet<String> tagNames = new HashSet<String>();
        for (Tag tag : book.tags()) {
            if (!tagNames.contains(tag.Name)) {
                if (buffer.length() > 0) {
                    buffer.append(", ");
                }
                buffer.append(tag.Name);
                tagNames.add(tag.Name);
            }
        }
        if (tagNames.size() > 0) {
            TextView genreTitle = (TextView)findViewById(R.id.yota_book_genre_title);
            genreTitle.setVisibility(View.VISIBLE);
            TextView genre = (TextView)findViewById(R.id.yota_book_genre);
            genre.setText(buffer.toString());
            genre.setVisibility(View.VISIBLE);
        }

        String language = book.getLanguage();
        if (!ZLLanguageUtil.languageCodes().contains(language)) {
            language = Language.OTHER_CODE;
        }
        TextView langTitle = (TextView)findViewById(R.id.yota_book_language_title);
        TextView lang = (TextView)findViewById(R.id.yota_book_language);
        langTitle.setVisibility(View.VISIBLE);
        lang.setVisibility(View.VISIBLE);
        lang.setText(language);
    }

    private void setupAnnotation(Book book) {
        final TextView titleView = (TextView)findViewById(R.id.yota_book_description_title);
        final TextView bodyView = (TextView)findViewById(R.id.yota_book_description);
        final String annotation = BookUtil.getAnnotation(book);
        if (annotation == null) {
            titleView.setVisibility(View.GONE);
            bodyView.setVisibility(View.GONE);
        } else {
            bodyView.setText(HtmlUtil.getHtmlText(annotation));
            bodyView.setMovementMethod(new LinkMovementMethod());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                    finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
