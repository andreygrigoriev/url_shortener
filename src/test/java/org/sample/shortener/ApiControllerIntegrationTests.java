package org.sample.shortener;

import org.apache.commons.validator.routines.UrlValidator;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sample.shortener.config.AppProperties;
import org.sample.shortener.model.entity.ShortenedUrl;
import org.sample.shortener.repository.ShortenedUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestConfig.class })
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
public class ApiControllerIntegrationTests {
   private static final String PATH_CREATE = "/api/v1/create";
   private static final String PATH_STATISTICS = "/api/v1/stats";
   private static final String VALID_URL = "https://github.com/spring-projects/spring-authorization-server/";
   private static final String INVALID_URL = "httpsd://github.com/spring-projects/spring-authorization-server/";
   private static final String CUSTOM_SUFFIX_VALID = "test";
   private static final String CUSTOM_SUFFIX_INVALID = "verylongstring";
   private static final String NON_EXISTING_SHORT_URL_SUFFIX = "trash";
   private static final String[] VALID_SCHEMES = {"http", "https"};
   private final UrlValidator urlValidator = new UrlValidator(VALID_SCHEMES, UrlValidator.ALLOW_LOCAL_URLS);

   @Autowired
   @SuppressWarnings("unused")
   private MockMvc mvc;

   @Autowired
   @SuppressWarnings("unused")
   private ShortenedUrlRepository repository;

   @Autowired
   @SuppressWarnings("unused")
   private AppProperties appProperties;

   @Test
   public void givenValidUrl_simple_create() throws Exception {
      MvcResult result = mvc.perform(MockMvcRequestBuilders.post(PATH_CREATE).param("url", VALID_URL))
            .andExpect(status().isOk())
            .andReturn();
      String content = result.getResponse().getContentAsString();
      Assertions.assertThat(urlValidator.isValid(content)).isTrue();
      Assertions.assertThat(content.substring(content.lastIndexOf('/') + 1).length()).isEqualTo(appProperties.getShortenUrlLength());
   }

   @Test
   public void givenValidUrlAndValidSuffix_create_custom() throws Exception {
      MvcResult result = mvc.perform(MockMvcRequestBuilders.post(PATH_CREATE)
                  .param("url", VALID_URL)
                  .param("customUrlSuffix", CUSTOM_SUFFIX_VALID))
            .andExpect(status().isOk())
            .andReturn();
      String content = result.getResponse().getContentAsString();
      Assertions.assertThat(urlValidator.isValid(content)).isTrue();
      Assertions.assertThat(content.substring(content.lastIndexOf('/') + 1).length()).isLessThanOrEqualTo(appProperties.getShortenUrlLength());
   }

   @Test
   public void givenValidUrlAndInvalidSuffix_create_custom() throws Exception {
      mvc.perform(MockMvcRequestBuilders.post(PATH_CREATE)
                  .param("url", VALID_URL)
                  .param("customUrlSuffix", CUSTOM_SUFFIX_INVALID))
            .andExpect(status().isBadRequest());
   }

   @Test
   public void givenInvalidUrl_simple_create() throws Exception {
      mvc.perform(MockMvcRequestBuilders.post(PATH_CREATE).param("url", INVALID_URL))
            .andExpect(status().isBadRequest());
   }

   @Test
   public void givenValidUrl_simple_create_and_then_navigate() throws Exception {
      repository.deleteAll();
      MvcResult shortenResult = mvc.perform(MockMvcRequestBuilders.post(PATH_CREATE).param("url", VALID_URL))
            .andExpect(status().isOk())
            .andReturn();
      String shortenedUrl = shortenResult.getResponse().getContentAsString();
      mvc.perform(MockMvcRequestBuilders.get(shortenedUrl))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(VALID_URL));
      List<ShortenedUrl> urls = repository.findAll();
      Assertions.assertThat(urls.size()).isEqualTo(1);
   }

   @Test
   public void givenValidButNotExistingUrl_navigate() throws Exception {
      repository.deleteAll();
      String shortenedUrl = appProperties.getBaseUrl() + NON_EXISTING_SHORT_URL_SUFFIX;
      mvc.perform(MockMvcRequestBuilders.get(shortenedUrl))
            .andExpect(status().isNotFound());
      List<ShortenedUrl> urls = repository.findAll();
      Assertions.assertThat(urls.size()).isEqualTo(0);
   }

   @Test
   public void navigate_and_check_stats_incremented() throws Exception {
      repository.deleteAll();
      MvcResult shortenResult = mvc.perform(MockMvcRequestBuilders.post(PATH_CREATE).param("url", VALID_URL))
            .andExpect(status().isOk())
            .andReturn();
      String shortenedUrl = shortenResult.getResponse().getContentAsString();

      mvc.perform(MockMvcRequestBuilders.get(shortenedUrl))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(VALID_URL));

      mvc.perform(MockMvcRequestBuilders.get(shortenedUrl))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(VALID_URL));

      MvcResult statsResult = mvc.perform(MockMvcRequestBuilders.get(PATH_STATISTICS).param("url", shortenedUrl))
            .andExpect(status().isOk())
            .andReturn();
      Long clickedTimes = Long.valueOf(statsResult.getResponse().getContentAsString());
      Assertions.assertThat(clickedTimes).isEqualTo(2);
   }

   @Test
   public void givenNonExistingUrl_check_stats_not_found() throws Exception {
      repository.deleteAll();
      String shortenedUrl = appProperties.getBaseUrl() + NON_EXISTING_SHORT_URL_SUFFIX;
      mvc.perform(MockMvcRequestBuilders.get(PATH_STATISTICS).param("url", shortenedUrl))
            .andExpect(status().isNotFound());
   }

}
