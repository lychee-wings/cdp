package com.example.cdp.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SecuritiesTest {

  private Validator validator;

  @BeforeEach
  public void setUp() {

    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  void createOrderTestValid() {

    Securities securities = new Securities(1L, "Testing", "test");

    Set<ConstraintViolation<Securities>> violationSet = validator.validate(securities);

    MatcherAssert.assertThat(violationSet, Matchers.empty());
  }

  @Test
  void createOrderTestNotBlank() {

    Set<ConstraintViolation<Securities>> violationSet;

    violationSet = validator.validate(
        new Securities(1L, "", "test"));

    MatcherAssert.assertThat(violationSet, Matchers.hasSize(1));

    violationSet = validator.validate(
        new Securities(1L, "Testing", null));

    MatcherAssert.assertThat(violationSet, Matchers.hasSize(1));
  }

}