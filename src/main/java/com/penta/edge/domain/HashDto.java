package com.penta.edge.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class HashDto {

    private String dataID;

    private LocalDateTime timestamp;

    private String sourceID;

    private String destinationID;



}
