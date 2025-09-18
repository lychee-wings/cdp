package com.example.cdp.controller;

import com.example.cdp.CdpApplication;
import com.example.cdp.config.batch.BatchConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest
@SpringJUnitConfig({CdpApplication.class, BatchConfig.class})
class JobLauncherControllerTest {

  private static final Path RESOURCES_DIRECTORY = Paths.get("src", "test", "resources");
  private static Path validFile;
  private static Path errorFile;
  @Autowired
  public JobLauncherTestUtils jobLauncherTestUtils;

  @BeforeAll
  public static void setupAll(@Autowired JdbcTemplate jdbcTemplate) throws IOException {

    if (RESOURCES_DIRECTORY.toFile().exists() || RESOURCES_DIRECTORY.toFile().mkdir()) {
      System.out.println("Creating path for storing csv files!");
    }

    validFile = Files.writeString(RESOURCES_DIRECTORY.resolve("validInput.csv"), """
        Date,Open,High,Low,Close,Adj Close,Volume
        14/2/25,44.88,44.96,44.8,44.84,44.84,4145900
        """);

    errorFile = Files.writeString(RESOURCES_DIRECTORY.resolve("errorInput.csv"), """
        Date,Open,High,Low,Close,Adj Close,Volume
        16/2/25,44.88,44.96,44.8,44.84,44.84,4145900
        15/2/25,44.88,44.96,44.8,44.84,44.84,4145900
        14/2/25,44.88,44.96,44.8,44.84,44.84,-
        14,44.88,44.96,44.8,44.84,44.84,4145900
        """);

    jdbcTemplate.batchUpdate(
        "INSERT INTO securities (stock_name, symbol) VALUES ('test_stock', 'test01');");
  }

  @BeforeEach
  void setUp(@Autowired JobLauncher syncJobLauncher) throws IOException {
    this.jobLauncherTestUtils.setJobLauncher(syncJobLauncher);

    File file = RESOURCES_DIRECTORY.toAbsolutePath().toFile();

    // true if the directory was created, false otherwise
    if (file.exists() || file.mkdir()) {
      System.out.printf("Directory - %s has been created!%n", file.getPath());
    } else {
      System.out.println("Failed to create directory!");
    }

  }

  @AfterEach
  void tearDown() {

  }

  @Test
  @DisplayName("GIVEN valid csv file WHEN jobLaunched THEN record shall be updated")
  void importDailyPrice(@Autowired Job jobImportDailyPrice)
      throws Exception {
    this.jobLauncherTestUtils.setJob(jobImportDailyPrice);

    JobParameters jobParameters = new JobParametersBuilder()//
        .addString("filePath", validFile.toString())//
        .addString("stockName", "test_stock")//
        .addLong("startAt", System.currentTimeMillis()).toJobParameters();

    JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

    jobExecution.getStepExecutions() //
        .forEach(stepExecution -> {
          String information = String.format(" >>> [QR Debug] %s", stepExecution.getSummary());
          System.out.println(information);
          Assertions.assertEquals(ExitStatus.COMPLETED, stepExecution.getExitStatus());
        });

    Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
  }

  @Test
  @DisplayName("GIVEN invalid csv record WHEN jobLaunched THEN skip count shall be recorded")
  void importDailyPriceInvalid(@Autowired Job jobImportDailyPrice)
      throws Exception {
    this.jobLauncherTestUtils.setJob(jobImportDailyPrice);

    JobParameters jobParameters = new JobParametersBuilder()//
        .addString("filePath", errorFile.toString())//
        .addString("stockName", "test_stock")//
        .addLong("startAt", System.currentTimeMillis()).toJobParameters();
    JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

    jobExecution.getStepExecutions() //
        .forEach(stepExecution -> {
          String information = String.format(" >>> [QR Debug] %s", stepExecution.getSummary());
          System.out.println(information);
          Assertions.assertEquals(ExitStatus.COMPLETED, stepExecution.getExitStatus());
        });
    Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
  }
}

/**
 * To have a sync jobImportTransaction launcher to test the behavior of batch steps.
 */
@Configuration
class JunitBatchConfig {

  @Bean
  public JobLauncher syncJobLauncher(@Autowired JobRepository jobRepository) throws Exception {
    TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
    jobLauncher.setJobRepository(jobRepository);
    jobLauncher.afterPropertiesSet();
    return jobLauncher;
  }
}