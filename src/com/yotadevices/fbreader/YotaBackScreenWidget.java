package com.yotadevices.fbreader;

import org.geometerplus.android.fbreader.FBReaderApplication;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;
import org.geometerplus.zlibrary.core.util.MiscUtil;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

public class YotaBackScreenWidget extends ZLAndroidWidget {
	private Bitmap myDefaultCoverBitmap;
	private Boolean myLastPaintWasActive;
	private Book myLastBook;
	private static final int BS_DENSITY = 240;

	YotaBackScreenWidget(Context context) {
		super(context);
	}
	
	public YotaBackScreenWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	private volatile byte[] myStoredMD5 = null;
	private Canvas myCanvas;
	private Boolean myBackScreenIsActive;
	private Book myCurrentBook;

	@Override
	public synchronized void repaint() {
		if (myCanvas == null) {
			Bitmap myBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
			myBitmap.setDensity(BS_DENSITY);
			myCanvas = new Canvas(myBitmap);
		}
		
		draw(myCanvas);
//		final byte[] currentMD5 = MD5(myBitmap);
//		if (myStoredMD5 == null || !myStoredMD5.equals(currentMD5)) {
//			getBSDrawer().drawBitmap(0, 0, myBitmap, BSDrawer.Waveform.WAVEFORM_GC_PARTIAL);
//			myStoredMD5 = currentMD5;
//		}
	}
	
	@Override
	protected Canvas createCanvas(Bitmap bitmap) {
		Canvas canvas = new Canvas(bitmap);
		canvas.setDensity(getContext().getResources().getDisplayMetrics().densityDpi);
		return canvas;
	}
	
	public void setIsBsActive(boolean isActive) {
		myBackScreenIsActive = isActive;
	}
	
	public void setBook(Book book) {
		myCurrentBook = book;
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		if (myBackScreenIsActive) {
			super.onDraw(canvas);
		} else {
			if (myLastPaintWasActive == null ||
				myLastPaintWasActive ||
				!MiscUtil.equals(myCurrentBook, myLastBook)) {
				drawCover(canvas, myCurrentBook);
			}
		}
		myLastPaintWasActive = myBackScreenIsActive;
		myLastBook = myCurrentBook;
	}

	private void drawCover(Canvas canvas, Book currentBook) {
		final Paint paint = new Paint();
		paint.setColor(0xFFFFFFFF);
		canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

		Bitmap coverBitmap = null;
		if (currentBook != null) {
			final ZLImage image = BookUtil.getCover(currentBook);

			if (image != null) {
				if (image instanceof ZLLoadableImage) {
					final ZLLoadableImage loadableImage = (ZLLoadableImage)image;
					if (!loadableImage.isSynchronized()) {
						loadableImage.synchronize();
					}
				}
				final ZLAndroidImageData data =
					((ZLAndroidImageManager)ZLAndroidImageManager.Instance()).getImageData(image);
				if (data != null) {
					coverBitmap = data.getBitmap(
						getWidth() - 20, getHeight() - 20
					);
				}
			}
		}
		if (coverBitmap == null) {
			coverBitmap = getDefaultCoverBitmap();
		}

		canvas.drawBitmap(
			coverBitmap,
			(getWidth() - coverBitmap.getWidth()) / 2,
			(getHeight() - coverBitmap.getHeight()) / 2,
			paint
		);
	}

	private Bitmap getDefaultCoverBitmap() {
		if (myDefaultCoverBitmap == null) {
			myDefaultCoverBitmap = BitmapFactory.decodeResource(
				getResources(), R.drawable.fbreader_256x256
			);
		}
		return myDefaultCoverBitmap;
	}
}