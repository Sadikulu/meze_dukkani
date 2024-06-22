package com.meze.exception.message;

public class ImageFileException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ImageFileException(String message) {
        super(message);
    }
}
