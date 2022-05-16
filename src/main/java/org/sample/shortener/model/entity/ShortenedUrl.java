package org.sample.shortener.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Entity
@Table(name = "shortened_url")
@NoArgsConstructor
public class ShortenedUrl {
   private static final long serialVersionUID = 1L;

   @Id
   private String shortenedSuffix;
   private String originalUrl;
   @Column(columnDefinition = "TIMESTAMP")
   private LocalDateTime lastNavigated;
   private Long timesClicked = 0L;

   public ShortenedUrl(String shortenedSuffix, String originalUrl) {
      this.shortenedSuffix = shortenedSuffix;
      this.originalUrl = originalUrl;
      this.lastNavigated = LocalDateTime.now(ZoneId.of("UTC"));
   }

   public void incrementTimesClicked() {
      this.timesClicked++;
      this.lastNavigated = LocalDateTime.now(ZoneId.of("UTC"));
   }
}
