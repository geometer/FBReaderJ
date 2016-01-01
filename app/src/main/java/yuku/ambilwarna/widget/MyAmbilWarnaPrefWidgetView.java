package yuku.ambilwarna.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class MyAmbilWarnaPrefWidgetView extends AmbilWarnaPrefWidgetView {
    boolean drawCross;

    public MyAmbilWarnaPrefWidgetView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void showCross(boolean show) {
        drawCross = show;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (drawCross) {
            canvas.drawLine(strokeWidth, strokeWidth,
                    rectSize - strokeWidth, rectSize - strokeWidth, paint);
            canvas.drawLine(strokeWidth,
                    rectSize - strokeWidth, rectSize - strokeWidth, strokeWidth, paint);
        }
    }
}
