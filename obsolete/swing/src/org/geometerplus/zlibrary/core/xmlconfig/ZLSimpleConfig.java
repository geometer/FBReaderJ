/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.core.xmlconfig;

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
