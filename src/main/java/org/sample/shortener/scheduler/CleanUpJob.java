package org.sample.shortener.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.sample.shortener.repository.ShortenedUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@SuppressWarnings("unused")
public class CleanUpJob {

   private final ShortenedUrlRepository shortenedUrlRepository;

   @Autowired
   public CleanUpJob(ShortenedUrlRepository shortenedUrlRepository) {
      this.shortenedUrlRepository = shortenedUrlRepository;
   }

   @Scheduled(cron = "${application.scheduler.clear-clicked-data-job-cron}")
   public void clearClickedData() {
      log.info("Clearing clicked statistics");
      shortenedUrlRepository.clearClickedTimes();
   }
}
