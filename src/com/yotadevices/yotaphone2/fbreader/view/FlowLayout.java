package com.yotadevices.yotaphone2.fbreader.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class FlowLayout extends ViewGroup {

    private int mGravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
    private List<Integer> mWidthList = new ArrayList<Integer>();

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = 0;
        int height = 10;

        /*
         * Рассмотрим ширину детей
         *      если она MATCH_PARENT то ширина будет наша ширина минус наш padding по горизонтали
         *      если на WRAP_CONTENT то ширина будет шириной ребенка
         *      тоже самое с высотой
         *
         *
         * Ширина: Если специцикация EXACTLY - нам надо вернуть ширину которую от нас хотят
         *                           AT_MOST - надо посчитать сколько детей влезает по ширине но не болше заданого
         *                           UNSPECIFIED - надо считать детей в линеечку по ширине
		 *	                              Надо смотреть родителя тогда и от него плясать
         *
         * Высота: Если спецификация EXACTLY - надо вернуть то что от нас хотят
         *                           AT_MOST - надо вернуть не больше чем от нас хотят
         *                           UNSPECIFIED - надо посчитать высоту в зависимости от того расположены дети
         */


        int lineWidth = 0;
        for (int i = 0; i < getChildCount(); ++i) {
            View v = getChildAt(i);
            if (v.getVisibility() == View.GONE) { // Не виден и не занимает места
                continue;
            }
            LayoutParams lp = (LayoutParams)v.getLayoutParams();
            int childWidthType = MeasureSpec.EXACTLY;
            int childWidth = lp.width;
            if (lp.width == LayoutParams.MATCH_PARENT) {
                childWidthType = MeasureSpec.EXACTLY;
                childWidth = widthSize - getPaddingLeft() - getPaddingRight() + lp.leftMargin + lp.rightMargin;
            } else if (lp.width == LayoutParams.WRAP_CONTENT) {
                childWidthType = MeasureSpec.AT_MOST;
                childWidth = widthSize - getPaddingLeft() - getPaddingRight() + lp.leftMargin + lp.rightMargin;
            } // else default value

            int childHeightType = MeasureSpec.UNSPECIFIED;
            int childHeight = 0;

            if (lp.height == LayoutParams.WRAP_CONTENT) {
                childHeightType = MeasureSpec.AT_MOST;
                childHeight = heightSize - getPaddingTop() - getPaddingBottom() + lp.topMargin + lp.bottomMargin;
            } else if (lp.height >= 0) {
                childHeightType = MeasureSpec.EXACTLY;
                childHeight = lp.height;
            }

            v.measure(
                    MeasureSpec.makeMeasureSpec(childWidthType, childWidth),
                    MeasureSpec.makeMeasureSpec(childHeightType, childHeight)
            );

            int childWidthReal = v.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeightReal = v.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            if (lineWidth + childWidthReal > widthSize) { // Переходим на следую
                width = Math.max(lineWidth, childWidthReal);
                lineWidth = childWidthReal;
                height += childHeightReal;
            } else {
                lineWidth += childWidthReal;
                height = Math.max(height, childHeightReal );
            }
        }
        width = Math.max(lineWidth, width);

        width = (widthMode == MeasureSpec.EXACTLY) ? widthSize : width;
        height = (heightMode == MeasureSpec.EXACTLY) ? heightSize : height;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int in_l, int in_t, int r, int b) {
       /*
        * 1) Надо пробежаться по всем детям посчитать максимальную ширину линии и максиманую высоту
        * 2) Исходя из настроек гравити расставить элементы
        */
        int height = 0;
        int lineWidth = 0;
        int childCount = getChildCount();
        mWidthList.clear();
        int widthSize = getMeasuredWidth();
        for (int i = 0; i < childCount; ++i) {
            View v = getChildAt(i);
            if (v.getVisibility() == View.GONE)
                continue;
            LayoutParams lp = (LayoutParams)v.getLayoutParams();
            int childWidth = v.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = v.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            if (lineWidth + childWidth > widthSize) {  // Новая линия
                mWidthList.add(lineWidth);
                lineWidth = childWidth;
                height += childHeight;
            } else {
                lineWidth += childWidth;
                height = Math.max(height, childHeight);
            }
        }
        mWidthList.add(lineWidth);

        int verticalGravityMargin = 0;
        switch(mGravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.BOTTOM:
                verticalGravityMargin = getHeight() - height;
                break;
            case Gravity.CENTER_VERTICAL:
                verticalGravityMargin = (getHeight() - height) / 2;
                break;
            case Gravity.TOP:
            default:
                break;
        }

        int globalWidth = getMeasuredWidth();
        lineWidth = 0;
        int top = verticalGravityMargin;
        int left = 0;
        int currentLineWidth;
        int lineCnt = 0;
        int t;
        for (int i = 0; i < childCount; ++i) {
            View v = getChildAt(i);
            if (v.getVisibility() == View.GONE)
                continue;
            LayoutParams lp = (LayoutParams)v.getLayoutParams();
            int childWidth = v.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = v.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            int l = left;

            if (i == 0 || (lineWidth + childWidth) > widthSize) {  // Новая линия
                lineWidth = childWidth;
                currentLineWidth = mWidthList.get(lineCnt);
                ++lineCnt;
                int lineHorizontalGravityMargin = 0;
                switch (mGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.CENTER_HORIZONTAL:
                        lineHorizontalGravityMargin = (globalWidth - currentLineWidth) / 2;
                        break;
                    case Gravity.RIGHT:
                        lineHorizontalGravityMargin = globalWidth - currentLineWidth;
                        break;
                    case Gravity.LEFT:
                    default:
                        break;
                }
                left = lineHorizontalGravityMargin;
                l = left;
                left += childWidth;
                top += childHeight;
            } else {
                left += childWidth;
                lineWidth += childWidth;
            }
            t = top - childHeight;
            v.layout(l+lp.leftMargin, t+lp.topMargin, l+childWidth-lp.rightMargin, t+childHeight-lp.bottomMargin);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void setGravity(int gravity) {
        if(mGravity != gravity) {
            mGravity = gravity;
            requestLayout();
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new FlowLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }


    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }
    }

}