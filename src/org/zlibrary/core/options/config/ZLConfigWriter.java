package org.zlibrary.core.options.config;

import java.io.*;
import java.util.*;

/*package*/ final class ZLConfigWriter implements ZLWriter {

	private final ZLConfigImpl myConfig = ZLConfigInstance.getExtendedInstance();

	private final File myDestinationDirectory;

	protected ZLConfigWriter(String path) {
		File file = new File(path);
		if (!file.exists()) {
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
		} catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
		}
	}

	private void deleteConfigFile(String filePath) {
		File file = new File(filePath);
		file.delete();
	}

	protected void writeDelta() {
		// TODO ƒќѕ»—ј“№, »—ѕќЋ№«”я —“–ќ≈Ќ»≈ ‘ј…Ћј ƒ≈Ћ№“џ »« —»ЎЌќ√ќ  ќƒј
		this.writeConfigFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ myConfig.getDelta(), myDestinationDirectory + "/delta.xml");
	}

	private String configFilePath(String category) {
		return myDestinationDirectory + "/" + category + ".xml";
	}

	public void write() {
		//writeDelta();
		
		
		//testtesttest
		/*myConfig.setValueDirectly("gr", "nm", "5", "cat");
		System.out.println(myConfig.getValue("gr", "nm", "xfiles1"));
		ZLIntegerOption option = new ZLIntegerOption("cat", "gr", "nm", 2);
		option.setValue(1);
		System.out.println("get - " +option.getValue());
		System.out.println(myConfig.getValue("gr", "nm", ""));
		option.setValue(2);
		System.out.println("get - " +option.getValue());
		System.out.println(myConfig.getValue("gr", "nm", ""));
		*/
		
		
		Set<String> usedCategories = myConfig.applyDelta();
		Set<ZLGroup> groups = myConfig.getGroups();
		// ключ - им€ категории, значение - содержимое соответствующего файла
		Map<String, StringBuffer> configFilesContent = new LinkedHashMap<String, StringBuffer>();
		StringBuffer sb;
		Map<String, Boolean> currentGroupOpenedIn;

		for (ZLGroup group : groups) {

			// ключ - имена категорий, о которых мы уже знаем, что она там есть
			// значение - записали ли мы уже это в файле
			currentGroupOpenedIn = new HashMap<String, Boolean>();

			for (ZLOptionInfo value : group.getOptions()) {
				sb = configFilesContent.get(value.getCategory());

				if (currentGroupOpenedIn.get(value.getCategory()) == null) {
					currentGroupOpenedIn.put(value.getCategory(), false);
				}

				if (sb == null) {
					sb = new StringBuffer();
					configFilesContent.put(value.getCategory(), sb);
				}

				if (!currentGroupOpenedIn.get(value.getCategory())) {
					sb.append("  <group name=\"" + group.getName() + "\">\n");
					currentGroupOpenedIn.put(value.getCategory(), true);
				}
				sb.append(value);
			}

			for (String category : currentGroupOpenedIn.keySet()) {
				configFilesContent.get(category).append("  </group>\n");
			}
		}

		for (String category : usedCategories) {
			this.writeConfigFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					+ "<config>\n" + configFilesContent.get(category)
					+ "</config>", configFilePath(category));
		}

		/**
		 * если в категори€х, которые мы изменили, существуют те, в которых
		 * после изменений ничего не лежит, то мы удал€ем соответствующие файлы
		 */
		for (String category : usedCategories) {
			if (!configFilesContent.keySet().contains(category.intern())) {
				deleteConfigFile(configFilePath(category));
			}
		}
	}
}
