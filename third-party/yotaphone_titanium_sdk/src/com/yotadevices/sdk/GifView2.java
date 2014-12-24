package com.yotadevices.sdk;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.InputStream;

public class GifView2 extends View {

	private InputStream gifInputStream;
	private Movie gifMovie;
	private int movieWidth, movieHeight;
	private long movieDuration;
	private long mMovieStart;
	private OnGifEventListener mListener;
	private int mOldTime;
	private long mStopDuration = -1;
	private long mCurrentRelTime = 0;

	public GifView2(Context context) {
		super(context);
		init(context, null, -1);
	}

	public GifView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, -1);
	}

	public GifView2(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr);
	}

	private void init(Context context, AttributeSet attributeSet, int defStyle) {
		if (isInEditMode())
			return;
		TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.GifView2, defStyle, 0);

		int animId = a.getResourceId(R.styleable.GifView2_animationGif, -1);
		a.recycle();
		setFocusable(true);
		gifInputStream = context.getResources().openRawResource(animId);

		gifMovie = Movie.decodeStream(gifInputStream);
		movieWidth = gifMovie.width();
		movieHeight = gifMovie.height();
		movieDuration = gifMovie.duration();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(movieWidth, movieHeight);
	}

	public int getMovieWidth() {
		return movieWidth;
	}

	public int getMovieHeight() {
		return movieHeight;
	}

	public long getMovieDuration() {
		return movieDuration;
	}

	public void setStopOnDuration(long stopDuration) {
		mStopDuration = stopDuration;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		long now = android.os.SystemClock.uptimeMillis();
		if (mMovieStart == 0) { // first time
			mMovieStart = now;
		}

		if (gifMovie != null) {

			int dur = gifMovie.duration();

			if (dur == 0) {
				dur = 1000;
			}

			int relTime = (int) ((now - mMovieStart) % dur);
			if (relTime < mOldTime) {
				mNumPlayed++;
				mCurrentRelTime += getMovieDuration();
				if (mListener != null) {
					mListener.onStart();
				}
			}

			// Log.d("gifMovie", "-- " + (mCurrentRelTime + relTime));

			if ((mNumPlays == -1 || mNumPlayed < mNumPlays)
					&& (mStopDuration == -1 || (mCurrentRelTime + relTime) < mStopDuration)) {
				gifMovie.setTime(relTime);
			}

			gifMovie.draw(canvas, 0, 0);
			if ((mNumPlays == -1 || mNumPlayed < mNumPlays)
					&& (mStopDuration == -1 || (mCurrentRelTime + relTime) < mStopDuration)) {
				invalidate();
			}

			mOldTime = relTime;
		}
	}

	private int mNumPlayed = 0;
	private int mNumPlays = -1;

	public void setPlayCount(int numPlays) {
		mNumPlays = numPlays;
	}

	public OnGifEventListener getListener() {
		return mListener;
	}

	public void setListener(OnGifEventListener mListener) {
		this.mListener = mListener;
	}

	public interface OnGifEventListener {
		public void onStart();
	}

}