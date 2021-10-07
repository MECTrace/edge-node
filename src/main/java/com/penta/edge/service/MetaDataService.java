package com.penta.edge.service;

import com.penta.edge.domain.MetaData;
import com.penta.edge.repository.MetaDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetaDataService {

    private final MetaDataRepository metaDataRepository;
    public void save(MetaData metaData) {
        metaDataRepository.save(metaData);
    }

}
