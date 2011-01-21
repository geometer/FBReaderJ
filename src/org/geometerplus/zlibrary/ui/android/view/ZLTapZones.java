package org.geometerplus.zlibrary.ui.android.view;

import java.util.ArrayList;

import org.geometerplus.fbreader.bookmodel.FBHyperlinkType;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.ScrollingPreferences;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

public class ZLTapZones {
	Paint myLinesPaint = new Paint();
	Paint myFillPaint = new Paint();
	Paint myGuidesPaint = new Paint();
	private ArrayList<ZLTapZone> myZones = new ArrayList<ZLTapZone>();
	private ArrayList<ZLTapZone> myZonesBkp = new ArrayList<ZLTapZone>();
	private ZLResource myActionsRes = ZLResource.resource("actions");

	private Rect myWatchArea = new Rect();
	private int myRulerSize = 16;
	private boolean myMoveInProgress = false;
	public ZLTapZone mySelectedZone;

	private final int ZONE_TOUCH_NONE = 0;
	private final int ZONE_TOUCH_CENTER = 1;
	private final int ZONE_TOUCH_LEFT_TOP = 2;
	private final int ZONE_TOUCH_RIGHT_TOP = 3;
	private final int ZONE_TOUCH_LEFT_BOTTOM = 4;
	private final int ZONE_TOUCH_RIGHT_BOTTOM = 5;

	public class ZLTapZone {
		String myAction;

		// zone's rectangle, each value is relative screen dimension from 0 to 1.0
		private float myLeft;
		private float myRight;
		private float myTop;
		private float myBottom;

		private int myCatchX;
		private int myCatchY;
		private int myCatchType;

		public Rect myViewRect = new Rect();

		ZLTapZone(String action, float left, float top, float right, float bottom) {
			myAction = action;
			myLeft = left;
			myTop = top;
			myRight = right;
			myBottom = bottom;
		}

		ZLTapZone cloneZone() {
			return new ZLTapZone(myAction, myLeft, myTop, myRight, myBottom);
		}

		void loadViewRect() {
			myViewRect.left		= toViewX(myLeft);
			myViewRect.top		= toViewY(myTop);
			myViewRect.right	= toViewX(myRight);
			myViewRect.bottom	= toViewY(myBottom);
		}

		public String getAction() {
			return myAction;
		}

		public void selectAction(){
			String[] actions = ZLApplication.Instance().getGetSimpleActions();
			final String items[] = new String[actions.length];
			int selectedActionIndex = -1;
			for (int actionIndex = 0; actionIndex < actions.length; actionIndex++) {
				items[actionIndex] = myActionsRes.getResource(actions[actionIndex]).getValue();
				if (myAction.equals(actions[actionIndex])) {
					selectedActionIndex = actionIndex;
				}
			}

			AlertDialog.Builder dialog = new AlertDialog.Builder(
				ZLAndroidApplication.Instance().myMainActivity);
			dialog.setSingleChoiceItems(items, selectedActionIndex,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton){
						myAction = ZLApplication.Instance().getGetSimpleActions()[whichButton];
						dialog.dismiss();
					}
				});
			dialog.show();
		}

		void saveViewRect() {
			myLeft		= fromViewX(myViewRect.left);
			myTop		= fromViewY(myViewRect.top);
			myRight		= fromViewX(myViewRect.right);
			myBottom	= fromViewY(myViewRect.bottom);
		}

		void draw(Canvas canvas) {
			int delta1 = myRulerSize;
			int delta2 = myRulerSize / 2;
			int delta3 = delta1 + delta2;

			if (myViewRect.width() < 3 * myRulerSize || myViewRect.height() < 3 * myRulerSize) {
				canvas.drawRect(myViewRect, myLinesPaint);
				if (myViewRect.width() > myRulerSize && myViewRect.height() > myRulerSize) {
					canvas.drawRect(myViewRect.left + delta2, myViewRect.top + delta2,
							myViewRect.right - delta2, myViewRect.bottom - delta2, myFillPaint);
				}
				return;
			}

			// corner rulers
			canvas.drawRect(myViewRect.left, myViewRect.top,
					myViewRect.left + delta1, myViewRect.top + delta1, myLinesPaint);
			canvas.drawRect(myViewRect.right - delta1, myViewRect.top,
					myViewRect.right, myViewRect.top + delta1, myLinesPaint);
			canvas.drawRect(myViewRect.left, myViewRect.bottom - delta1,
					myViewRect.left + delta1, myViewRect.bottom, myLinesPaint);
			canvas.drawRect(myViewRect.right - delta1, myViewRect.bottom - delta1,
					myViewRect.right, myViewRect.bottom, myLinesPaint);

			// lines between rulers
			canvas.drawLine(myViewRect.left + delta1, myViewRect.top + delta2,
					myViewRect.right - delta1, myViewRect.top + delta2, myLinesPaint);
			canvas.drawLine(myViewRect.left + delta1, myViewRect.bottom - delta2,
					myViewRect.right - delta1, myViewRect.bottom - delta2, myLinesPaint);
			canvas.drawLine(myViewRect.left + delta2, myViewRect.top + delta1,
					myViewRect.left + delta2, myViewRect.bottom - delta1, myLinesPaint);
			canvas.drawLine(myViewRect.right - delta2, myViewRect.top + delta1,
					myViewRect.right - delta2, myViewRect.bottom - delta1, myLinesPaint);

			canvas.drawLine(myViewRect.left + delta3, myViewRect.top,
					myViewRect.right - delta3, myViewRect.top, myLinesPaint);
			canvas.drawLine(myViewRect.left + delta3, myViewRect.bottom,
					myViewRect.right - delta3, myViewRect.bottom, myLinesPaint);
			canvas.drawLine(myViewRect.left, myViewRect.top + delta3,
					myViewRect.left, myViewRect.bottom - delta3, myLinesPaint);
			canvas.drawLine(myViewRect.right, myViewRect.top + delta3,
					myViewRect.right, myViewRect.bottom - delta3, myLinesPaint);

			// center transparent area
			canvas.drawRect(myViewRect.left + delta1, myViewRect.top + delta1,
					myViewRect.right - delta1, myViewRect.bottom - delta1, myFillPaint);
		}

		private void ensureWatchArea(Point delta, int left, int top, int right, int bottom) {
			if (left + delta.x < myWatchArea.left) {
				delta.x = myWatchArea.left - left;
			}
			if (right + delta.x > myWatchArea.right) {
				delta.x = myWatchArea.right - right;
			}
			if (top + delta.y < myWatchArea.top) {
				delta.y = myWatchArea.top - top;
			}
			if (bottom + delta.y > myWatchArea.bottom) {
				delta.y = myWatchArea.bottom - bottom;
			}
		}

		private void updateCatch(int x, int y, Rect rect) {
			rect.sort();
			if (x == rect.left) {
				if (y == rect.top) {
					myCatchType = ZONE_TOUCH_LEFT_TOP;
				}
				if (y == rect.bottom) {
					myCatchType = ZONE_TOUCH_LEFT_BOTTOM;
				}
			}
			if (x == rect.right) {
				if (y == rect.top) {
					myCatchType = ZONE_TOUCH_RIGHT_TOP;
				}
				if (y == rect.bottom) {
					myCatchType = ZONE_TOUCH_RIGHT_BOTTOM;
				}
			}
		}

		public boolean change(int x, int y) {
			Point delta = new Point(x - myCatchX, y - myCatchY);
			Rect newRect = new Rect(myViewRect);
			switch (myCatchType) {
				case ZONE_TOUCH_CENTER:
					ensureWatchArea(delta, newRect.left, newRect.top, newRect.right, newRect.bottom);
					newRect.offset(delta.x, delta.y);
					break;
				case ZONE_TOUCH_LEFT_TOP:
					ensureWatchArea(delta, newRect.left, newRect.top, newRect.left, newRect.top);
					newRect.left += delta.x;
					newRect.top += delta.y;
					updateCatch(newRect.left, newRect.top, newRect);
					break;
				case ZONE_TOUCH_RIGHT_TOP:
					ensureWatchArea(delta, newRect.right, newRect.top, newRect.right, newRect.top);
					newRect.right += delta.x;
					newRect.top += delta.y;
					updateCatch(newRect.right, newRect.top, newRect);
					break;
				case ZONE_TOUCH_LEFT_BOTTOM:
					ensureWatchArea(delta, newRect.left, newRect.bottom, newRect.left, newRect.bottom);
					newRect.left += delta.x;
					newRect.bottom += delta.y;
					updateCatch(newRect.left, newRect.bottom, newRect);
					break;
				case ZONE_TOUCH_RIGHT_BOTTOM:
					ensureWatchArea(delta, newRect.right, newRect.bottom, newRect.right, newRect.bottom);
					newRect.right += delta.x;
					newRect.bottom += delta.y;
					updateCatch(newRect.right, newRect.bottom, newRect);
					break;
			}

			myCatchX += delta.x;
			myCatchY += delta.y;
			if (myViewRect.equals(newRect)) {
				return false;
			}

			myViewRect.set(newRect);
			return true;
		}

		public int isTouched(int x, int y) {
			Rect rulerRect = new Rect(myViewRect);
			rulerRect.inset(-myRulerSize, -myRulerSize);//myViewRect.sort()
			int delta = 2 * myRulerSize;
			if (rulerRect.contains(x, y)) {
				if (y - myViewRect.top < delta) {
					if (x - myViewRect.left < delta) {
						return ZONE_TOUCH_LEFT_TOP;
					}
					if (myViewRect.right - x < delta) {
						return ZONE_TOUCH_RIGHT_TOP;
					}
				}
				if (myViewRect.bottom - y < delta) {
					if (x - myViewRect.left < delta) {
						return ZONE_TOUCH_LEFT_BOTTOM;
					}
					if (myViewRect.right - x < delta) {
						return ZONE_TOUCH_RIGHT_BOTTOM;
					}
				}

				if (myViewRect.contains(x, y)) {
					return ZONE_TOUCH_CENTER;
				}
			}
			return ZONE_TOUCH_NONE;
		}

		public boolean tryToCatch(int x, int y) {
			myCatchType = isTouched(x, y);
			if (myCatchType != ZONE_TOUCH_NONE) {
				myCatchX = x;
				myCatchY = y;
				return true;
			}
			return false;
		}

		public void saveToDB(SQLiteStatement insert) {
			insert.bindString(1, myAction);
			insert.bindDouble(2, myLeft);
			insert.bindDouble(3, myTop);
			insert.bindDouble(4, myRight);
			insert.bindDouble(5, myBottom);
			insert.execute();
		}

		public void release(){
		}
	}

	int toViewX(float x) {
		return myWatchArea.left + (int)(((float)myWatchArea.width()) * x + 0.5f);
	}

	int toViewY(float y) {
		return myWatchArea.top + (int)(((float)myWatchArea.height()) * y + 0.5f);
	}

	float fromViewX(int x) {
		return (float)(x - myWatchArea.left)/(float)myWatchArea.width();
	}

	float fromViewY(int y) {
		return (float)(y - myWatchArea.top)/(float)myWatchArea.height();
	}

	public ZLTapZones() {
		myLinesPaint.setStyle(Paint.Style.STROKE);
		myLinesPaint.setStrokeWidth(3);
		myFillPaint.setStyle(Paint.Style.FILL);
		loadZonesFromDB();
		if(myZones.size() == 0) {
			final ScrollingPreferences preferences = ScrollingPreferences.Instance();
			if (preferences.HorizontalOption.getValue()) {
				addZone("previousPage", 0, 0, 0.5f, 1);
				addZone("nextPage", 0.5f, 0, 1, 1);
			}
			else {
				addZone("previousPage", 0, 0, 1, 0.5f);
				addZone("nextPage", 0, 0.5f, 1, 1);
			}
		}
	}

	private void addZone(String action, float left, float top, float right, float bottom) {
		ZLTapZone zone = new ZLTapZone(action, left, top, right, bottom);
		zone.loadViewRect();
		myZones.add(0, zone);
	}

	public void draw(Canvas canvas) {
		FBReaderApp reader = (FBReaderApp)FBReaderApp.Instance();
		int color = ZLAndroidColorUtil.rgb(reader.BookTextView.getTextColor(FBHyperlinkType.INTERNAL));
		myLinesPaint.setColor(color);
		myFillPaint.setColor(color);
		myFillPaint.setAlpha(100);
		for (int zoneIndex = 0; zoneIndex < myZones.size(); zoneIndex++) {
			myZones.get(zoneIndex).draw(canvas);
		}
		drawGuideLines(canvas);
	}

	private void drawGuideLines(Canvas canvas) {
		FBReaderApp reader = (FBReaderApp)FBReaderApp.Instance();
		myGuidesPaint.setColor(ZLAndroidColorUtil.rgb(reader.BookTextView.getTextColor(FBHyperlinkType.NONE)));
		int dx2 = myWatchArea.width() / 2;
		int dy2 = myWatchArea.height() / 2;
		int dx3 = myWatchArea.width() / 3;
		int dy3 = myWatchArea.height() / 3;
		int dx8 = myWatchArea.width() / 8;
		int dy8 = myWatchArea.height() / 8;
		int dx16 = myWatchArea.width() / 16;
		int dy16 = myWatchArea.height() / 16;

		// middle horizontal lines
		canvas.drawLine(myWatchArea.left, myWatchArea.top + dy2,
				myWatchArea.left + dx8, myWatchArea.top + dy2, myGuidesPaint);
		canvas.drawLine(myWatchArea.left + dx3, myWatchArea.top + dy2,
				myWatchArea.right - dx3, myWatchArea.top + dy2, myGuidesPaint);
		canvas.drawLine(myWatchArea.right - dx8, myWatchArea.top + dy2,
				myWatchArea.right, myWatchArea.top + dy2, myGuidesPaint);

		// middle vertical lines
		canvas.drawLine(myWatchArea.left + dx2, myWatchArea.top,
				myWatchArea.left + dx2, myWatchArea.top + dy8, myGuidesPaint);
		canvas.drawLine(myWatchArea.left + dx2, myWatchArea.top + dy3,
				myWatchArea.left + dx2, myWatchArea.bottom - dy3, myGuidesPaint);
		canvas.drawLine(myWatchArea.left + dx2, myWatchArea.bottom - dy8,
				myWatchArea.left + dx2, myWatchArea.bottom, myGuidesPaint);

		// third 1st horizontal lines
		canvas.drawLine(myWatchArea.left, myWatchArea.top + dy3,
				myWatchArea.left + dx16, myWatchArea.top + dy3, myGuidesPaint);
		canvas.drawLine(myWatchArea.left + dx3 - dx16, myWatchArea.top + dy3,
				myWatchArea.left + dx3 + dx16, myWatchArea.top + dy3, myGuidesPaint);
		canvas.drawLine(myWatchArea.right - dx3 - dx16, myWatchArea.top + dy3,
				myWatchArea.right - dx3 + dx16, myWatchArea.top + dy3, myGuidesPaint);
		canvas.drawLine(myWatchArea.right - dx16, myWatchArea.top + dy3,
				myWatchArea.right, myWatchArea.top + dy3, myGuidesPaint);

		// third 2nd horizontal lines
		canvas.drawLine(myWatchArea.left, myWatchArea.bottom - dy3,
				myWatchArea.left + dx16, myWatchArea.bottom - dy3, myGuidesPaint);
		canvas.drawLine(myWatchArea.left + dx3 - dx16, myWatchArea.bottom - dy3,
				myWatchArea.left + dx3 + dx16, myWatchArea.bottom - dy3, myGuidesPaint);
		canvas.drawLine(myWatchArea.right - dx3 - dx16, myWatchArea.bottom - dy3,
				myWatchArea.right - dx3 + dx16, myWatchArea.bottom - dy3, myGuidesPaint);
		canvas.drawLine(myWatchArea.right - dx16, myWatchArea.bottom - dy3,
				myWatchArea.right, myWatchArea.bottom - dy3, myGuidesPaint);

		// third 1st vertical lines
		canvas.drawLine(myWatchArea.left + dx3, myWatchArea.top,
				myWatchArea.left + dx3, myWatchArea.top + dy16, myGuidesPaint);
		canvas.drawLine(myWatchArea.left + dx3, myWatchArea.top + dy3 + dy16,
				myWatchArea.left + dx3, myWatchArea.top + dy3 - dy16, myGuidesPaint);
		canvas.drawLine(myWatchArea.left + dx3, myWatchArea.bottom - dy3 + dy16,
				myWatchArea.left + dx3, myWatchArea.bottom - dy3 - dy16, myGuidesPaint);
		canvas.drawLine(myWatchArea.left + dx3, myWatchArea.bottom - dy16,
				myWatchArea.left + dx3, myWatchArea.bottom, myGuidesPaint);

		// third 2nd vertical lines
		canvas.drawLine(myWatchArea.right - dx3, myWatchArea.top,
				myWatchArea.right - dx3, myWatchArea.top + dy16, myGuidesPaint);
		canvas.drawLine(myWatchArea.right - dx3, myWatchArea.top + dy3 + dy16,
				myWatchArea.right - dx3, myWatchArea.top + dy3 - dy16, myGuidesPaint);
		canvas.drawLine(myWatchArea.right - dx3, myWatchArea.bottom - dy3 + dy16,
				myWatchArea.right - dx3, myWatchArea.bottom - dy3 - dy16, myGuidesPaint);
		canvas.drawLine(myWatchArea.right - dx3, myWatchArea.bottom - dy16,
				myWatchArea.right - dx3, myWatchArea.bottom, myGuidesPaint);
	}

	public void setWatchArea(int x, int y, int width, int height) {
		myWatchArea.set(x, y, x + width, y + height);
		myRulerSize = (int)(Math.sqrt(myWatchArea.width() * myWatchArea.width() + myWatchArea.height() * myWatchArea.height()) / 38 + 0.5);
		for (int zoneIndex = 0; zoneIndex < myZones.size(); zoneIndex++) {
			myZones.get(zoneIndex).loadViewRect();
		}
	}

	public void addZoneByTap(int x, int y) {
		Rect newZoneRect = new Rect(x - 3 * myRulerSize, y - 3 * myRulerSize, x + 3 * myRulerSize, y + 3 * myRulerSize);
		int dx = (newZoneRect.left < myWatchArea.left ? myWatchArea.left - newZoneRect.left:
			(newZoneRect.right > myWatchArea.right ? myWatchArea.right - newZoneRect.right : 0));
		int dy = (newZoneRect.top < myWatchArea.top ? myWatchArea.top - newZoneRect.top:
			(newZoneRect.bottom > myWatchArea.bottom ? myWatchArea.bottom - newZoneRect.bottom : 0));
		newZoneRect.offset(dx, dy);
		addZone(ActionCode.NOTHING, fromViewX(newZoneRect.left), fromViewY(newZoneRect.top),
			fromViewX(newZoneRect.right), fromViewY(newZoneRect.bottom));
		ZLApplication.Instance().repaintView();
	}

	public void deleteSelectedZone() {
		if (myZones.remove(mySelectedZone)) {
			ZLApplication.Instance().repaintView();
		}
	}

	public boolean onFingerPress(int x, int y){
		if (selectZone(x, y)) {
			// if is pressed over zone - select action
			mySelectedZone.selectAction();
		}
		else {
			// if is pressed over free space - add new zone
			addZoneByTap(x, y);
		}
		return true;
	}

	public boolean onFingerRelease(int x, int y) {
		myMoveInProgress = false;
		if (mySelectedZone != null) {
			mySelectedZone.saveViewRect();
		}
		return true;
	}

	public boolean onFingerMove(int x, int y) {
		// need to detect first move after screen was released by onFingerRelease
		if (!myMoveInProgress) {
			// if first move then try to catch any zone
			myMoveInProgress = true;
			selectZone(x, y);
		}
		else {
			// if continued move, then modify zone and redraw screen
			if (mySelectedZone != null) {
				if (mySelectedZone.change(x, y)) {
					ZLApplication.Instance().repaintView();
				}
			}
		}
		return false;
	}

	public boolean selectZone(int x, int y){
		mySelectedZone = null;
		for (int zoneIndex = 0; zoneIndex < myZones.size(); zoneIndex++) {
			if (myZones.get(zoneIndex).tryToCatch(x, y)) {
				mySelectedZone = myZones.get(zoneIndex);
				// set this zone as first in list
				myZones.set(zoneIndex, myZones.get(0));
				myZones.set(0, mySelectedZone);
				return true;
			}
		}
		return false;
	}

	public void startEdit() {
		myZonesBkp.clear();
		for (int zoneIndex = 0; zoneIndex < myZones.size(); zoneIndex++) {
			myZonesBkp.add(myZones.get(zoneIndex).cloneZone());
		}
	}

	public void saveChanges() {
		myZonesBkp.clear();
		saveZonesToDB();
	}

	public void cancelChanges() {
		myZones.clear();
		for (int zoneIndex = 0; zoneIndex < myZonesBkp.size(); zoneIndex++) {
			ZLTapZone zone = myZonesBkp.get(zoneIndex);
			zone.loadViewRect();
			myZones.add(zone);
		}
		myZonesBkp.clear();
	}

	private SQLiteDatabase getDB() {
		Application app = ZLAndroidApplication.Instance();
		SQLiteDatabase db = app.openOrCreateDatabase("config.db", Context.MODE_PRIVATE, null);
		db.execSQL("CREATE TABLE IF NOT EXISTS TapZones (action TEXT, left FLOAT, top FLOAT, right FLOAT, bottom FLOAT)");
		return db;
	}

	private void saveZonesToDB() {
		SQLiteDatabase db = getDB();
		db.execSQL("DELETE FROM TapZones");
		final SQLiteStatement insert = db.compileStatement("INSERT INTO TapZones (action, left, top, right, bottom) VALUES (?, ?, ?, ?, ?)");
		for (int zoneIndex = 0; zoneIndex < myZones.size(); zoneIndex++) {
			myZones.get(zoneIndex).saveToDB(insert);
		}
	}

	private void loadZonesFromDB() {
		SQLiteDatabase db = getDB();
		Cursor cursor = db.rawQuery("SELECT action, left, top, right, bottom FROM TapZones", null);
		while (cursor.moveToNext()) {
			addZone(cursor.getString(0), (float)cursor.getDouble(1), (float)cursor.getDouble(2),
				(float)cursor.getDouble(3), (float)cursor.getDouble(4));
		}
		cursor.close();
	}

	public boolean doTap(int x, int y) {
		if (selectZone(x, y)) {
			ZLApplication.Instance().doAction(mySelectedZone.myAction);
			return true;
		}
		return false;
	}
}
