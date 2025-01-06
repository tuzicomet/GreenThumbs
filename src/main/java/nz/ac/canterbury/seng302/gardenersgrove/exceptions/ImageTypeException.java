package nz.ac.canterbury.seng302.gardenersgrove.exceptions;


public class ImageTypeException extends Exception {
    public ImageTypeException() {
        super();
    }

    public ImageTypeException(String message) {
        super(message);
    }

    public ImageTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImageTypeException(Throwable cause) {
        super(cause);
    }
}

