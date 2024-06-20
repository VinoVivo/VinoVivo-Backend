package com.mscommerce.repositories.jpa;

import com.mscommerce.models.Winery;
import com.mscommerce.repositories.interfaces.IWineryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WineryRepository extends JpaRepository<Winery, Integer>, IWineryRepository {
}
