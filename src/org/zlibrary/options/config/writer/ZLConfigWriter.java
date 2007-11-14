package org.zlibrary.options.config.writer;

import java.io.*;
import java.util.Map;

import org.zlibrary.options.config.*;

/*package*/ class ZLConfigWriter implements ZLWriter {
   
    private ZLConfig myConfig = ZLConfigInstance.getInstance();
    private File myDestinationDirectory;
    
    public ZLConfigWriter(String path){
        File file = new File(path);
        if (!file.exists()){
            file.mkdir();
        } 
        myDestinationDirectory = file;
    }
    
    public void writeCategoryInFile(ZLCategory category, String filePath) {
        File file = new File(filePath);
        try {
            PrintWriter pw = new PrintWriter(file, "UTF-8");
            try {
                pw.write(category.toString());
            } finally {
                pw.close();
            }
        } catch (FileNotFoundException fnfException) {
            System.out.println(fnfException.getMessage());
        }  catch(UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public void write() {
        Map<String, ZLCategory> data = myConfig.getCategories();
        for (String name : data.keySet()){
            this.writeCategoryInFile(data.get(name), 
                    myDestinationDirectory + "/" + name + ".xml");
        }
    }
	
}
