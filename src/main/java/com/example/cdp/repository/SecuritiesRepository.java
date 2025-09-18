package com.example.cdp.repository;

import com.example.cdp.model.Securities;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecuritiesRepository extends JpaRepository<Securities, Long> {

  List<Securities> findByStockName(String stockName);

  List<Securities> findBySymbol(String symbol);


}
