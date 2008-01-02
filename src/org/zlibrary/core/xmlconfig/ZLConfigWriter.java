package org.zlibrary.core.xmlconfig;

import java.io.*;
import java.util.*;

final class ZLConfigWriter implements ZLWriter {
	private final ZLConfigImpl myConfig;
	private final File myDestinationDirectory;

	protected ZLConfigWriter(ZLConfigImpl config, String path) {
		myConfig = config;
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
			if (!file.getName().equals("delta.xml")) {
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

	//TODO пока public в целях отладки
	public void writeDelta() {
		this.writeConfigFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ myConfig.getDelta(), myDestinationDirectory + "/delta.xml");
	}

	private String configFilePath(String category) {
		return myDestinationDirectory + "/" + category + ".xml";
	}

	public void write() {
		final Set<String> usedCategories = myConfig.applyDelta();
		// ключ - имя категории, значение - содержимое соответствующего файла
		final HashMap<String, StringBuffer> configFilesContent = new HashMap<String, StringBuffer>();

		for (String groupName : myConfig.groupNames()) {

			// ключ - имена категорий, о которых мы уже знаем, что она там есть
			// значение - записали ли мы уже это в файле
			final HashMap<String,Boolean> currentGroupOpenedIn = new HashMap<String, Boolean>();

			ZLGroup group = myConfig.getGroup(groupName);
			for (String optionName : group.optionNames()) {
				ZLOptionInfo option = group.getOption(optionName);
				StringBuffer sb = configFilesContent.get(option.getCategory());

				if (currentGroupOpenedIn.get(option.getCategory()) == null) {
					currentGroupOpenedIn.put(option.getCategory(), false);
				}

				if (sb == null) {
					sb = new StringBuffer();
					configFilesContent.put(option.getCategory(), sb);
				}

				if (!currentGroupOpenedIn.get(option.getCategory())) {
					sb.append("  <group name=\"" + groupName + "\">\n");
					currentGroupOpenedIn.put(option.getCategory(), true);
				}
				sb.append(option);
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
		 * если в категориях, которые мы изменили, существуют те, в которых
		 * после изменений ничего не лежит, то мы удаляем соответствующие файлы
		 */
		for (String category : usedCategories) {
			if (!configFilesContent.keySet().contains(category)) {
				deleteConfigFile(configFilePath(category));
			}
		}
	}
}
