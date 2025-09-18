package com.example.cdp.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "temp.folder=testFolder"
})
class TempFolderConfigTest {

  @Autowired
  TempFolderConfig tempFolderConfig = Mockito.mock(TempFolderConfig.class);

  File file = Paths.get("", "testFolder").toAbsolutePath().toFile();

  @AfterEach
  public void deleteFolder() {

    // to delete the folder
    file.delete();
    assertFalse(file.exists());
  }

  @Test
  void createTempFolder() {

    tempFolderConfig.createTempFolder();
    assertTrue(file.exists());

  }
}