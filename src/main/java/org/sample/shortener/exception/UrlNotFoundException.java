package org.sample.shortener.exception;


public class UrlNotFoundException extends RuntimeException {
   public UrlNotFoundException(String message) {
      super(message);
   }
}
