package org.zlibrary.core.options.config;

import java.util.*;

/**
 * класс Конфиг. это своеобразная структура опций. основное поле myData содержит
 * список групп, которым в качестве ключей сопоставляются их имена.
 * 
 * @author Администратор
 * 
 */
/* package */final class ZLSimpleConfig implements ZLConfig {

	private Set<ZLGroup> myData;

	public ZLDeltaConfig getDelta() {
		return null;
	}
	public ZLSimpleConfig() {
		myData = new LinkedHashSet<ZLGroup>();
	}

	protected void clear() {
		myData.clear();
	}

	protected Set<ZLGroup> getGroups() {
		return Collections.unmodifiableSet(myData);
	}

	public void removeGroup(String name) {
		for (ZLGroup group : myData) {
			if (group.getName().equals(name)) {
				myData.remove(group);
			}
		}
	}

	protected ZLGroup getGroup(String name) {
		for (ZLGroup group : myData) {
			if (group.getName().equals(name)) {
				return group;
			}
		}
		return null;
	}

	public String getValue(String group, String name, String defaultValue) {
		ZLGroup gr = getGroup(group);
		if (gr != null) {
			if (gr.getValue(name) != null) {
				return gr.getValue(name);
			}
		}
		return defaultValue;
	}

	protected void setCategory(String group, String name, String cat) {
		ZLGroup gr = getGroup(group);
		if (gr != null) {
			ZLOptionInfo option = gr.getOption(name);
			if (option != null) {
				option.setCategory(cat);
			}
		}
	}

	protected String getCategory(String group, String name) {
		ZLGroup gr = getGroup(group);
		if (gr != null) {
			ZLOptionInfo option = gr.getOption(name);
			if (option != null) {
				return option.getCategory();
			}
		}
		return null;
	}

	public void setValue(String group, String name, String value,
			String category) {
		ZLGroup gr = getGroup(group);
		if (gr != null) {
			gr.setValue(name, value, category);
		} else {
			ZLGroup newGroup = new ZLGroup(group);
			newGroup.setValue(name, value, category);
			myData.add(newGroup);
		}
	}

	public void unsetValue(String group, String name) {
		//System.out.println("run");
		ZLGroup gr = getGroup(group);
		if (gr != null) {
			//System.out.println("case");
			gr.unsetValue(name);
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
