package org.fbreader.fbreader;

import org.zlibrary.core.library.ZLibrary;

public class Main {
	public static void main(String[] args) {
		ZLibrary.init();
		ZLibrary.run(new FBReader(args[0]));
		ZLibrary.shutdown();
	}
}
