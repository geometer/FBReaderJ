package org.zlibrary.options.config;

public class ZLConfigInstance {
    
    private static final ZLConfigImpl myConfig = new ZLConfigImpl();
    
    public static ZLConfigImpl getInstance(){
        return myConfig;
    }
}
