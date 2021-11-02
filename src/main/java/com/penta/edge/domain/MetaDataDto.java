package com.penta.edge.domain;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MetaDataDto {


    private String dataID;

    private LocalDateTime timestamp;

    private String fileType;

    private Integer dataType;

    private Integer securityLevel;

    private Integer dataPriority;

    private Integer availabilityPolicy;

    private String dataSignature;

    private String cert;

    private String directory;

    private String linkedEdge;

    private Long dataSize;

    public MetaData toMetaData() {
        return MetaData.builder()
                .dataID(this.dataID)
                .timestamp(this.timestamp)
                .fileType(this.fileType)
                .dataType(this.dataType)
                .securityLevel(this.securityLevel)
                .dataPriority(this.dataPriority)
                .availabilityPolicy(this.availabilityPolicy)
                .dataSignature(this.dataSignature)
                .cert(this.cert)
                .directory(this.directory)
                .linkedEdge(this.linkedEdge)
                .dataSize(this.dataSize)
                .build();
    }


}
