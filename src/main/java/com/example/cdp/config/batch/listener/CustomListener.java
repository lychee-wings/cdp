package com.example.cdp.config.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CustomListener {

  @Bean
  public ExecutionContextPromotionListener securitiesListener() {

    ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
    listener.setKeys(new String[]{"securities"});
    return listener;
  }
}
