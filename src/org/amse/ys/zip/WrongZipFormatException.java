package org.amse.ys.zip;

public class WrongZipFormatException extends Exception {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    WrongZipFormatException(String errorText) {
	super(errorText);
    }
}
