package com.mscommerce.repositories.jpa;

import com.mscommerce.models.Type;
import com.mscommerce.repositories.interfaces.ITypeRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeRepository extends JpaRepository<Type, Integer>, ITypeRepository {
}
