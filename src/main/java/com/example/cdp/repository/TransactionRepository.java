package com.example.cdp.repository;

import com.example.cdp.model.Transaction;
import com.example.cdp.model.Transaction.Action;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>,
    TransactionCustomQuery {

  @Query(value = """
      SELECT * FROM Transaction txn 
      WHERE txn.securities_id IN (SELECT s.id FROM Securities s WHERE s.stock_name = :stockName)
      """, nativeQuery = true)
  List<Transaction> findBySecurities(@Param("stockName") String stockName);

  List<Transaction> findBySecurities_StockNameIgnoreCase(String stockName);

  List<Transaction> findByAction(Action action);


}

