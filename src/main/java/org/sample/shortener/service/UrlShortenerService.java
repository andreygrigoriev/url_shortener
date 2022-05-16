package org.sample.shortener.service;

public interface UrlShortenerService {
   String shorten(String url, String customUrlSuffix);
   String getOriginal(String suffix);
   String getOriginalAndIncrementClickedTimes(String suffix);
   Long getClicksCount(String suffix);
}
