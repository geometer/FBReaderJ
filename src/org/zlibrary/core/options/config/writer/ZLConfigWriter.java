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
	
	private void writeConfigFile(String configFileContent, String filePath) {
		File file = new File(filePath);
		try {
			PrintWriter pw = new PrintWriter(file, "UTF-8");
			try {
				pw.write(configFileContent);
			} finally {
				pw.close();
			}
		} catch (FileNotFoundException fnfException) {
			if (!file.getName().toLowerCase().equals("delta.xml")) {
				System.err.println(fnfException.getMessage());
			}
		}  catch(UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void deleteConfigFile(String filePath) {
		File file = new File(filePath);
		file.delete();
	}
	
	public void writeDelta() {
		//TODO ДОПИСАТЬ, ИСПОЛЬЗУЯ СТРОЕНИЕ ФАЙЛА ДЕЛЬТЫ ИЗ СИШНОГО КОДА
		this.writeConfigFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ myConfig.getDelta(), myDestinationDirectory + "/delta.xml");
	}
	
	private String configFilePath(String category) {
		return myDestinationDirectory + "/" + category + ".xml";
	}
	
	public void write() {
		myConfig.applyDelta();
		Map<String, ZLGroup> data = myConfig.getGroups();
		// ключ - имя категории, значение - содержимое соответствующего файла
		Map<String, StringBuffer> configFilesContent = 
			new LinkedHashMap<String, StringBuffer>();
		StringBuffer sb;
		Map<String, Boolean> currentGroupOpenedIn;
		Set<String> notEmptyCategories = new HashSet<String>();
		
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
				//if (!value.getCategory().equals("books"))
				//System.out.println(value.getCategory());
				sb.append(value);
			}
			
			for (String category : currentGroupOpenedIn.keySet()) {
				configFilesContent.get(category).append("  </group>\n");
			}
			}
		
		for (String category : configFilesContent.keySet()){
			notEmptyCategories.add(category.intern());
			this.writeConfigFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					+ "<config>\n" + configFilesContent.get(category) + "</config>", 
					configFilePath(category));
		}
		
		/*for (String category : myConfig.getCategories()) {
			if (!notEmptyCategories.contains(category.intern())) {
				deleteConfigFile(configFilePath(category));
			}
		}*/
	}
}
