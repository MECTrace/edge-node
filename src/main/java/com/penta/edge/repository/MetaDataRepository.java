package com.penta.edge.repository;

import com.penta.edge.domain.MetaData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetaDataRepository extends JpaRepository<MetaData,String> {
}
