package com.example.cdp.config.batch.reader;

import static com.example.cdp.constant.AppConstant.JOB_PARAMS_STOCK_NAME;
import static com.example.cdp.constant.AppConstant.STEP_EXECUTION_SECURITIES;

import com.example.cdp.model.Securities;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

public class SecuritiesJdbcReader extends JdbcCursorItemReader<Securities> {

//  private Map<String, Securities> securitiesMap;

  private Securities securities = new Securities();

  public SecuritiesJdbcReader() {

    this.setName("ReaderSecurities"); //
    this.setSql("SELECT * FROM securities WHERE stock_name LIKE ?;");
    this.setRowMapper(new BeanPropertyRowMapper<>(Securities.class));
  }

  @Override
  public Securities read() throws Exception {

    Securities read = super.read();

    if (read != null) {
      this.securities = read;
    }
    return read;
  }

  @Override
  public void update(ExecutionContext executionContext) throws ItemStreamException {
    executionContext.put(STEP_EXECUTION_SECURITIES, this.securities);
    super.update(executionContext);
  }

  @BeforeStep
  public void beforeStep(StepExecution stepExecution) {

    this.setPreparedStatementSetter(new ArgumentPreparedStatementSetter(
        new Object[]{stepExecution.getJobParameters().getString(JOB_PARAMS_STOCK_NAME, "")}));

    // get the executionContext and set the key

    this.securities = stepExecution.getExecutionContext()
        .get(STEP_EXECUTION_SECURITIES, Securities.class, null);


  }

}
