package com.extractor.unraveldocs.config;

import com.extractor.unraveldocs.utils.generatetoken.GenerateVerificationToken;
import com.extractor.unraveldocs.utils.userlib.DateHelper;
import com.extractor.unraveldocs.utils.userlib.UserLibrary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilsConfig {

    @Bean
    public GenerateVerificationToken generateToken() {
        return new GenerateVerificationToken();
    }

    @Bean
    public UserLibrary userLibrary() {
        return new UserLibrary();
    }

    @Bean
    public DateHelper dateHelper() {
        return new DateHelper();
    }
}
