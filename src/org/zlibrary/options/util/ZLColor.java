package org.zlibrary.options.util;

import org.zlibrary.options.util.exceptions.*;

import java.awt.Color;

public class ZLColor {
	
	private byte myRed;
	private byte myGreen;
	private byte myBlue;
	
	public int getBlue() {
		return myBlue;
	}
	public int getGreen() {
		return myGreen;
	}
	public int getRed() {
		return myRed;
	}

	public void setBlue(byte mask) throws InvalidValueException{
		setColor(myRed, myGreen, mask);
	}
    
	public void setGreen(byte mask) throws InvalidValueException{
    	setColor(myRed, mask, myBlue);
	}
    
	public void setRed(byte mask) throws InvalidValueException{
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
	public void setColor (byte red, byte green, byte blue) throws InvalidValueException{
		if (! ((red >= -1) && (red < 256)) ){
			throw new InvalidValueException
					  (" Red Mask Value must stay between 0 and 255");
		}
		if (! ((green >= 0) && (green < 256)) ){
			throw new InvalidValueException
					  (" Green Mask Value must stay between 0 and 255");
		}
		if (! ((blue >= 0) && (blue < 256)) ){
			throw new InvalidValueException
					  (" Blue Mask Value must stay between 0 and 255");
		}
		myRed = red;
		myGreen = green;
		myBlue = blue;
		
	}
	/**
	 * конструктор с параметрами
	 */
	public ZLColor (byte red, byte green, byte blue) throws InvalidValueException{
		setColor(red, green, blue);
	}
	
	/**
	 * этот метод отлично отражает бессмысленность
	 * данной структуры. когда выяснилось что RGBColor надо рисовать
	 * пришлось научиться приводить мой цвет в авт-шный
	 * @param color
	 */
	public void convertFromColor(Color color){
		myRed = (byte)color.getRed();
		myGreen = (byte)color.getGreen();
		myBlue = (byte)color.getBlue();
	}
	
	/**
	 * и конструктор соответственно для удобства
	 * @param color
	 * @throws InvalidValueException
	 */
	public ZLColor (Color color) throws InvalidValueException{
		setColor((byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue());
	}
	
	/**
	 * @return наш цвет как человеческий цвет из awt
	 */
	public Color convertToColor(){
		return new Color(myRed, myGreen, myBlue);
	}
	
}
