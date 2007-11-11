package org.zlibrary.core.application.toolbar;

abstract public class Item {
	public Item() {}
	
    public abstract Type type();
    
	boolean isButton() { 
		return type() == Type.BUTTON; 
	}
}
