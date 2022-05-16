package org.sample.shortener.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.sample.shortener.config.AppProperties;
import org.sample.shortener.exception.InvalidUrlException;
import org.sample.shortener.exception.UrlNotFoundException;
import org.sample.shortener.model.entity.ShortenedUrl;
import org.sample.shortener.repository.ShortenedUrlRepository;
import org.sample.shortener.service.UrlShortenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class GeneratorBasedUrlShortenerServiceImpl implements UrlShortenerService {

   private final AppProperties appProperties;
   private final ShortenedUrlRepository repository;

   @Autowired
   @SuppressWarnings("unused")
   public GeneratorBasedUrlShortenerServiceImpl(AppProperties appProperties, ShortenedUrlRepository repository) {
      this.appProperties = appProperties;
      this.repository = repository;
   }

   public String shorten(String url, String customUrlSuffix) {
      if (StringUtils.isNotEmpty(customUrlSuffix)) {
         if (customUrlSuffix.length() > appProperties.getShortenUrlLength()) {
            throw new InvalidUrlException(
                  String.format("Supplied custom url suffix (%s) is too long. Max length is %s characters", customUrlSuffix, appProperties.getShortenUrlLength()));
         }
         repository.save(new ShortenedUrl(customUrlSuffix, url));
         return appProperties.getBaseUrl() + customUrlSuffix;
      }
      Optional<ShortenedUrl> shortenedUrl = repository.findByOriginalUrl(url);
      if (shortenedUrl.isPresent()) {
         return appProperties.getBaseUrl() + shortenedUrl.get().getShortenedSuffix();
      }
      String shortenedUrlSuffix = generateRandomString();
      repository.save(new ShortenedUrl(shortenedUrlSuffix, url));

      return appProperties.getBaseUrl() + shortenedUrlSuffix;
   }

   @Override
   public String getOriginal(String shortenedSuffix) {
      return repository.findByShortenedSuffix(shortenedSuffix)
            .map(ShortenedUrl::getOriginalUrl)
            .orElseThrow(() -> new UrlNotFoundException(String.format("Url %s%s is not found", appProperties.getBaseUrl(), shortenedSuffix)));
   }

   @Override
   public String getOriginalAndIncrementClickedTimes(String shortenedSuffix) {
      return repository.findByShortenedSuffix(shortenedSuffix).stream()
            .peek(ShortenedUrl::incrementTimesClicked)
            .peek(repository::save)
            .map(ShortenedUrl::getOriginalUrl)
            .findFirst().orElse(null);
   }

   @Override
   public Long getClicksCount(String shortenedSuffix) {
      return repository.findByShortenedSuffix(shortenedSuffix)
            .map(ShortenedUrl::getTimesClicked)
            .orElseThrow(() -> new UrlNotFoundException(String.format("Url %s%s is not found", appProperties.getBaseUrl(), shortenedSuffix)));
   }

   protected String generateRandomString() {
      return RandomStringUtils.randomAlphabetic(appProperties.getShortenUrlLength());
   }
}
