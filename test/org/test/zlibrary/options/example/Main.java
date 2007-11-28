package org.test.zlibrary.options.example;

import org.zlibrary.core.options.config.reader.ZLConfigReaderFactory;
import org.zlibrary.core.options.config.writer.*;

//TODO ЭТО ПОКА ЧТО КРИВОЙ ТЕСТ.

/**
 * НЕПОЛНОЦЕННЫЙ ТЕСТ!!!!!
 * тесты 01 - это тесты на 
 * @author Администратор
 *
 */
public class Main {
	
	public static void main(String[] args){
		//readConfigFile(new File("test/org/test/zlibrary/options/example/books.xml"));
		//readConfigFile(new File("test/org/test/zlibrary/options/example/options.xml"));
		// readConfigFile(new File("test/org/test/zlibrary/options/example/search.xml"));
		//readConfigFile(new File("test/org/test/zlibrary/options/example/state.xml"));
		//readConfigFile(new File("test/org/test/zlibrary/options/example/ui.xml"));
		//readConfigFile(new File("test/org/test/zlibrary/options/example/output/ui.xml"));
		
		String input = "test/org/test/zlibrary/options/example/";
		//String input = "test/org/test/zlibrary/options/example/";
		ZLConfigReaderFactory.createConfigReader(input).read();
		
		
		
		/*ZLIntegerOption myOption = new ZLIntegerOption("ui", "MYGROUP", 
				"length", 12);
		myOption.setValue(156);
		ZLStringOption myChangeableOption = new ZLStringOption("gruiu", "MYGROUP", 
				"lengthChangeable", "12312");
		myChangeableOption.setValue("I_COULD_CHANGE!");
		ZLConfigInstance.getInstance().unsetValue("MYGROUP", 
		"lengthChangeable");*/
		
        String output = "test/org/test/zlibrary/options/example/output/";
        //ZLConfigWriterFactory.createConfigWriter(output).writeDelta();
		ZLConfigWriterFactory.createConfigWriter(output).write();
        //ZLConfigWriterFactory.createConfigWriter(output + "cleared-").writeDelta();
	}
	
}
