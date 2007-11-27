package org.fbreader.fbreader;

import org.zlibrary.core.application.ZLAction;

public class FBReaderActions {
	
	private static class FBAction extends ZLAction {
		private FBReader myFBReader;

		protected FBAction(FBReader fbreader) {
			this.myFBReader = fbreader;
		}
		
		protected FBReader fbreader() {
			return this.myFBReader;
		}
		
		public void run() {}

	};

	static public class ShowHelpAction extends FBAction {
		ShowHelpAction(FBReader fbreader) {
			super(fbreader);
		}
		
		public void run() {
	    	System.out.println("hello");
	    }
	};
	
	static public class QuitAction extends FBAction {

		public QuitAction(FBReader fbreader) {
			super(fbreader);
		}
		
		public void run() {
			fbreader().closeView();
		}		
	};

}
