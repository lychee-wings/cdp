package com.example.cdp.config.batch.listener;

import static com.example.cdp.constant.AppConstant.JOB_PARAMS_FILE_PATH;

import com.example.cdp.model.BatchError;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Configuration
public class ReadListener implements SkipListener<Object, Object>,
    StepExecutionListener {

  @Autowired
  JdbcTemplate jdbcTemplate;

  String sql = """
      INSERT INTO 
      batch_error (created_at, original_value, line_number, error_msg, source, job_id)
      VALUES (?, ?, ?, ?, ?, ?);
      """;

  private List<BatchError> readErrors = new ArrayList<>();

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    // writing in the @code afterStep allow the errors to be written and recorded just before all
    // read -> process -> write has been completed.

    Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    // Writing my error records.
    int[] results = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps, int i) throws SQLException {
        BatchError error = readErrors.get(i);
        ps.setTimestamp(1, createdAt);
        ps.setString(2, error.getOriginalValue());
        ps.setInt(3, error.getLineNumber());
        ps.setString(4, error.getErrorMsg());
        ps.setString(5,
            stepExecution.getJobParameters().getString(JOB_PARAMS_FILE_PATH, ""));
        ps.setLong(6, stepExecution.getJobExecutionId());
      }

      @Override
      public int getBatchSize() {
        return readErrors.size();
      }
    });

    log.info("Inserted {} skips (error) row. Please refer to the record for detail!",
        IntStream.of(results).sum());

    if (!readErrors.isEmpty()) {
      readErrors.clear();
    }

    return StepExecutionListener.super.afterStep(stepExecution);
  }

  @Override
  public void onSkipInRead(Throwable t) {

    if (t instanceof FlatFileParseException fileParseException) {
      readErrors.add(BatchError.builder().errorMsg(t.getCause().getMessage())
          .lineNumber(fileParseException.getLineNumber())
          .originalValue(fileParseException.getInput()).build());
    } else {
      readErrors.add(BatchError.builder().errorMsg(t.getMessage()).build());
    }
    SkipListener.super.onSkipInRead(t);
  }
}
