package org.zlibrary.core.options.config;

import java.util.*;

/**
 * класс Конфиг. это своеобразная структура опций.
 * основное поле myData содержит список групп, которым
 * в качестве ключей сопоставляются их имена.
 * @author Администратор
 *
 */
/*package*/ class ZLSimpleConfigImpl implements ZLSimpleConfig {
	// public abstract void unsetValue(String group, String name);

	// public abstract  boolean isAutoSavingSupported() const = 0;
	// public abstract  void startAutoSave(int seconds) = 0;
	private Map<String, ZLGroup> myData;
	
	public ZLSimpleConfigImpl() {
		myData = new TreeMap<String, ZLGroup>();
	}
	
	protected void clear() {
		myData.clear();
	}
	
	public Map<String, ZLGroup> getGroups() {
		return Collections.unmodifiableMap(myData);
	}
	
	public void removeGroup(String group) {
		if (myData.get(group) != null){
			myData.remove(group);
		}
	}

	public String getValue(String group, String name, String defaultValue) {
		if (myData.get(group) != null){
			if (myData.get(group).getValue(name) != null){
				return myData.get(group).getValue(name);
			}
		} 
		return defaultValue;
	}
	
	public void setCategory(String group, String name, String cat) {
		ZLGroup gr = myData.get(group);
		if (gr != null){
			ZLOptionValue option = gr.getOption(name);
			if (option != null) {
				option.setCategory(cat);
			}
		} 
	}
	
	public void setValue(String group, String name, String value, String category) {
		if (myData.get(group) != null){
			myData.get(group).setValue(name, value, category);
		} else {
			ZLGroup newGroup = new ZLGroup();
			newGroup.setValue(name, value, category);
			myData.put(group, newGroup);
		}
	}
	
	public void unsetValue(String group, String name) {
		ZLGroup gr = myData.get(group);
		if (gr != null) {
			gr.unsetValue(name);
		}
	}
	
	/**
	 * метод вывода в строку
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (String categoryName : myData.keySet()){
			sb.append("" + categoryName + "\n\n" + myData.get(categoryName) + "\n");
		}
		return sb.toString();
	}   
}
