package com.penta.edge.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "edge-info")
@Getter
@ConstructorBinding
@RequiredArgsConstructor
public class EdgeInfoProperties {

    // jar 배포시 배포 옵션으로 properties값을 변경하여 각 edge에 맞는 uuid와 ip를 할당하기 위함
    private final String uuid;
    private final String ip;

}
