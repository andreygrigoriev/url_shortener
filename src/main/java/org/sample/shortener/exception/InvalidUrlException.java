package org.sample.shortener.exception;

public class InvalidUrlException extends RuntimeException {
   public InvalidUrlException(String message) {
      super(message);
   }
}
