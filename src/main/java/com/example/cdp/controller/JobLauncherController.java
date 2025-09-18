package com.example.cdp.controller;

import static com.example.cdp.constant.AppConstant.JOB_PARAMS_FILE_PATH;
import static com.example.cdp.constant.AppConstant.JOB_PARAMS_STOCK_NAME;

import com.example.cdp.constant.AppConstant;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/batch")
public class JobLauncherController {

//  @Autowired
//  JobLauncher jobLauncher;

  @Autowired
  JobLauncher asyncJobLauncher;

  @Value("${temp.folder}")
  String tempFolder;

  @Autowired
  Job jobImportDailyPrice;

  @Autowired
  Job jobImportTransaction;

  @PostMapping(path = "/daily-price/stock-name/{stockName}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Object> importDailyPrice(
      @RequestParam("csv") MultipartFile multipartFile,
      @PathVariable String stockName) {

    try {

      Path path = Paths.get("", tempFolder, multipartFile.getOriginalFilename()).toAbsolutePath();

      multipartFile.transferTo(path);

      JobParameters jobParameters = new JobParametersBuilder()//
          .addString(JOB_PARAMS_FILE_PATH, path.toString())
          .addString(JOB_PARAMS_STOCK_NAME, stockName)
          .addLong(AppConstant.JOB_PARAMS_START_AT, System.currentTimeMillis())
          .toJobParameters();

      JobExecution jobExecution = asyncJobLauncher.run(jobImportDailyPrice, jobParameters);

      Map<String, Object> response = new HashMap<>();
      response.put("jobInstance", jobExecution.getJobInstance());
      response.put("status", jobExecution.getStatus());
      response.put("startTime", jobExecution.getStartTime());
      response.put("endTime", jobExecution.getEndTime());

      return ResponseEntity.ok(response);

    } catch (JobExecutionAlreadyRunningException | JobRestartException |
             JobInstanceAlreadyCompleteException | JobParametersInvalidException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  @PostMapping(path = "/transactions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Object> importTransaction(
      @RequestParam("csv") MultipartFile multipartFile) {

    try {

      Path path = Paths.get("", tempFolder, multipartFile.getOriginalFilename()).toAbsolutePath();

      multipartFile.transferTo(path);

      JobParameters jobParameters = new JobParametersBuilder()//
          .addString(JOB_PARAMS_FILE_PATH, path.toString())
          .addLong(AppConstant.JOB_PARAMS_START_AT, System.currentTimeMillis())
          .toJobParameters();

      JobExecution jobExecution = asyncJobLauncher.run(jobImportTransaction, jobParameters);

      Map<String, Object> response = new HashMap<>();
      response.put("jobInstance", jobExecution.getJobInstance());
      response.put("status", jobExecution.getStatus());
      response.put("startTime", jobExecution.getStartTime());
      response.put("endTime", jobExecution.getEndTime());

      return ResponseEntity.ok(response);

    } catch (JobExecutionAlreadyRunningException | JobRestartException |
             JobInstanceAlreadyCompleteException | JobParametersInvalidException | IOException e) {
      throw new RuntimeException(e);
    }
  }

}
