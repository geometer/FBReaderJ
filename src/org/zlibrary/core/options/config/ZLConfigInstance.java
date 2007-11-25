package org.zlibrary.core.options.config;

public class ZLConfigInstance {
	
	private static final ZLConfig myConfig = new ZLConfigImpl();
	
	public static ZLConfig getInstance(){
		return myConfig;
	}
}
