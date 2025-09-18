package com.example.cdp.controller;

import com.example.cdp.model.Transaction;
import com.example.cdp.model.Transaction.Action;
import com.example.cdp.model.Transaction.TransactionStatus;
import com.example.cdp.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/transactions")
public class TransactionController {

  @Autowired
  TransactionService transactionService;

  @GetMapping("/id/{id}")
  public ResponseEntity<Object> readTransactionById(@PathVariable Long id) {

    return ResponseEntity.ok(transactionService.findById(id));
  }

  @PostMapping
  public ResponseEntity<Object> updateTransaction(@RequestBody Transaction transaction) {

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(transactionService.saveTransaction(transaction));
  }

  @GetMapping("/stock-name/{stockName}")
  public ResponseEntity<Object> readTransactionBySecuritiesStockName(
      @PathVariable String stockName) {

    return ResponseEntity.ok(transactionService.findTransactionByStockName(stockName));
  }

  @GetMapping("/action/{action}")
  public ResponseEntity<Object> readTransactionByAction(@PathVariable Action action) {

    return ResponseEntity.ok(transactionService.findTransactionByAction(action));
  }

  @GetMapping("/query")
  public ResponseEntity<Object> readTransactionByStockNameAndTransactionStatus(
      @RequestParam(name = "stock-name", required = false) String stockName,
      @RequestParam(name = "status", required = false) TransactionStatus status,
      @RequestParam(name = "action", required = false) Action action) {

    return ResponseEntity.ok(
        transactionService.findTransactionByStockNameAndActionAndTransactionStatus(stockName,
            action, status));
  }

}
