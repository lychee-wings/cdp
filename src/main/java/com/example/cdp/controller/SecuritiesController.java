package com.example.cdp.controller;

import com.example.cdp.model.Securities;
import com.example.cdp.service.SecuritiesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/securities")
public class SecuritiesController {

  @Autowired
  SecuritiesService securitiesService;

  @GetMapping("/id/{id}")
  public ResponseEntity<Object> readSecuritiesById(@PathVariable Long id) {
    return ResponseEntity.ok(securitiesService.getSecuritiesById(id));
  }

  @GetMapping
  public ResponseEntity<Object> readAllSecurities() {
    return ResponseEntity.ok().body(securitiesService.getSecurities());
  }

  @GetMapping("/symbol/{symbol}")
  public ResponseEntity<Object> readSecuritiesBySymbol(@PathVariable String symbol) {
    return ResponseEntity.ok().body(securitiesService.getSecuritiesBySymbol(symbol));
  }

  @GetMapping("/stock-name/{stockName}")
  public ResponseEntity<Object> readSecuritiesByStockName(@PathVariable String stockName) {
    return ResponseEntity.ok().body(securitiesService.getSecuritiesByStockName(stockName));
  }

  @PostMapping
  public ResponseEntity<Object> updateSecurities(@RequestBody Securities securities) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(securitiesService.saveSecurities(securities));

  }

}
