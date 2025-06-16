package com.extractor.unraveldocs.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.document")
@Getter
@Setter
@Validated
public class DocumentConfigProperties {
    @NotEmpty
    private List<String> allowedFileTypes;

    @NotEmpty
    private String storageFolder;
}
