package com.penta.edge.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "file.upload.location")
@Getter
@ConstructorBinding
@RequiredArgsConstructor
public class FileUploadProperties {

    private final String vehicle;
    private final String cert;

}
