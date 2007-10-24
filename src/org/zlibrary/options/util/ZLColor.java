package org.zlibrary.options.util;

import java.awt.Color;

public class ZLColor {
	
	private int myRed;
	private int myGreen;
	private int myBlue;
	
	public int getBlue() {
		return myBlue;
	}
	public int getGreen() {
		return myGreen;
	}
	public int getRed() {
		return myRed;
	}

	public void setBlue(int mask){
		setColor(myRed, myGreen, mask);
	}
    
	public void setGreen(int mask){
    	setColor(myRed, mask, myBlue);
	}
    
	public void setRed(int mask){
		setColor(mask, myGreen, myBlue);
	}
	
	public boolean isInitialized(){
		return (myRed != -1);
	}
	/**
	 * перекрываем метод toString,
	 * по сути - кодировка в десятичную запись
	 */
	public String toString(){
		if (!isInitialized()){
			return "not initialized";
		} else {
			return "" + myRed + ", " + myGreen + ", " + myBlue;
		}
	}
	
	/**
	 * конструктор по умолчанию делает цвет черным
	 */
	public ZLColor (){
		myRed = 0;
		myGreen = 0;
		myBlue = 0;
	}
	
	/**
	 * с инициализацией сразу трех компонент цвета есть одна проблема
	 * она состоит в том что если мы сначала удачно прочли красный
	 * а потом неудачно прочли синий, то красный то мы уже успели
	 * поменять а вот на синем вылетели с иксепшоном и таким образом
	 * получили полуторный непонятный цвет, явно не то чего хотели
	 * поэтому приходится определять такой метод, а остальные через него
	 * @param red
	 * @param green
	 * @param blue
	 */
	public void setColor (int red, int green, int blue){
		myRed = red;
		myGreen = green;
		myBlue = blue;
	}
	/**
	 * конструктор с параметрами
	 */
	public ZLColor (int red, int green, int blue){
		setColor(red, green, blue);
	}
	
	/**
	 * этот метод отлично отражает бессмысленность
	 * данной структуры. когда выяснилось что RGBColor надо рисовать
	 * пришлось научиться приводить мой цвет в авт-шный
	 * @param color
	 */
	public void convertFromColor(Color color){
		myRed = (int)color.getRed();
		myGreen = (int)color.getGreen();
		myBlue = (int)color.getBlue();
	}
	
	/**
	 * и конструктор соответственно для удобства
	 * @param color
	 * @throws InvalidValueException
	 */
	public ZLColor (Color color){
		setColor((int)color.getRed(), (int)color.getGreen(), (int)color.getBlue());
	}
	
	/**
	 * @return наш цвет как человеческий цвет из awt
	 */
	public Color convertToColor(){
		return new Color(myRed, myGreen, myBlue);
	}
	
	public int getIntValue(){
		//TODO возвращение цвета одним числом
		return 0;
	}
}
