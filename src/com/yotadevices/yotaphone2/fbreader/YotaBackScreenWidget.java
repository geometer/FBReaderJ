package com.yotadevices.yotaphone2.fbreader;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookUtil;
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
import android.util.Log;
import android.view.MotionEvent;

public class YotaBackScreenWidget extends ZLAndroidWidget {
	private Bitmap myDefaultCoverBitmap;
	private Boolean myLastPaintWasActive;
	private Book myLastBook;

    private float mStartY;
    private float mStartX;
    private boolean mIsGestureStart = false;

	YotaBackScreenWidget(Context context) {
		super(context);
	}

	public YotaBackScreenWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private Boolean myBackScreenIsActive;
	private Book myCurrentBook;

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
			if (myLastPaintWasActive == null || myLastPaintWasActive
					|| !MiscUtil.equals(myCurrentBook, myLastBook)) {
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
					final ZLLoadableImage loadableImage = (ZLLoadableImage) image;
					if (!loadableImage.isSynchronized()) {
						loadableImage.synchronize();
					}
				}
				final ZLAndroidImageData data = ((ZLAndroidImageManager) ZLAndroidImageManager
						.Instance()).getImageData(image);
				if (data != null) {
					coverBitmap = data.getBitmap(getWidth() - 20,
							getHeight() - 20);
				}
			}
		}
		if (coverBitmap == null) {
			coverBitmap = getDefaultCoverBitmap();
		}

		canvas.drawBitmap(coverBitmap,
				(getWidth() - coverBitmap.getWidth()) / 2,
				(getHeight() - coverBitmap.getHeight()) / 2, paint);
	}

	private Bitmap getDefaultCoverBitmap() {
		if (myDefaultCoverBitmap == null) {
			myDefaultCoverBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.fbreader_256x256);
		}
		return myDefaultCoverBitmap;
	}
	
    private static final String DEBUG_TAG = "motion";

    /*@Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //this.mDetector.onTouchEvent(event);
        int action = event.getActionMasked();

        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                Log.d(DEBUG_TAG,"Action was DOWN");
                mStartX = event.getX();
                mStartY = event.getY();
                mIsGestureStart = true;
                break;
            case (MotionEvent.ACTION_MOVE) :
                //Log.d(DEBUG_TAG,"Action was MOVE");
                if (!mIsGestureStart) {
                    mStartX = event.getX();
                    mStartY = event.getY();
                    mIsGestureStart = true;
                }
                break;
            case (MotionEvent.ACTION_UP) :
                float endX = event.getX();
                float endY = event.getY();
                Log.d(DEBUG_TAG,"Action was UP");
                Log.d(DEBUG_TAG,String.format("x1:%s,y1:%s;x2:%s,y2:%s", mStartX, mStartY, endX, endY));
                if (mIsGestureStart) {
                    // is gesture horizontal
                    if (Math.abs(mStartY - endY) < 200) {
                        if ((mStartX - endX) > 200) {
                            Log.d(DEBUG_TAG,"Gesture right to left");
                        	turnPageStatic(true);
                        }
                        if ((endX - mStartX) > 200) {
                            Log.d(DEBUG_TAG,"Gesture left to right");
                            turnPageStatic(false);
                        }
                    }
                }

                mIsGestureStart = false;
                break;
            case (MotionEvent.ACTION_CANCEL) :
                Log.d(DEBUG_TAG,"Action was CANCEL");
                break;
            case (MotionEvent.ACTION_OUTSIDE) :
                Log.d(DEBUG_TAG,"Movement occurred outside bounds " +
                        "of current screen element");
                break;
            default :
                break;
        }

        return super.dispatchTouchEvent(event);
    }*/
}