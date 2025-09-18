package com.example.cdp.model;

import com.example.cdp.model.Transaction.Action;
import com.example.cdp.model.Transaction.Settlement;
import com.example.cdp.model.Transaction.TransactionStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionTest {

  private Validator validator;

  @BeforeEach
  public void setUp() {

    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  void createOrderTestValid() {

    Securities securities = new Securities(1L, "Testing", "test");

    Transaction order = Transaction.builder() //
        .id(1L).orderDateTime(Timestamp.valueOf(LocalDateTime.now())).securities(securities)
        .market("SG").action(Action.BUY).quantity(1000).price(1.0d).filledQuantity(1000)
        .status(TransactionStatus.FILLED).settlement(Settlement.CASH).build();

    Set<ConstraintViolation<Transaction>> violationSet = validator.validate(order);

    MatcherAssert.assertThat(violationSet, Matchers.empty());
  }

  @Test
  void createOrderTestNotNull() {

    Set<ConstraintViolation<Transaction>> violationSet;
    Securities securities = new Securities(1L, "Testing", "test");

    violationSet = validator.validate(
        new Transaction(1L, Timestamp.valueOf(LocalDateTime.now()), securities, "SG", null, 1000,
            1.0d, 1000, TransactionStatus.FILLED, Settlement.CASH, null));
    MatcherAssert.assertThat(violationSet, Matchers.hasSize(1));

    violationSet = validator.validate(
        new Transaction(1L, Timestamp.valueOf(LocalDateTime.now()), securities, "SG", Action.BUY,
            1000, 1.0d, 1000, null, Settlement.CASH, null));
    MatcherAssert.assertThat(violationSet, Matchers.hasSize(1));

    violationSet = validator.validate(
        new Transaction(1L, Timestamp.valueOf(LocalDateTime.now()), null, "SG", Action.BUY, 1000,
            1.0d, 1000, TransactionStatus.FILLED, Settlement.CASH, null));
    MatcherAssert.assertThat(violationSet, Matchers.hasSize(1));

  }

  @Test
  void createOrderTestIsPositive() {

    Set<ConstraintViolation<Transaction>> violationSet;
    Securities securities = new Securities(1L, "Testing", "test");

    violationSet = validator.validate(
        new Transaction(1L, Timestamp.valueOf(LocalDateTime.now()), securities, "SG", Action.BUY, 0,
            1.0d, 1000, TransactionStatus.FILLED, Settlement.CASH, null));
    MatcherAssert.assertThat(violationSet, Matchers.hasSize(1));

    violationSet = validator.validate(
        new Transaction(1L, Timestamp.valueOf(LocalDateTime.now()), securities, "SG", Action.BUY,
            1000, 0.0d, 1000, TransactionStatus.FILLED, Settlement.CASH, null));
    MatcherAssert.assertThat(violationSet, Matchers.hasSize(1));
  }

  @Test
  void createOrderTestTimeHasPast() {

    Set<ConstraintViolation<Transaction>> violationSet;
    Securities securities = new Securities(1L, "Testing", "test");

    violationSet = validator.validate(
        new Transaction(1L, Timestamp.valueOf(LocalDateTime.now().plusDays(1)), securities, "SG",
            Action.BUY, 1000, 1.0d, 1000, TransactionStatus.FILLED, Settlement.CASH, null));
    MatcherAssert.assertThat(violationSet, Matchers.hasSize(1));

  }
}