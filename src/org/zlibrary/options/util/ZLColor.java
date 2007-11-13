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
     * и конструктор соответственно дл€ удобства
     * @param color
     */
    public ZLColor(Color color){
        setColor((int)color.getRed(), (int)color.getGreen(), (int)color.getBlue());
    }

    /**
     * конструктор по умолчанию делает цвет черным
     */
    public ZLColor(){
    }
    
    /**
     * конструктор с параметрами
     */
    public ZLColor(int red, int green, int blue){
        setColor(red, green, blue);
    }
    
    public ZLColor(String color){
        String[] components = color.split(",");
        setColor(Integer.parseInt(components[0]), Integer.parseInt(components[1]),
                 Integer.parseInt(components[2]));
    }
    
    /**
	 * перекрываем метод toString,
	 * по сути - кодировка в дес€тичную запись
     * результат должен нагл€дно совпадать с результатом getIntValue
	 */
	public String toString(){
		return "" + myRed + "," + myGreen + "," + myBlue;
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
		if (isCorrectComponent(red)) {
			myRed = red;
        }
	    if (isCorrectComponent(green)) {
			myGreen = green;
        }
		if (isCorrectComponent(blue)) {
			myBlue = blue;
        }
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
	 * @return цвет одним числом, чтобы хранить в пам€ти меньше =)
	 */	
	public long getIntValue(){
		return myRed*1000000 + myGreen*1000 + myBlue;
	}
	
	/**
	 * перекрываем метод equals. цвета считаем равными при равенстве всех компонент
	 */
	public boolean equals (Object o){
		if (o == this) 
			return true;
		
		if (! (o.getClass() == this.getClass()))
			return false;
		
		ZLColor zlc = (ZLColor) o;
		
		return ((zlc.myRed == this.myRed) &&
				(zlc.myGreen == this.myGreen) &&
				(zlc.myBlue == this.myBlue));
	}
	
}
