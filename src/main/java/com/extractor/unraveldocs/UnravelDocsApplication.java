package com.extractor.unraveldocs;

import io.awspring.cloud.autoconfigure.core.AwsAutoConfiguration;
import io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
        S3AutoConfiguration.class,
        AwsAutoConfiguration.class
})
@EnableScheduling
@EnableAsync
@EnableCaching
public class UnravelDocsApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnravelDocsApplication.class, args);
    }

}
