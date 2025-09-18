package com.example.cdp.config.batch;

import com.example.cdp.config.batch.listener.ReadListener;
import com.example.cdp.model.PriceDaily;
import com.example.cdp.model.Securities;
import com.example.cdp.model.Transaction;
import com.example.cdp.repository.PriceDailyRepository;
import com.example.cdp.repository.TransactionRepository;
import java.io.File;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
//@EnableBatchProcessing() // this is no longer recommended: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide#spring-batch-changes
public class BatchConfig {

  private final PriceDailyRepository priceDailyRepository;
  private final TransactionRepository transactionRepository;


  private final JobRepository jobRepository;
  private final PlatformTransactionManager platformTransactionManager;
  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Bean
  public JobLauncher asyncJobLauncher() throws Exception {
    TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
    jobLauncher.setJobRepository(jobRepository);
    jobLauncher.afterPropertiesSet();
    jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
    return jobLauncher;
  }

  // ======================================================
  // + Readers +
  // ======================================================
  @Bean
  @StepScope
  public FlatFileItemReader<PriceDaily> priceDailyFileItemReader(
      @Value("#{jobParameters[filePath]}") String csvFile,
      BeanWrapperFieldSetMapper<PriceDaily> priceDailyBeanWrapper) {

    FlatFileItemReader<PriceDaily> fileItemReader;

    fileItemReader = new FlatFileItemReaderBuilder<PriceDaily>()//
        .name("ReaderPriceDaily").resource(new FileSystemResource(new File(csvFile))) //
        .linesToSkip(1).delimited()
        .names("date", "open", "high", "low", "close", "adjClose", "volume") //
        .fieldSetMapper(priceDailyBeanWrapper) //
        .strict(false) //
        .build();
    return fileItemReader;
  }

  @Bean
  @StepScope
  public FlatFileItemReader<Transaction> transactionItemReader(
      @Value("#{jobParameters[filePath]}") String csvFile,
      BeanWrapperFieldSetMapper<Transaction> transactionBeanWrapper) {

    FlatFileItemReader<Transaction> fileItemReader;

    fileItemReader = new FlatFileItemReaderBuilder<Transaction>()//
        .name("ReaderTransaction").resource(new FileSystemResource(new File(csvFile))) //
        .linesToSkip(1).delimited()
        .names("Order Date/Time", "Symbol", "Stock Name/Transaction ID", "Market", "Action",
            "Quantity", "Price", "Settlement Mode", "Filled Quantity", "Filled Price",
            "Outstanding Quantity", "Status", "Input Value") //
        .fieldSetMapper(transactionBeanWrapper) //
        .strict(false) //
        .build();

    return fileItemReader;
  }

  // ======================================================
  // + Writer +
  // ======================================================

  @Bean
  public RepositoryItemWriter<PriceDaily> repositoryPriceDailyWriter() {
    return new RepositoryItemWriterBuilder<PriceDaily>().repository(priceDailyRepository).build();
  }

  @Bean
  public RepositoryItemWriter<Transaction> repositoryTransactionWriter() {
    return new RepositoryItemWriterBuilder<Transaction>().repository(transactionRepository).build();
  }

  // ======================================================
  // + Step +
  // ======================================================
  @Bean
  public Step importDailyPrice(FlatFileItemReader<PriceDaily> priceDailyFileItemReader,
      ReadListener readErrorListener, RepositoryItemWriter<PriceDaily> repositoryPriceDailyWriter,
      ItemWriter<Object> itemWriterPrintToConsole) {

    return new StepBuilder("Step-WriteDailyPrice", jobRepository) //
        .<PriceDaily, PriceDaily>chunk(100, platformTransactionManager) //
        .reader(priceDailyFileItemReader) //
        .faultTolerant().skipLimit(100).skip(FlatFileParseException.class)
        .listener((StepExecutionListener) readErrorListener)//
//        .writer(new CompositeItemWriter<>(itemWriterPrintToConsole, repositoryPriceDailyWriter)) // for debugging
        .writer(repositoryPriceDailyWriter) //
        .build();
  }

  @Bean
  public Step importTransaction(FlatFileItemReader<Transaction> transactionItemReader,
      ReadListener readErrorListener, RepositoryItemWriter<Transaction> repositoryTransactionWriter,
      ItemWriter<Object> itemWriterPrintToConsole) {

    return new StepBuilder("Step-WriteTransaction", jobRepository) //
        .<Transaction, Transaction>chunk(100, platformTransactionManager) //
        .reader(transactionItemReader) //
        .faultTolerant().skipLimit(100).skip(FlatFileParseException.class)
        .listener((StepExecutionListener) readErrorListener)//
        .processor(item -> {
          String sql = "SELECT * FROM securities WHERE symbol LIKE ?;";
          try {
            Securities securities = jdbcTemplate.queryForObject(sql, //
                new BeanPropertyRowMapper<>(Securities.class),
                item.getSecurities().getSymbol());
            item.setSecurities(securities);
          } catch (DataAccessException e) {
            throw new RuntimeException("Unknown or ambiguous securities!!");
          }

          return item;
        })
//        .writer(new CompositeItemWriter<>(itemWriterPrintToConsole, repositoryTransactionWriter)) // for debugging
        .writer(repositoryTransactionWriter) //
        .build();
  }

  @Bean
  protected Step readSecurities(ExecutionContextPromotionListener securitiesListener) {

    return new StepBuilder("Step-ReadSecurity", jobRepository)//
        .tasklet(new Tasklet() {
          @Override
          public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
              throws Exception {
            String sql = "SELECT * FROM securities WHERE stock_name LIKE ?;";

            StepContext stepContext = chunkContext.getStepContext();
            Securities securities = null;
            try {
              securities = jdbcTemplate.queryForObject(sql, //
                  new BeanPropertyRowMapper<>(Securities.class),
                  stepContext.getJobParameters().get("stockName").toString());
            } catch (DataAccessException e) {
              throw new RuntimeException("Unknown or ambiguous securities!!");
            }

            // set the step context to feed for next step
            stepContext.getStepExecution().getExecutionContext().put("securities", securities);

            return RepeatStatus.FINISHED;
          }

        }, platformTransactionManager)//
        .listener(securitiesListener).build();
  }

  @Bean
  protected Job jobImportDailyPrice(Step readSecurities, Step importDailyPrice) {
    return new JobBuilder("Job-ImportDailyPrice", jobRepository) //
        .start(readSecurities) //
        .on(ExitStatus.FAILED.toString()).end() //
        .from(readSecurities).next(importDailyPrice) //
        .end() //
        .build();
  }

  @Bean
  protected Job jobImportTransaction(Step importTransaction) {
    return new JobBuilder("Job-ImportTransaction", jobRepository) //
        .start(importTransaction) //
        .build();
  }

  @Bean // for testing the reading
  protected Job runReadingJob(Step readSecurities, Step importDailyPrice) {
    return new JobBuilder("readSecuritiesJob", jobRepository) //
        .start(readSecurities) //
        .on(ExitStatus.FAILED.toString()).end().next(importDailyPrice).end().build();
  }

  // ======================================================
  // + Debug +
  // ======================================================
  @Bean
  public ItemWriter<Object> itemWriterPrintToConsole() {

    // for debug purpose
    return chunk -> {
      for (Object item : chunk) {
        System.out.println(item.toString());
      }
    };
  }
}
