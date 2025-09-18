package com.example.cdp.repository;

import com.example.cdp.model.PriceDaily;
import java.sql.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceDailyRepository extends JpaRepository<PriceDaily, Long> {

  List<PriceDaily> findByDateBetween(Date startDate, Date endDate);

}
