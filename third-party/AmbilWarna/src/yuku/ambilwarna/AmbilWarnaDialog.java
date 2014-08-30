package yuku.ambilwarna;

import android.app.*;
import android.content.*;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.*;
import android.view.*;
import android.widget.*;

public class AmbilWarnaDialog {
	public interface OnAmbilWarnaListener {
		void onCancel(AmbilWarnaDialog dialog);
		void onOk(AmbilWarnaDialog dialog, int color);
	}

	final AlertDialog dialog;
	final OnAmbilWarnaListener listener;
	final View viewHue;
	final AmbilWarnaKotak viewSatVal;
	final ImageView viewCursor;
	final View viewOldColor;
	final View viewNewColor;
	final ImageView viewTarget;
	final ViewGroup viewContainer;
	final float[] currentColorHsv = new float[3];

	private final CharSequence positiveButtonText;
	private final CharSequence negativeButtonText;

	/**
	 * create an AmbilWarnaDialog. call this only from OnCreateDialog() or from a background thread.
	 * 
	 * @param context
	 *            current context
	 * @param color
	 *            current color
	 * @param listener
	 *            an OnAmbilWarnaListener, allowing you to get back error or
	 */
	public AmbilWarnaDialog(final Context context, int color, OnAmbilWarnaListener listener, String positiveButtonText, String negativeButtonText) {
		this.listener = listener;
		Color.colorToHSV(color, currentColorHsv);

		this.positiveButtonText = positiveButtonText;
		this.negativeButtonText = negativeButtonText;

		final View view = LayoutInflater.from(context).inflate(R.layout.ambilwarna_dialog, null);
		viewHue = view.findViewById(R.id.ambilwarna_viewHue);
		viewSatVal = (AmbilWarnaKotak) view.findViewById(R.id.ambilwarna_viewSatBri);
		viewCursor = (ImageView) view.findViewById(R.id.ambilwarna_cursor);
		viewOldColor = view.findViewById(R.id.ambilwarna_warnaLama);
		viewNewColor = view.findViewById(R.id.ambilwarna_warnaBaru);
		viewTarget = (ImageView) view.findViewById(R.id.ambilwarna_target);
		viewContainer = (ViewGroup) view.findViewById(R.id.ambilwarna_viewContainer);

		viewSatVal.setHue(getHue());
		viewOldColor.setBackgroundColor(color);
		viewNewColor.setBackgroundColor(color);

		viewHue.setOnTouchListener(new View.OnTouchListener() {
			@Override public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE
						|| event.getAction() == MotionEvent.ACTION_DOWN
						|| event.getAction() == MotionEvent.ACTION_UP) {

					float y = event.getY();
					if (y < 0.f) y = 0.f;
					if (y > viewHue.getMeasuredHeight()) y = viewHue.getMeasuredHeight() - 0.001f; // to avoid looping from end to start.
					float hue = 360.f - 360.f / viewHue.getMeasuredHeight() * y;
					if (hue == 360.f) hue = 0.f;
					setHue(hue);

					// update view
					viewSatVal.setHue(getHue());
					moveCursor();
					viewNewColor.setBackgroundColor(getColor());

					return true;
				}
				return false;
			}
		});
		viewSatVal.setOnTouchListener(new View.OnTouchListener() {
			@Override public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE
						|| event.getAction() == MotionEvent.ACTION_DOWN
						|| event.getAction() == MotionEvent.ACTION_UP) {

					float x = event.getX(); // touch event are in dp units.
					float y = event.getY();

					if (x < 0.f) x = 0.f;
					if (x > viewSatVal.getMeasuredWidth()) x = viewSatVal.getMeasuredWidth();
					if (y < 0.f) y = 0.f;
					if (y > viewSatVal.getMeasuredHeight()) y = viewSatVal.getMeasuredHeight();

					setSat(1.f / viewSatVal.getMeasuredWidth() * x);
					setVal(1.f - (1.f / viewSatVal.getMeasuredHeight() * y));

					// update view
					moveTarget();
					viewNewColor.setBackgroundColor(getColor());

					return true;
				}
				return false;
			}
		});

		dialog = new AlertDialog.Builder(context)
			.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					if (AmbilWarnaDialog.this.listener != null) {
						AmbilWarnaDialog.this.listener.onOk(AmbilWarnaDialog.this, getColor());
					}
				}
			})
			.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					if (AmbilWarnaDialog.this.listener != null) {
						AmbilWarnaDialog.this.listener.onCancel(AmbilWarnaDialog.this);
					}
				}
			})
			.setOnCancelListener(new OnCancelListener() {
				// if back button is used, call back our listener.
				@Override public void onCancel(DialogInterface paramDialogInterface) {
					if (AmbilWarnaDialog.this.listener != null) {
						AmbilWarnaDialog.this.listener.onCancel(AmbilWarnaDialog.this);
					}

				}
			})
			.create();
		// kill all padding from the dialog window
		dialog.setView(view, 0, 0, 0, 0);

		// move cursor & target on first draw
		ViewTreeObserver vto = view.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override public void onGlobalLayout() {
				moveCursor();
				moveTarget();
				view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});
	}

	protected void moveCursor() {
		float y = viewHue.getMeasuredHeight() - (getHue() * viewHue.getMeasuredHeight() / 360.f);
		if (y == viewHue.getMeasuredHeight()) y = 0.f;
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewCursor.getLayoutParams();
		layoutParams.leftMargin = (int) (viewHue.getLeft() - Math.floor(viewCursor.getMeasuredWidth() / 2) - viewContainer.getPaddingLeft());
		;
		layoutParams.topMargin = (int) (viewHue.getTop() + y - Math.floor(viewCursor.getMeasuredHeight() / 2) - viewContainer.getPaddingTop());
		;
		viewCursor.setLayoutParams(layoutParams);
	}

	protected void moveTarget() {
		float x = getSat() * viewSatVal.getMeasuredWidth();
		float y = (1.f - getVal()) * viewSatVal.getMeasuredHeight();
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewTarget.getLayoutParams();
		layoutParams.leftMargin = (int) (viewSatVal.getLeft() + x - Math.floor(viewTarget.getMeasuredWidth() / 2) - viewContainer.getPaddingLeft());
		layoutParams.topMargin = (int) (viewSatVal.getTop() + y - Math.floor(viewTarget.getMeasuredHeight() / 2) - viewContainer.getPaddingTop());
		viewTarget.setLayoutParams(layoutParams);
	}

	private int getColor() {
		return Color.HSVToColor(currentColorHsv);
	}

	private float getHue() {
		return currentColorHsv[0];
	}

	private float getSat() {
		return currentColorHsv[1];
	}

	private float getVal() {
		return currentColorHsv[2];
	}

	private void setHue(float hue) {
		currentColorHsv[0] = hue;
	}

	private void setSat(float sat) {
		currentColorHsv[1] = sat;
	}

	private void setVal(float val) {
		currentColorHsv[2] = val;
	}

	public void show() {
		dialog.show();
	}

	public AlertDialog getDialog() {
		return dialog;
	}
}
