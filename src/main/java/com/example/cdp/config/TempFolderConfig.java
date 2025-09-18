package com.example.cdp.config;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class TempFolderConfig {

  @Value("${temp.folder}")
  private String tempFolder;

  @PostConstruct
  public void createTempFolder() {
    File file = Paths.get("", tempFolder).toAbsolutePath().toFile();

    // true if the directory was created, false otherwise
    if (file.exists() || file.mkdir()) {
      log.info("Directory - {} has been created!", file.getPath());
    } else {
      log.error("Failed to create directory!");
    }
  }

}
