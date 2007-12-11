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
	public static final String DIALOG_TITLE = "title";
	
	protected ZLDialogManager() {
	}
	
	public static ZLDialogManager getInstance() {
		return ourInstance;
	} 

	public static boolean isInitialized() {
		return ourInstance != null;
	}

	public static void deleteInstance() {
		ourInstance = null;
	}
	
	public void informationBox(String key) {
		informationBox(key, dialogMessage(key));
	}
	
	public void errorBox(String key) {
		errorBox(key, dialogMessage(key));
	}
	
	public abstract void errorBox(String key, String message);
	
	public abstract void informationBox(String key, String message);
	
	public abstract void createApplicationWindow(ZLApplication application);
	
	public static String dialogMessage(String key) {
		return resource().getResource(key).getResource("message").value();
	}
	
	public static String dialogTitle(String key) {
		return resource().getResource(key).getResource(DIALOG_TITLE).value();
	}
	
	protected static ZLResource resource() {
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

	enum ClipboardType {
		CLIPBOARD_MAIN,
		CLIPBOARD_SELECTION
	};
	virtual bool isClipboardSupported(ClipboardType type) const = 0;
	virtual void setClipboardText(const std::string &text, ClipboardType type) const = 0;
 * 
 */
