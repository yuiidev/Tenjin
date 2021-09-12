package net.demozo.tenjin.exceptions;

public class InvalidTableException extends RuntimeException{
    public InvalidTableException(String message) {
        super(message);
    }
}
