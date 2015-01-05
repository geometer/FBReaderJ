package yuku.ambilwarna.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.View;

public class AmbilWarnaPrefWidgetView extends View {
	Paint paint;
	float rectSize;
	float strokeWidth;

	boolean drawCross;

	public AmbilWarnaPrefWidgetView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		float density = context.getResources().getDisplayMetrics().density;
		rectSize = FloatMath.floor(24.f * density + 0.5f);
		strokeWidth = FloatMath.floor(1.f * density + 0.5f);

		paint = new Paint();
		paint.setColor(0xffffffff);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(strokeWidth);
	}

	public void showCross(boolean show) {
		drawCross = show;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawRect(strokeWidth, strokeWidth, rectSize - strokeWidth, rectSize - strokeWidth, paint);
		if (drawCross) {
			canvas.drawLine(strokeWidth, strokeWidth, rectSize - strokeWidth, rectSize - strokeWidth, paint);
			canvas.drawLine(strokeWidth, rectSize - strokeWidth, rectSize - strokeWidth, strokeWidth, paint);
		}
	}
}
