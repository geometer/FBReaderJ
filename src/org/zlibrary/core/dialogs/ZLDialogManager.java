package org.zlibrary.core.dialogs;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.resources.ZLResourceKey;

public abstract class ZLDialogManager {
	protected static ZLDialogManager ourInstance;
	
	public static final ZLResourceKey OK_BUTTON = new ZLResourceKey("ok");
	public static final ZLResourceKey CANCEL_BUTTON = new ZLResourceKey("cancel");
	public static final ZLResourceKey YES_BUTTON = new ZLResourceKey("yes");
	public static final ZLResourceKey NO_BUTTON = new ZLResourceKey("no");
	public static final ZLResourceKey APPLY_BUTTON = new ZLResourceKey("apply");
	
	public static final ZLResourceKey COLOR_KEY = new ZLResourceKey("color");
	public static final ZLResourceKey DIALOG_TITLE = new ZLResourceKey("title");
	
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
	
	public void informationBox(ZLResourceKey key) {
		informationBox(key, dialogMessage(key));
	}
	
	public void errorBox(ZLResourceKey key) {
		errorBox(key, dialogMessage(key));
	}
	
	public abstract void errorBox(ZLResourceKey key, String message);
	
	public abstract void informationBox(ZLResourceKey key, String message);
	
	public abstract void createApplicationWindow(ZLApplication application);
	
	public static String dialogMessage(ZLResourceKey key) {
		return resource().getResource(key).getResource("message").value();
	}
	
	public static String dialogTitle(ZLResourceKey key) {
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