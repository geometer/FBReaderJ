package org.zlibrary.core.options.config;

import java.util.Set;
import java.util.HashMap;

/**
 * класс Конфиг. это своеобразная структура опций. основное поле myData содержит
 * список групп, которым в качестве ключей сопоставляются их имена.
 * 
 * @author Администратор
 * 
 */
final class ZLSimpleConfig {
	private final HashMap<String,ZLGroup> myData = new HashMap<String,ZLGroup>();

	void clear() {
		myData.clear();
	}

	Set<String> groupNames() {
		return myData.keySet();
	}

	void removeGroup(String name) {
		myData.remove(name);
	}

	ZLGroup getGroup(String name) {
		return myData.get(name);
	}

	String getValue(String groupName, String name, String defaultValue) {
		ZLGroup group = getGroup(groupName);
		if (group != null) {
			String value = group.getValue(name);
			if (value != null) {
				return value;
			}
		}
		return defaultValue;
	}

	String getCategory(String groupName, String name) {
		ZLGroup group = getGroup(groupName);
		if (group != null) {
			ZLOptionInfo option = group.getOption(name);
			if (option != null) {
				return option.getCategory();
			}
		}
		return null;
	}

	void setValue(String groupName, String name, String value, String category) {
		ZLGroup group = getGroup(groupName);
		if (group == null) {
			group = new ZLGroup();
			myData.put(groupName, group);
		}
		group.setValue(name, value, category);
	}

	void unsetValue(String groupName, String name) {
		//System.out.println("run");
		ZLGroup group = getGroup(groupName);
		if (group != null) {
			//System.out.println("case");
			group.unsetValue(name);
		}
	}

	/**
	 * метод вывода в строку
	 * 
	 * public String toString() { StringBuffer sb = new StringBuffer(); for
	 * (String categoryName : myData.keySet()) { sb.append("" + categoryName +
	 * "\n\n" + myData.get(categoryName) + "\n"); } return sb.toString(); }
	 */
}
