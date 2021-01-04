package com.jz.jsoncompare;

/**
 * @author jzfeng
 */
public class WrongJsonPathException extends RuntimeException {

    WrongJsonPathException() {
        super();
    }

    public WrongJsonPathException(String message) {
        super(message);
    }

}
