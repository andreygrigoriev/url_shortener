package org.sample.shortener.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.sample.shortener.exception.InvalidUrlException;
import org.sample.shortener.exception.UrlNotFoundException;
import org.sample.shortener.exception.UrlShortenGeneralException;
import org.sample.shortener.service.UrlShortenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@Slf4j
@SuppressWarnings("unused")
@RequestMapping("/api/v1")
public class ApiController {

   private final UrlShortenerService urlShortenerService;
   private static final String[] VALID_SCHEMES = {"http", "https"};
   private final UrlValidator urlValidator;

   @Autowired
   public ApiController(UrlShortenerService urlShortenerService) {
      this.urlShortenerService = urlShortenerService;
      this.urlValidator = new UrlValidator(VALID_SCHEMES, UrlValidator.ALLOW_LOCAL_URLS);
   }

   @PostMapping("/create")
   public String createShortUrl(String url, @RequestParam(required = false) String customUrlSuffix) {
      if (!urlValidator.isValid(url)) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Supplied url is not valid");
      }
      try {
         return urlShortenerService.shorten(url, customUrlSuffix);
      } catch (InvalidUrlException ex) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
      } catch (UrlNotFoundException ex) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
      } catch (UrlShortenGeneralException ex) {
         log.error(String.format("Something went wrong in createShortUrl. url=%s, customUrlSuffix=%s", url, customUrlSuffix), ex);
         throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
      }
   }

   @GetMapping("/stats")
   public Long statisticsByUrl(@RequestParam String url) {
      if (StringUtils.isEmpty(url)) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Supplied url is empty");
      }
      String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
      if (!urlValidator.isValid(decodedUrl)) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Supplied url is not valid");
      }

      String suffix = url.substring(url.lastIndexOf('/') + 1);

      try {
         return urlShortenerService.getClicksCount(suffix);
      } catch (UrlNotFoundException ex) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
      } catch (UrlShortenGeneralException ex) {
         log.error(String.format("Something went wrong in statisticsByUrl. url=%s", url), ex);
         throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
      }
   }
}
