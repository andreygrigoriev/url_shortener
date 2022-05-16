package org.sample.shortener;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@Profile("test")
@SpringBootApplication(scanBasePackages={"org.sample.shortener"})
public class TestConfig {
}
