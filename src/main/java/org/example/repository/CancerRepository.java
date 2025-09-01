// CancerRepository.java  
package org.example.repository;

import org.example.domain.Cancer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CancerRepository extends JpaRepository<Cancer, Integer> {

    // 암 이름으로 조회
    Optional<Cancer> findByCancerName(String cancerName);
}