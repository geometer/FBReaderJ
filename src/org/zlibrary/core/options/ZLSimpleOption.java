package org.zlibrary.core.options;

interface ZLSimpleOption {
	public interface Type {
		int BOOLEAN = 1;
		int BOOLEAN3 = 2;
		int STRING = 3;
	}

	int getType();
}
