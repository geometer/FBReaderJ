package org.geometerplus.zlibrary.core.options;

public interface ZLSimpleOption {
	public interface Type {
		int BOOLEAN = 1;
		int BOOLEAN3 = 2;
		int STRING = 3;
	}

	int getType();
}
