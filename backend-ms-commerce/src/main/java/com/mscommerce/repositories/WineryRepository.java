package com.mscommerce.repositories;

import com.mscommerce.models.Winery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WineryRepository extends JpaRepository<Winery, Integer>, IWineryRepository {
}
