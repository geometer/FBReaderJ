package org.zlibrary.sampleview;

import org.zlibrary.core.library.ZLibrary;

public class Main {
	public static void main(String[] args) {
		ZLibrary.init();
		ZLibrary.run(new SampleApplication());
		ZLibrary.shutdown();
	}
}
