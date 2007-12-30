package org.zlibrary.core.dialogs;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.resources.ZLResource;

public abstract class ZLDialogManager {
	protected static ZLDialogManager ourInstance;
	
	public static final String OK_BUTTON = "ok";
	public static final String CANCEL_BUTTON = "cancel";
	public static final String YES_BUTTON = "yes";
	public static final String NO_BUTTON = "no";
	public static final String APPLY_BUTTON = "apply";
	
	public static final String COLOR_KEY = "color";
	
	protected ZLDialogManager() {
		ourInstance = this;
	}
	
	public static ZLDialogManager getInstance() {
		return ourInstance;
	} 
	
	abstract public boolean runSelectionDialog(String key, ZLTreeHandler handler);

	abstract public void showInformationBox(String key, String message);

	public void showInformationBox(String key) {
		showInformationBox(key, getDialogMessage(key));
	}
	
	public abstract void showErrorBox(String key, String message);
	
	public void showErrorBox(String key) {
		showErrorBox(key, getDialogMessage(key));
	}
	
	//public abstract void createApplicationWindow(ZLApplication application);
	
	public static String getButtonText(String key) {
		return getResource().getResource("button").getResource(key).getValue();
	}

	public static String getDialogMessage(String key) {
		return getResource().getResource(key).getResource("message").getValue();
	}
	
	public static String getDialogTitle(String key) {
		return getResource().getResource(key).getResource("title").getValue();
	}
	
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
	virtual shared_ptr<ZLOptionsDialog> createOptionsDialog(const ZLResourceKey &key, shared_ptr<ZLRunnable> applyAction = 0, bool showApplyButton = false) const = 0;
	virtual bool selectionDialog(const ZLResourceKey &key, ZLTreeHandler &handler) const = 0;

	int questionBox(const ZLResourceKey &key, const ZLResourceKey &button0, const ZLResourceKey &button1, const ZLResourceKey &button2 = ZLResourceKey()) const;
	virtual int questionBox(const ZLResourceKey &key, const std::string &message, const ZLResourceKey &button0, const ZLResourceKey &button1, const ZLResourceKey &button2 = ZLResourceKey()) const = 0;

	virtual void wait(const ZLResourceKey &key, ZLRunnable &runnable) const = 0;

	interface ClipboardType {
		CLIPBOARD_MAIN,
		CLIPBOARD_SELECTION
	};
	virtual bool isClipboardSupported(ClipboardType type) const = 0;
	virtual void setClipboardText(const std::string &text, ClipboardType type) const = 0;
 * 
 */
