package com.penta.edge;

import com.penta.edge.configuration.CertificateProperties;
import com.penta.edge.configuration.EdgeInfoProperties;
import com.penta.edge.configuration.FileUploadProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
@EnableConfigurationProperties(
        {FileUploadProperties.class, CertificateProperties.class, EdgeInfoProperties.class}
)
@SpringBootApplication
public class EdgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(EdgeApplication.class, args);

    }


}
