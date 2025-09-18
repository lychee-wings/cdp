package com.example.cdp.controller;


import com.example.cdp.service.batch.SpringBatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class JobController {

  @Autowired
  private SpringBatchService batchService;

  @Autowired
  private JobExplorer jobExplorer;

  @GetMapping("/jobs/names")
  public ResponseEntity<Object> findAllJobNames() {
    return ResponseEntity.ok(jobExplorer.getJobNames());
  }

  @GetMapping("/jobs/id/{jobId}")
  public ResponseEntity<Object> findJobById(@PathVariable Long jobId) {

    JobInstance jobInstance = jobExplorer.getJobInstance(jobId);
    if (jobInstance == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(jobInstance);
  }

  @GetMapping("/jobs/name/{jobName}/last")
  public ResponseEntity<Object> findLastJobByName(@PathVariable String jobName) {

    JobInstance jobInstance = jobExplorer.getLastJobInstance(jobName);

    if (jobInstance == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(jobInstance);
  }

  @GetMapping("/jobs/name/{jobName}")
  public ResponseEntity<Object> findJobsByName(@PathVariable String jobName,
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false, defaultValue = "0") Integer after) {

    try {

      limit = limit == null ? (int) jobExplorer.getJobInstanceCount(jobName) : limit;
      return ResponseEntity.ok(jobExplorer.getJobInstances(jobName, after, limit));
    } catch (NoSuchJobException ex) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/jobs/id/{jobId}/executions")
  public ResponseEntity<Object> findJobExecutionByJobId(@PathVariable Long jobId) {
    JobInstance jobInstance = jobExplorer.getJobInstance(jobId);
    if (jobInstance == null) {
      return ResponseEntity.notFound().build();
    }
    List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(jobInstance);

    // simple response

    List<Map<String, Object>> list = jobExecutions.stream()
        .peek(jobExecution -> {
          System.out.println("The jobExecution's id : " + jobExecution.getJobId());
          jobExecution.getStepExecutions().forEach(stepExecution -> System.out.println(
              "The step summary : " + stepExecution.getSummary()));
        }).map(jobExecution -> {
          Map<String, Object> simpleResponse = new HashMap<>();
          simpleResponse.put("jobInstance", jobExecution.getJobInstance());
          simpleResponse.put("status", jobExecution.getStatus());
          simpleResponse.put("startTime", jobExecution.getStartTime());
          simpleResponse.put("endTime", jobExecution.getEndTime());
          return simpleResponse;
        }).toList();

    return ResponseEntity.ok(list);
  }

  @GetMapping("/executions/id/{executionId}")
  public ResponseEntity<Object> findJobExecutionByExecutionId(@PathVariable Long executionId) {
    JobExecution jobExecution = jobExplorer.getJobExecution(executionId);
    if (jobExecution == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(jobExecution);
  }
}
