package com.mscommerce.repositories;

import com.mscommerce.models.Variety;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VarietyRepository extends JpaRepository<Variety, Integer> {

    Optional<Variety> findById(Integer id);
}
