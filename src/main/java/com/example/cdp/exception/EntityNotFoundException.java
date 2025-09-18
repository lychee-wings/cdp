package com.example.cdp.exception;

public class EntityNotFoundException extends RuntimeException {

  public EntityNotFoundException(Class<?> entityClass) {
    super(entityClass.getSimpleName() + " is not found!");
  }
}
