package org.zlibrary.options;

/**
 * класс простая опция. описывает опции, вывод которых на экран
 * делается более или менее одной строчкой
 * @author Администратор
 *
 */
abstract class ZLSimpleOption extends ZLOption {
	/**
	 * во всех final наследниках должен быть метод,
	 * отдающий тип этого наследника.
	 * @return
	 */
	public abstract ZLOptionType getType();
	
	/**
	 * конструктор. создается так же как и любая опция
	 * @see ZLOption
	 */
	public ZLSimpleOption(ZLConfig config, String category, String group, String optionName){
		super(config, category, group, optionName);
	}
}
