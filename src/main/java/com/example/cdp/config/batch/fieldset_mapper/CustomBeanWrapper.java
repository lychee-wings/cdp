package com.example.cdp.config.batch.fieldset_mapper;

import com.example.cdp.model.PriceDaily;
import com.example.cdp.model.Securities;
import com.example.cdp.model.Transaction;
import com.example.cdp.model.Transaction.Action;
import com.example.cdp.model.Transaction.Settlement;
import com.example.cdp.model.Transaction.TransactionStatus;
import jakarta.annotation.PostConstruct;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.BindException;

@Configuration
public class CustomBeanWrapper {

  Predicate<String> nullPredicate = s -> false;

  @Value("${application.literal-null}")
  List<String> nullLiteral;

  @Value("${application.strict-parser}")
  boolean strictParser;

  @PostConstruct
  public void postConstruct() {

    if (strictParser) {
      return; // skip the predicate to check a series of "null" words.
    }

    for (String word : nullLiteral) {
      Predicate<String> isEqual = s -> s.equalsIgnoreCase(word);
      nullPredicate = nullPredicate.or(isEqual);
    }
  }

  @Bean
  @StepScope
  public BeanWrapperFieldSetMapper<PriceDaily> priceDailyBeanWrapper(
      @Value("#{jobExecutionContext[securities]}") Securities securities) {
    return new BeanWrapperFieldSetMapper<>() {
      {
        setTargetType(PriceDaily.class);
      }

      @Override
      public PriceDaily mapFieldSet(FieldSet fs) throws BindException {

        return PriceDaily.builder() //
            .date(nullPredicate.test(fs.readRawString("date")) ? null
                : Date.valueOf(dateTimeParse(fs.readRawString("date"), LocalDate::from))) //
            .open(nullPredicate.test(fs.readRawString("open")) ? null : fs.readDouble("open")) //
            .high(nullPredicate.test(fs.readRawString("high")) ? null : fs.readDouble("high")) //
            .low(nullPredicate.test(fs.readRawString("low")) ? null : fs.readDouble("low")) //
            .close(nullPredicate.test(fs.readRawString("close")) ? null : fs.readDouble("close")) //
            .adjClose(nullPredicate.test(fs.readRawString("adjClose")) ? null
                : fs.readDouble("adjClose")) //
            .volume(
                nullPredicate.test(fs.readRawString("volume")) ? null : fs.readLong("volume")) //
            .securities(securities).build();
      }
    };
  }

  @Bean
  @StepScope
  public BeanWrapperFieldSetMapper<Transaction> transactionBeanWrapper(
      @Value("#{jobExecutionContext[securities]}") Securities securities) {
    return new BeanWrapperFieldSetMapper<>() {
      {
        setTargetType(Transaction.class);
      }

      @Override
      public Transaction mapFieldSet(FieldSet fs) throws BindException {

        return Transaction.builder() //
            .orderDateTime(nullPredicate.test(fs.readRawString("Order Date/Time")) ? null
                : Timestamp.valueOf(
                    dateTimeParse(fs.readRawString("Order Date/Time"), LocalDateTime::from))) //
            .securities(Securities.builder().symbol(fs.readRawString("Symbol")).build()) //
            .market(
                nullPredicate.test(fs.readRawString("Market")) ? null : fs.readString("Market")) //
            .action(nullPredicate.test(fs.readRawString("Action")) ? null
                : Action.byText(fs.readString("Action"))) //
            .quantity(
                nullPredicate.test(fs.readRawString("Quantity")) ? null : fs.readInt("Quantity")) //
            .price(nullPredicate.test(fs.readRawString("Filled Price")) ? null
                : fs.readDouble("Filled Price")) //
            .filledQuantity(nullPredicate.test(fs.readRawString("Filled Quantity")) ? null
                : fs.readInt("Filled Quantity")) //
            .status(nullPredicate.test(fs.readRawString("Status")) ? null
                : TransactionStatus.byText(fs.readString("Status"))) //
            .settlement(nullPredicate.test(fs.readRawString("Settlement Mode")) ? null
                : Settlement.byText(fs.readString("Settlement Mode")))
//            .fees(nullPredicate.test(fs.readRawString("Fees")) ? null : fs.readDouble("Fees"))
            .build();

      }
    };
  }

  /**
   * The function help to parse most of the format to DateTime object, e.g. ISOFormat, RFC1123
   * format and etc.
   *
   * @param dateTimeString
   * @param query
   * @param <R>            temporal – the temporal object to query, not null
   * @return The return type is depending on the TemporalQuery, * LocalDate::from it returns
   * LocalDate; * LocalDateTime::from it returns LocalDateTime;
   */
  private <R> R dateTimeParse(String dateTimeString, TemporalQuery<R> query) {

    TemporalAccessor temporalAccessor;
    DateTimeFormatter readParser = new DateTimeFormatterBuilder()
        .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME) //
        .appendOptional(DateTimeFormatter.ISO_ZONED_DATE_TIME) //
        .appendOptional(DateTimeFormatter.RFC_1123_DATE_TIME) //
        .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE) //
        .appendOptional(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        .optionalStart().appendLiteral(" ").optionalEnd()
        .optionalStart().appendLiteral(", ").optionalEnd()
        .appendOptional(DateTimeFormatter.ISO_TIME.localizedBy(Locale.ENGLISH))
        .toFormatter();
    temporalAccessor = readParser.parse(dateTimeString);

    return temporalAccessor.query(query);
  }
}
