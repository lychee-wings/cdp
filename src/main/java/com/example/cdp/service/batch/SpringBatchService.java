package com.example.cdp.service.batch;

import java.util.List;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpringBatchService {

  @Autowired
  private JobRepository jobRepository; // for CRUD scenario

  public List<JobExecution> findJobExecution(JobInstance jobInstance) {
    return jobRepository.findJobExecutions(jobInstance);
  }

  public List<JobInstance> findJobInstancesByJobName(String jobName, int start, int count) {
    return jobRepository.findJobInstancesByName(jobName, start, count);
  }

}
