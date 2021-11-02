package com.penta.edge.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class RequestDto {

    private String uuid;                    // 요청 edge의 UUID
    private List<MultipartFile> datafile;
    private List<MultipartFile> certfile;
    private MetaDataDto metadata;
    private HashDto hash;


}
