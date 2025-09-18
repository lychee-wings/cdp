package com.example.cdp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Version {

  // server name with default name by machine
  @Value("${server.name}")
  private String server;
  // server version with default version by machine
  private String version = "1.0.0";
}