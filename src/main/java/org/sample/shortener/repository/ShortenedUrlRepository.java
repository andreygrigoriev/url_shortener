package org.sample.shortener.repository;

import org.sample.shortener.model.entity.ShortenedUrl;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShortenedUrlRepository extends CrudRepository<ShortenedUrl, String> {
   List<ShortenedUrl> findAll();
   Optional<ShortenedUrl> findByOriginalUrl(String originalUrl);
   Optional<ShortenedUrl> findByShortenedSuffix(String shortenedSuffix);
   @Query(value = "update ShortenedUrl s set timesClicked = null")
   void clearClickedTimes();
}
