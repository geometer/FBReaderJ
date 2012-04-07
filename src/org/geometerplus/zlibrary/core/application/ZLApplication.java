/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.core.application;

import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.util.ZLBoolean3;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;

public abstract class ZLApplication {
	public static ZLApplication Instance() {
		return ourInstance;
	}

	private static ZLApplication ourInstance;

	public static final String NoAction = "none";

	private volatile ZLApplicationWindow myWindow;
	private volatile ZLView myView;

	private final HashMap<String,ZLAction> myIdToActionMap = new HashMap<String,ZLAction>();

	protected ZLApplication() {
		ourInstance = this;
	}

	protected final void setView(ZLView view) {
		if (view != null) {
			myView = view;
			final ZLViewWidget widget = getViewWidget();
			if (widget != null) {
				widget.reset();
				widget.repaint();
			}
			onViewChanged();
		}
	}

	public final ZLView getCurrentView() {
		return myView;
	}

	final void setWindow(ZLApplicationWindow window) {
		myWindow = window;
	}

	public final void initWindow() {
		setView(myView);
	}

	protected void setTitle(String title) {
		if (myWindow != null) {
			myWindow.setTitle(title);
		}
	}

	protected void runWithMessage(String key, Runnable runnable, Runnable postAction) {
		if (myWindow != null) {
			myWindow.runWithMessage(key, runnable, postAction);
		}
	}

	protected void processException(Exception e) {
		if (myWindow != null) {
			myWindow.processException(e);
		}
	}

	public final ZLViewWidget getViewWidget() {
		return myWindow != null ? myWindow.getViewWidget() : null;
	}

	public final void onRepaintFinished() {
		if (myWindow != null) {
			myWindow.refresh();
		}
		for (PopupPanel popup : popupPanels()) {
			popup.update();
		}
	}

	public final void onViewChanged() {
		hideActivePopup();
	}

	public final void hideActivePopup() {
		if (myActivePopup != null) {
			myActivePopup.hide_();
			myActivePopup = null;
		}
	}

	public final void showPopup(String id) {
		hideActivePopup();
		myActivePopup = myPopups.get(id);
		if (myActivePopup != null) {
			myActivePopup.show_();
		}
	}

	public final void addAction(String actionId, ZLAction action) {
		myIdToActionMap.put(actionId, action);
	}

	public final void removeAction(String actionId) {
		myIdToActionMap.remove(actionId);
	}

	public final boolean isActionVisible(String actionId) {
		final ZLAction action = myIdToActionMap.get(actionId);
		return action != null && action.isVisible();
	}

	public final boolean isActionEnabled(String actionId) {
		final ZLAction action = myIdToActionMap.get(actionId);
		return action != null && action.isEnabled();
	}

	public final ZLBoolean3 isActionChecked(String actionId) {
		final ZLAction action = myIdToActionMap.get(actionId);
		return action != null ? action.isChecked() : ZLBoolean3.B3_UNDEFINED;
	}

	public final void runAction(String actionId, Object ... params) {
		final ZLAction action = myIdToActionMap.get(actionId);
		if (action != null) {
			action.checkAndRun(params);
		}
	}

	//may be protected
	abstract public ZLKeyBindings keyBindings();

	public final boolean hasActionForKey(int key, boolean longPress) {
		final String actionId = keyBindings().getBinding(key, longPress);
		return actionId != null && !NoAction.equals(actionId);	
	}

	public final boolean runActionByKey(int key, boolean longPress) {
		final String actionId = keyBindings().getBinding(key, longPress);
		if (actionId != null) {
			final ZLAction action = myIdToActionMap.get(actionId);
			return action != null && action.checkAndRun();
		}
		return false;
	}

	public boolean closeWindow() {
		onWindowClosing();
		if (myWindow != null) {
			myWindow.close();
		}
		return true;
	}

	public void onWindowClosing() {
	}

	public abstract void openFile(ZLFile file, Runnable postAction);

	//Action
	static abstract public class ZLAction {
		public boolean isVisible() {
			return true;
		}

		public boolean isEnabled() {
			return isVisible();
		}

		public ZLBoolean3 isChecked() {
			return ZLBoolean3.B3_UNDEFINED;
		}

		public final boolean checkAndRun(Object ... params) {
			if (isEnabled()) {
				run(params);
				return true;
			}
			return false;
		}

		abstract protected void run(Object ... params);
	}

	public static abstract class PopupPanel {
		protected final ZLApplication Application;

		protected PopupPanel(ZLApplication application) {
			application.myPopups.put(getId(), this);
			Application = application;
		}

		abstract public String getId();
		abstract protected void update();
		abstract protected void hide_();
		abstract protected void show_();
	}

	private final HashMap<String,PopupPanel> myPopups = new HashMap<String,PopupPanel>();
	private PopupPanel myActivePopup;
	public final Collection<PopupPanel> popupPanels() {
		return myPopups.values();
	}
	public final PopupPanel getActivePopup() {
		return myActivePopup;
	}
	public final PopupPanel getPopupById(String id) {
		return myPopups.get(id);
	}

	public int getBatteryLevel() {
		return (myWindow != null) ? myWindow.getBatteryLevel() : 0;
	}

	private volatile Timer myTimer;
	private final HashMap<Runnable,Long> myTimerTaskPeriods = new HashMap<Runnable,Long>();
	private final HashMap<Runnable,TimerTask> myTimerTasks = new HashMap<Runnable,TimerTask>();
	private static class MyTimerTask extends TimerTask {
		private final Runnable myRunnable;

		MyTimerTask(Runnable runnable) {
			myRunnable = runnable;
		}

		public void run() {
			myRunnable.run();
		}
	}

	private void addTimerTaskInternal(Runnable runnable, long periodMilliseconds) {
		final TimerTask task = new MyTimerTask(runnable);
		myTimer.schedule(task, periodMilliseconds / 2, periodMilliseconds);
		myTimerTasks.put(runnable, task);
	}

	private final Object myTimerLock = new Object();
	public final void startTimer() {
		synchronized (myTimerLock) {
			if (myTimer == null) {
				myTimer = new Timer();
				for (Map.Entry<Runnable,Long> entry : myTimerTaskPeriods.entrySet()) {
					addTimerTaskInternal(entry.getKey(), entry.getValue());
				}
			}
		}
	}

	public final void stopTimer() {
		synchronized (myTimerLock) {
			if (myTimer != null) {
				myTimer.cancel();
				myTimer = null;
				myTimerTasks.clear();
			}
		}
	}

	public final void addTimerTask(Runnable runnable, long periodMilliseconds) {
		synchronized (myTimerLock) {
			removeTimerTask(runnable);
			myTimerTaskPeriods.put(runnable, periodMilliseconds);
			if (myTimer != null) {
				addTimerTaskInternal(runnable, periodMilliseconds);
			}
		}
	}	

	public final void removeTimerTask(Runnable runnable) {
		synchronized (myTimerLock) {
			TimerTask task = myTimerTasks.get(runnable);
			if (task != null) {
				task.cancel();
				myTimerTasks.remove(runnable);
			}
			myTimerTaskPeriods.remove(runnable);
		}
	}
}
