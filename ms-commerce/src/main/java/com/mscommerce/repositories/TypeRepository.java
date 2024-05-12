package com.mscommerce.repositories;

import com.mscommerce.models.Type;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TypeRepository extends JpaRepository<Type, Integer> {

    Optional<Type> findById(Integer id);
}
