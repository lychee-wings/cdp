package com.example.cdp.repository;

import com.example.cdp.model.Transaction;
import com.example.cdp.model.Transaction.Action;
import com.example.cdp.model.Transaction.TransactionStatus;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionCustomQuery {

  List<Transaction> customizedQuery(String stockName,
      Action action, TransactionStatus transactionStatus);
}
