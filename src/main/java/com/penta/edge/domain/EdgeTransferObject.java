package com.penta.edge.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class EdgeTransferObject {

    private MultipartFile dataFile;
    private MultipartFile certFile;
    private MultipartFile signature;

    private MetaData metaData;
    private Hash hash;

}
