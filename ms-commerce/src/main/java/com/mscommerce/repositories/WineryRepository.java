package com.mscommerce.repositories;

import com.mscommerce.models.Winery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WineryRepository extends JpaRepository<Winery, Integer> {

    Optional<Winery> findById(Integer id);
}
