package org.amse.ys.zip;

import java.io.IOException;

@SuppressWarnings("serial")
public class ZipException extends IOException {
    ZipException(String message) {
        super(message);
    }
}
