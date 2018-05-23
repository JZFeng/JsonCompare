package com.jz.json.jsoncompare;

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
