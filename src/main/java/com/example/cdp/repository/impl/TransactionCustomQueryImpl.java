package com.example.cdp.repository.impl;

import com.example.cdp.model.Transaction;
import com.example.cdp.model.Transaction.Action;
import com.example.cdp.model.Transaction.TransactionStatus;
import com.example.cdp.repository.TransactionCustomQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;

public class TransactionCustomQueryImpl implements TransactionCustomQuery {

  @Autowired
  private EntityManager entityManager;

  @Override
  public List<Transaction> customizedQuery(
      String stockName,
      Action action, TransactionStatus transactionStatus) {
    System.out.println("[Qr Debug]" + Thread.currentThread().getStackTrace()[1].getMethodName());

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

    CriteriaQuery<Transaction> criteriaQuery = criteriaBuilder.createQuery(Transaction.class);
    Root<Transaction> transactionRoot = criteriaQuery.from(Transaction.class);

    List<Predicate> conditions = new ArrayList<>();

    // Predicate for the stockName.
    conditions.add(stockName == null || stockName.isBlank() ? null
        : criteriaBuilder.equal(
            criteriaBuilder.upper(transactionRoot.get("securities").get("stockName")),
            stockName.toUpperCase()));

    // Predicate for the stockName.
    conditions.add(action == null ? null
        : criteriaBuilder.equal(transactionRoot.get("action"),
            action));

    // Predicate for the stockName.
    conditions.add(transactionStatus == null ? null
        : criteriaBuilder.equal(transactionRoot.get("status"),
            transactionStatus));

    criteriaQuery.where(criteriaBuilder.and(
        conditions.stream().filter(Objects::nonNull).toArray(Predicate[]::new)));

    return entityManager.createQuery(criteriaQuery).getResultList();
  }
}
