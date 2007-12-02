package org.test.zlibrary.options.example;

import org.zlibrary.core.library.ZLibrary;
import org.zlibrary.core.options.config.reader.ZLConfigReaderFactory;
import org.zlibrary.core.options.config.writer.*;
import org.zlibrary.core.options.*;

import org.zlibrary.ui.swing.library.ZLSwingLibrary;
//TODO ›“Œ œŒ ¿ ◊“Œ  –»¬Œ… “≈—“.

/**
 * Õ≈œŒÀÕŒ÷≈ÕÕ€… “≈—“!!!!!
 * ÚÂÒÚ˚ 01 - ˝ÚÓ ÚÂÒÚ˚ Ì‡ 
 * @author ¿‰ÏËÌËÒÚ‡ÚÓ
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
		//(new ZLSwingLibrary()).run(args);
		
		String input = "test/org/test/zlibrary/options/example/";
		//String input = "test/org/test/zlibrary/options/example/";
		ZLConfigReaderFactory.createConfigReader(input).read();
		
		String output = "test/org/test/zlibrary/options/example/output/";
		
		
		ZLBooleanOption option = new ZLBooleanOption("test", "group", "test", true);
		ZLBooleanOption option2 = new ZLBooleanOption("test", "group", "steest", true);
		//option.setValue(true);
		ZLConfigWriterFactory.createConfigWriter(output).write();
		option.setValue(false);
		option2.setValue(false);
		option.setValue(true);
		option2.setValue(true);
		ZLIntegerOption myOption = new ZLIntegerOption("ui", "MYGROUP", 
				"length", 12);
		myOption.setValue(156);
		ZLStringOption myChangeableOption = new ZLStringOption("gruiu", "MYGROUP", 
				"lengthChangeable", "12312");
		myChangeableOption.setValue("I_COULD_CHANGE!");
		//ZLConfigInstance.getInstance().unsetValue("MYGROUP", 
		//"lengthChangeable");
		
		ZLConfigWriterFactory.createConfigWriter(output).writeDelta();
		ZLConfigWriterFactory.createConfigWriter(output).write();
		//ZLConfigWriterFactory.createConfigWriter(output + "cleared-").writeDelta();
	}
	
}
