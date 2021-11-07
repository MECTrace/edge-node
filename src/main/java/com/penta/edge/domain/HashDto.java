package com.penta.edge.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class HashDto {

    private String dataId;

    private LocalDateTime timestamp;

    private String sourceId;

    private String destinationId;



}
