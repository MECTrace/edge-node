package com.penta.edge.service;

import com.penta.edge.domain.Hash;
import com.penta.edge.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HashService {

    private final HashRepository hashRepository;

    public void save(Hash hash) {
        hashRepository.save(hash);
    }

}
