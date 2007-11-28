package org.zlibrary.core.options.config;

public class ZLConfigInstance {
	
	private static final ZLConfig myConfig = new ZLConfigImp();
	
	public static ZLConfig getInstance(){
		return myConfig;
	}
}
