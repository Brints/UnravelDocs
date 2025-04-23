package com.extractor.unraveldocs;

import io.awspring.cloud.autoconfigure.core.AwsAutoConfiguration;
import io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        S3AutoConfiguration.class,
        AwsAutoConfiguration.class
})
public class UnravelDocsApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnravelDocsApplication.class, args);
    }

}
