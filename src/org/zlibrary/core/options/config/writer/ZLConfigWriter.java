package org.zlibrary.core.options.config.writer;

import java.io.*;
import java.util.*;

import org.zlibrary.core.options.config.*;

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
	
	public void writeConfigFile(String configFileContent, String filePath) {
		File file = new File(filePath);
		try {
			PrintWriter pw = new PrintWriter(file, "UTF-8");
			try {
				pw.write(configFileContent);
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
		Map<String, ZLGroup> data = myConfig.getGroups();
		// ключ - имя категории, значение - содержимое соответствующего файла
		Map<String, StringBuffer> configFilesContent = 
			new HashMap<String, StringBuffer>();
		StringBuffer sb;
		Map<String, Boolean> currentGroupOpenedIn;
		
		for (String group : data.keySet()) {
			
			//ключ - имена категорий, о которых мы уже знаем, что она там есть 
			//значение - записали ли мы уже это в файле 
			currentGroupOpenedIn = new HashMap<String, Boolean>();
			
			for (ZLOptionValue value : data.get(group).getValues()) {
				sb = configFilesContent.get(value.getCategory());
				
				if (currentGroupOpenedIn.get(value.getCategory()) == null) {
					currentGroupOpenedIn.put(value.getCategory(), false);
				}
				
				if (sb == null) {
					sb = new StringBuffer();
					configFilesContent.put(value.getCategory(), sb);
				}
				
				if (!currentGroupOpenedIn.get(value.getCategory())) {
					sb.append("  <group name=\"" + group + "\">\n");
					currentGroupOpenedIn.put(value.getCategory(), true);
				}
				
				sb.append(value);
			}
			
			for (String category : currentGroupOpenedIn.keySet()) {
				configFilesContent.get(category).append("  </group>\n");
			}
			}
		
		for (String category : configFilesContent.keySet()){
			this.writeConfigFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					+ "<config>\n" + configFilesContent.get(category) + "</config>", 
					myDestinationDirectory + "/" + category + ".xml");
		}
	}
}
