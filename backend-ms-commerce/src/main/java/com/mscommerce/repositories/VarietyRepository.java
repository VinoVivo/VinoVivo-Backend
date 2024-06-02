package com.mscommerce.repositories;

import com.mscommerce.models.Variety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VarietyRepository extends JpaRepository<Variety, Integer>, IVarietyRepository {
}
