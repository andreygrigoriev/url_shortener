package org.sample.shortener.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("application")
public class AppProperties {
   private Integer shortenUrlLength = 6;
   private String baseUrl = "http://localhost:7001/";
}