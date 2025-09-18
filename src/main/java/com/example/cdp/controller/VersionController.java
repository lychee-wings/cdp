package com.example.cdp.controller;

import com.example.cdp.model.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/version")
public class VersionController {

  @Autowired
  private Version version;

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<Version> getVersion() {
    return ResponseEntity.ok(version);
  }
}
