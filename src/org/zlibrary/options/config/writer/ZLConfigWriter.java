package org.zlibrary.options.config.writer;

import java.io.*;
import java.util.Map;

import org.zlibrary.options.config.*;

/*package*/ class ZLConfigWriter implements ZLWriter {
   
    private ZLConfig myConfig = ZLConfigInstance.getInstance();
    private String myDestinationPath = "";
    
    public ZLConfigWriter(String path){
        myDestinationPath = path;
    }
    
    public void writeCategoryInFile(ZLCategory category, String filePath) {
        try {
            PrintWriter pw = new PrintWriter(new File(filePath));
            try {
                pw.write(category.toString());
            } finally {
                pw.close();
            }
        } catch (FileNotFoundException fnfException) {
            System.out.println(fnfException.getMessage());
        }
    }
    
    public void write() {
        Map<String, ZLCategory> data = myConfig.getCategories();
        for (String name : data.keySet()){
            this.writeCategoryInFile(data.get(name), myDestinationPath + name + ".xml");
        }
    }
	
}
