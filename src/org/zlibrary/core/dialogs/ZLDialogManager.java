package org.zlibrary.core.dialogs;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.application.ZLApplicationWindow;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.runnable.ZLRunnable;

public abstract class ZLDialogManager {
	protected static ZLDialogManager ourInstance;
	
	public static final String OK_BUTTON = "ok";
	public static final String CANCEL_BUTTON = "cancel";
	public static final String YES_BUTTON = "yes";
	public static final String NO_BUTTON = "no";
	public static final String APPLY_BUTTON = "apply";
	
	public static final String COLOR_KEY = "color";
	public static final String DIALOG_TITLE = "title";
	
	protected ZLDialogManager() {
		ourInstance = this;
	}
	
	public static ZLDialogManager getInstance() {
		return ourInstance;
	} 
	
	public abstract boolean runSelectionDialog(String key, ZLTreeHandler handler);

	public abstract void showInformationBox(String key, String message);

	public final void showInformationBox(String key) {
		showInformationBox(key, getDialogMessage(key));
	}
	
	public abstract void showErrorBox(String key, String message);
	
	public final void showErrorBox(String key) {
		showErrorBox(key, getDialogMessage(key));
	}
	
	public abstract ZLApplicationWindow createApplicationWindow(ZLApplication application);
	
	public abstract ZLOptionsDialog createOptionsDialog(String key, ZLRunnable applyAction, boolean showApplyButton);
	
	public final ZLOptionsDialog createOptionsDialog(String key) {
		return createOptionsDialog(key, null, false);
	}
	
	public static String getButtonText(String key) {
		return getResource().getResource("button").getResource(key).getValue();
	}

	public static String getDialogMessage(String key) {
		return getResource().getResource(key).getResource("message").getValue();
	}
	
	public static String getDialogTitle(String key) {
		return getResource().getResource(key).getResource("title").getValue();
	}
	
	public final int questionBox(String key, String button0, String button1, String button2) {
		return questionBox(key, getDialogMessage(key), button0, button1, button2);
	}
	
	public abstract int questionBox(String key, String message, String button0, String button1, String button2);
	
	protected static ZLResource getResource() {
		return ZLResource.resource("dialog");
	}
}


/*
 * 
 * public:

	
	static const std::string &buttonName(const ZLResourceKey &key);
	static const std::string &waitMessageText(const ZLResourceKey &key);

public:
	

	virtual shared_ptr<ZLDialog> createDialog(const ZLResourceKey &key) const = 0;
	
	virtual void wait(const ZLResourceKey &key, ZLRunnable &runnable) const = 0;

	interface ClipboardType {
		CLIPBOARD_MAIN,
		CLIPBOARD_SELECTION
	};
	virtual bool isClipboardSupported(ClipboardType type) const = 0;
	virtual void setClipboardText(const std::string &text, ClipboardType type) const = 0;
 * 
 */
