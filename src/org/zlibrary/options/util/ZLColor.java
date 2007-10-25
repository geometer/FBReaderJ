package org.zlibrary.options.util;

import java.awt.Color;

/**
 * сущность "цвет". представление здесь - стандартные RGB компоненты
 * @author јдминистратор
 *
 */
public class ZLColor {
	
	private int myRed = 0;
	private int myGreen = 0;
	private int myBlue = 0;
	
	/**
	 * перекрываем метод toString,
	 * по сути - кодировка в дес€тичную запись
	 */
	public String toString(){
		return "" + myRed + ", " + myGreen + ", " + myBlue;
	}
	
	/**
	 * €вл€етс€ ли число корректным значением компоненты ргб цвета.
	 */
	private boolean isCorrectComponent(int value){
		return (value >=0 && value <= 255);
	}
	/**
	 * устанавливаем только те компоненты, которые заданы корректно,
	 * то есть €вл€ютс€ неотрицательным числом < 256
	 */
	public void setColor (int red, int green, int blue){
		if (isCorrectComponent(red))
			myRed = red;
	    if (isCorrectComponent(green))
			myGreen = green;
		if (isCorrectComponent(blue))	
			myBlue = blue;
	}
	
	/**
	 * конвертирование в ј¬“шный цвет.
	 * @param color
	 */
	//TODO решить нужно ли это вообще
	public void convertFromColor(Color color){
		myRed = (int)color.getRed();
		myGreen = (int)color.getGreen();
		myBlue = (int)color.getBlue();
	}
	
	public Color convertToColor(){
		return new Color(myRed, myGreen, myBlue);
	}
	
	/**
	 * и конструктор соответственно дл€ удобства
	 * @param color
	 * @throws InvalidValueException
	 */
	public ZLColor (Color color){
		setColor((int)color.getRed(), (int)color.getGreen(), (int)color.getBlue());
	}

	/**
	 * @return цвет одним числом, чтобы хранить в пам€ти меньше =)
	 */	
	public int getIntValue(){
		return myRed*1000000 + myGreen*1000 + myBlue;
	}
	
	/**
	 * конструктор по умолчанию делает цвет черным
	 */
	public ZLColor (){
	}
	
	/**
	 * конструктор с параметрами
	 */
	public ZLColor (int red, int green, int blue){
		setColor(red, green, blue);
	}
	
	
}
