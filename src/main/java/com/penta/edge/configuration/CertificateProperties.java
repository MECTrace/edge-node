package com.penta.edge.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "server.ssl")
@ConstructorBinding
@RequiredArgsConstructor
@Getter
public class CertificateProperties {

    private final String keyStore;
    private final String keyStorePassword;
    private final String keyAlias;
    private final String trustStore;
    private final String trustStorePassword;

}
