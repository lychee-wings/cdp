package com.example.cdp.service;

import com.example.cdp.exception.EntityNotFoundException;
import com.example.cdp.model.Securities;
import com.example.cdp.repository.SecuritiesRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SecuritiesService {

  @Autowired
  private SecuritiesRepository securitiesRepository;

  public Securities getSecuritiesById(Long id) {

    return securitiesRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
        Securities.class));
  }

  public List<Securities> getSecurities() {
    return securitiesRepository.findAll();
  }

  public List<Securities> getSecuritiesByStockName(String stockName) {

    return securitiesRepository.findByStockName(stockName);
  }

  public List<Securities> getSecuritiesBySymbol(String symbol) {

    return securitiesRepository.findBySymbol(symbol);
  }

  public Securities saveSecurities(Securities securities) {

    try {
      return securitiesRepository.save(securities);
    } catch (Exception ex) {
      throw new EntityNotFoundException(Securities.class);
    }
  }
}
