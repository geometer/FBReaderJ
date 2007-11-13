package org.fbreader.fbreader;

import org.zlibrary.core.library.ZLibrary;

public class Main {
	public static void main(String[] args) {
		ZLibrary.init();
		ZLibrary.run(new FBReader((args.length > 0) ? args[0] : "data/help/MiniHelp.ru.fb2"));
		ZLibrary.shutdown();
	}
}
