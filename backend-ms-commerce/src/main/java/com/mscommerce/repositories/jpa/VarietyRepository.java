package com.mscommerce.repositories.jpa;

import com.mscommerce.models.Variety;
import com.mscommerce.repositories.interfaces.IVarietyRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VarietyRepository extends JpaRepository<Variety, Integer>, IVarietyRepository {
}
