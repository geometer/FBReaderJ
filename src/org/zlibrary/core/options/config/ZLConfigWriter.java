package org.zlibrary.core.options.config;

import java.io.*;
import java.util.*;

/*package*/ class ZLConfigWriter implements ZLWriter {
   
	private ZLConfigImpl myConfig = ZLConfigInstance.getExtendedInstance();
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
		//TODO ƒќѕ»—ј“№, »—ѕќЋ№«”я —“–ќ≈Ќ»≈ ‘ј…Ћј ƒ≈Ћ№“џ »« —»ЎЌќ√ќ  ќƒј
		this.writeConfigFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ myConfig.getDelta(), myDestinationDirectory + "/delta.xml");
	}
	
	private String configFilePath(String category) {
		return myDestinationDirectory + "/" + category + ".xml";
	}
	
	public void write() {
		Set<String> usedCategories = myConfig.applyDelta();
		Map<String, ZLGroup> data = myConfig.getGroups();
		// ключ - им€ категории, значение - содержимое соответствующего файла
		Map<String, StringBuffer> configFilesContent = 
			new LinkedHashMap<String, StringBuffer>();
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
		
		for (String category : usedCategories){
			this.writeConfigFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					+ "<config>\n" + configFilesContent.get(category) + "</config>", 
					configFilePath(category));
		}
		
        /**
         * если в категори€х, которые мы изменили, существуют те, в которых после изменений
         * ничего не лежит, то мы удал€ем соответствующие файлы
         */
		for (String category : usedCategories) {
			if (!configFilesContent.keySet().contains(category.intern())) {
				deleteConfigFile(configFilePath(category));
			}
		}
	}
}
