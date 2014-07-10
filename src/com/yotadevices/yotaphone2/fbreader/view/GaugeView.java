package com.yotadevices.yotaphone2.fbreader.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.FBHyperlinkType;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.options.ColorProfile;
import org.geometerplus.fbreader.fbreader.options.FooterOptions;
import org.geometerplus.fbreader.fbreader.options.ViewOptions;
import org.geometerplus.zlibrary.core.fonts.FontEntry;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlink;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidPaintContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GaugeView extends View {
    private Bitmap mGaugeBitmap;
    private FBReaderApp mReader;
    private ViewOptions mViewOptions;
    private Footer mFooter;
    private final Paint myPaint = new Paint();

    private class Footer implements ZLView.FooterArea {
        private Runnable UpdateTask = new Runnable() {
            public void run() {
                if (!mViewOptions.YotaDrawOnBackScreen.getValue()) {
                    mReader.getViewWidget().repaint();
                }
            }
        };

        private ArrayList<TOCTree> myTOCMarks;

        public int getHeight() {
            return GaugeView.this.getHeight();
        }

        public synchronized void resetTOCMarks() {
            myTOCMarks = null;
        }

        private final int MAX_TOC_MARKS_NUMBER = 100;
        private synchronized void updateTOCMarks(BookModel model) {
            myTOCMarks = new ArrayList<TOCTree>();
            TOCTree toc = model.TOCTree;
            if (toc == null) {
                return;
            }
            int maxLevel = Integer.MAX_VALUE;
            if (toc.getSize() >= MAX_TOC_MARKS_NUMBER) {
                final int[] sizes = new int[10];
                for (TOCTree tocItem : toc) {
                    if (tocItem.Level < 10) {
                        ++sizes[tocItem.Level];
                    }
                }
                for (int i = 1; i < sizes.length; ++i) {
                    sizes[i] += sizes[i - 1];
                }
                for (maxLevel = sizes.length - 1; maxLevel >= 0; --maxLevel) {
                    if (sizes[maxLevel] < MAX_TOC_MARKS_NUMBER) {
                        break;
                    }
                }
            }
            for (TOCTree tocItem : toc.allSubtrees(maxLevel)) {
                myTOCMarks.add(tocItem);
            }
        }

        private List<FontEntry> myFontEntry;
        public synchronized void paint(ZLPaintContext context) {
            ZLColor backgroundColor = getBackgroundColor();
            context.clear(backgroundColor);

            final BookModel model = mReader.Model;
            if (model == null) {
                return;
            }

            final FooterOptions footerOptions = mViewOptions.getFooterOptions();
            final ZLColor fgColor = getTextColor(ZLTextHyperlink.NO_LINK);
            final ZLColor fillColor = mViewOptions.getColorProfile().FooterFillOption.getValue();

            final int left = 0;
            final int right = context.getWidth();
            final int height = getHeight();
            final int lineWidth = height <= 10 ? 1 : 2;
            final int delta = height <= 10 ? 0 : 1;
            final String family = footerOptions.Font.getValue();
            if (myFontEntry == null || !family.equals(myFontEntry.get(0).Family)) {
                myFontEntry = Collections.singletonList(FontEntry.systemEntry(family));
            }
            context.setFont(
                    myFontEntry,
                    height <= 10 ? height + 3 : height + 1,
                    height > 10, false, false, false
            );
            final ZLTextView textView = mReader.getTextView();
            final ZLTextView.PagePosition pagePosition = textView.pagePosition();

            // draw gauge
            final int gaugeRight = right;
            myGaugeWidth = gaugeRight - left - 2 * lineWidth;

            context.setLineColor(fgColor);
            context.setLineWidth(lineWidth);
            context.drawLine(left, lineWidth, left, height - lineWidth);
            context.drawLine(left, height - lineWidth, gaugeRight, height - lineWidth);
            context.drawLine(gaugeRight, height - lineWidth, gaugeRight, lineWidth);
            context.drawLine(gaugeRight, lineWidth, left, lineWidth);

            final int gaugeInternalRight =
                    left + lineWidth + (int)(1.0 * myGaugeWidth * pagePosition.Current / pagePosition.Total);

            context.setFillColor(fillColor);
            context.fillRectangle(left + 1, height - 2 * lineWidth, gaugeInternalRight, lineWidth + 1);

            if (footerOptions.ShowTOCMarks.getValue()) {
                if (myTOCMarks == null) {
                    updateTOCMarks(model);
                }
                final int fullLength = sizeOfFullText(textView);
                for (TOCTree tocItem : myTOCMarks) {
                    TOCTree.Reference reference = tocItem.getReference();
                    if (reference != null) {
                        final int refCoord = sizeOfTextBeforeParagraph(textView, reference.ParagraphIndex);
                        final int xCoord =
                                left + 2 * lineWidth + (int)(1.0 * myGaugeWidth * refCoord / fullLength);
                        context.drawLine(xCoord, height - lineWidth, xCoord, lineWidth);
                    }
                }
            }
        }

        // TODO: remove
        int myGaugeWidth = 1;
		/*public int getGaugeWidth() {
			return myGaugeWidth;
		}*/

		/*public void setProgress(int x) {
			// set progress according to tap coordinate
			int gaugeWidth = getGaugeWidth();
			float progress = 1.0f * Math.min(x, gaugeWidth) / gaugeWidth;
			int page = (int)(progress * computePageNumber());
			if (page <= 1) {
				gotoHome();
			} else {
				gotoPage(page);
			}
			myReader.getViewWidget().reset();
			myReader.getViewWidget().repaint();
		}*/
    }

    public GaugeView(Context context) {
        super(context);
    }

    public GaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GaugeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init() {
        mReader = (FBReaderApp) FBReaderApp.Instance();
        mViewOptions = mReader.ViewOptions;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mFooter = getFooterArea();
        if (mGaugeBitmap != null &&
                (mGaugeBitmap.getWidth() != getWidth() || mGaugeBitmap.getHeight() != getHeight())) {
            mGaugeBitmap = null;
        }
        if (mGaugeBitmap == null) {
            mGaugeBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
        }
        final ZLAndroidPaintContext context = new ZLAndroidPaintContext(
                createCanvas(mGaugeBitmap),
                getWidth(),
                getHeight(),
                0
        );

        mFooter.paint(context);
        canvas.drawBitmap(mGaugeBitmap, 0, 0, myPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mGaugeBitmap = Bitmap.createBitmap(getContext().getResources().getDisplayMetrics(),
                getWidth(), getHeight(), Bitmap.Config.RGB_565);
    }

    public Footer getFooterArea() {
        if (mFooter == null) {
            mFooter = new Footer();
            //mReader.addTimerTask(mFooter.UpdateTask, 15000);
        }
        return mFooter;
    }

    protected Canvas createCanvas(Bitmap bitmap) {
        return new Canvas(bitmap);
    }

    public ZLColor getBackgroundColor() {
        return mViewOptions.getColorProfile().BackgroundOption.getValue();
    }

    public ZLColor getSelectionBackgroundColor() {
        return mViewOptions.getColorProfile().SelectionBackgroundOption.getValue();
    }

    public ZLColor getSelectionForegroundColor() {
        return mViewOptions.getColorProfile().SelectionForegroundOption.getValue();
    }

    public ZLColor getTextColor(ZLTextHyperlink hyperlink) {
        final ColorProfile profile = mViewOptions.getColorProfile();
        if (mViewOptions.YotaDrawOnBackScreen.getValue()) {
            return profile.RegularTextOption.getValue();
        }
        switch (hyperlink.Type) {
            default:
            case FBHyperlinkType.NONE:
                return profile.RegularTextOption.getValue();
            case FBHyperlinkType.INTERNAL:
                return mReader.Collection.isHyperlinkVisited(mReader.Model.Book, hyperlink.Id)
                        ? profile.VisitedHyperlinkTextOption.getValue()
                        : profile.HyperlinkTextOption.getValue();
            case FBHyperlinkType.EXTERNAL:
                return profile.HyperlinkTextOption.getValue();
        }
    }
    protected final synchronized int sizeOfFullText(ZLTextView textView) {
        if (textView.getModel() == null || textView.getModel().getParagraphsNumber() == 0) {
            return 1;
        }
        return textView.getModel().getTextLength(textView.getModel().getParagraphsNumber() - 1);
    }

    protected final synchronized int sizeOfTextBeforeParagraph(ZLTextView textView, int paragraphIndex) {
        return textView.getModel() != null ? textView.getModel().getTextLength(paragraphIndex - 1) : 0;
    }
}
