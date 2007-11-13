package org.test.zlibrary.options.example;

import java.io.File;

import org.zlibrary.options.*;
import org.zlibrary.options.config.ZLConfigInstance;
import org.zlibrary.options.config.reader.ZLConfigReaderFactory;
import org.zlibrary.options.config.writer.*;

//TODO ЭТО ПОКА ЧТО КРИВОЙ ТЕСТ.

/**
 * НЕПОЛНОЦЕННЫЙ ТЕСТ!!!!!
 * тесты 01 - это тесты на 
 * @author Администратор
 *
 */
public class Main {
	
	private static void readConfigFile (File file){
        ZLConfigReaderFactory.createConfigReader().read(file);
	}
	
	public static void main(String[] args){
		readConfigFile(new File("test/org/test/zlibrary/options/example/books.xml"));
        readConfigFile(new File("test/org/test/zlibrary/options/example/options.xml"));
        readConfigFile(new File("test/org/test/zlibrary/options/example/search.xml"));
        readConfigFile(new File("test/org/test/zlibrary/options/example/state.xml"));
        readConfigFile(new File("test/org/test/zlibrary/options/example/ui.xml"));
       
        //System.out.println(ZLConfigInstance.getInstance());
        ZLStringOption myOption = new ZLStringOption("ui", "MYGROUP", 
                "length", "12312");
        myOption.setValue("I'M_CHANGED!");
        ZLStringOption myChangeableOption = new ZLStringOption("ui", "MYGROUP", 
                "lengthChangeable", "12312");
        myChangeableOption.setValue("I_COULD_CHANGE!");
        ZLConfigInstance.getInstance().unsetValue("ui", "MYGROUP", 
                "lengthChangeable");
        String path = "test/org/test/zlibrary/options/example/output/";
        ZLConfigWriterFactory.createConfigWriter(path).write(); 
	}
	
}
