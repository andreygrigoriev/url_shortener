package org.sample.shortener.controller;

import org.apache.commons.lang3.StringUtils;
import org.sample.shortener.service.UrlShortenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@SuppressWarnings("unused")
public class RedirectionController {

   private final UrlShortenerService urlShortenerService;

   @Autowired
   public RedirectionController(UrlShortenerService urlShortenerService) {
      this.urlShortenerService = urlShortenerService;
   }

   @GetMapping("/{suffix}")
   public void redirect(@PathVariable String suffix, HttpServletResponse response) throws IOException {
      String originalUrl = urlShortenerService.getOriginalAndIncrementClickedTimes(suffix);
      if (StringUtils.isEmpty(originalUrl)) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find original url");
      }
      response.sendRedirect(originalUrl);
   }
}
