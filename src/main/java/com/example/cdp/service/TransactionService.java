package com.example.cdp.service;

import com.example.cdp.exception.EntityNotFoundException;
import com.example.cdp.model.Transaction;
import com.example.cdp.model.Transaction.Action;
import com.example.cdp.model.Transaction.TransactionStatus;
import com.example.cdp.repository.TransactionRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

  @Autowired
  private TransactionRepository transactionRepository;

  public Transaction findById(Long id) {

    return transactionRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(Transaction.class));
  }

  public Transaction saveTransaction(Transaction transaction) {

    try {
      return transactionRepository.save(transaction);
    } catch (Exception ex) {
      throw new EntityNotFoundException(Transaction.class);
    }
  }

  public List<Transaction> findTransactionByStockName(String stockName) {
    return transactionRepository.findBySecurities_StockNameIgnoreCase(stockName);
  }

  public List<Transaction> findTransactionByAction(Action action) {
    return transactionRepository.findByAction(action);
  }

  public List<Transaction> findTransactionByStockNameAndActionAndTransactionStatus(String stockName,
      Action action, TransactionStatus status) {

    return transactionRepository.customizedQuery(stockName,
        action, status);
  }
}
