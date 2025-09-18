package com.example.cdp.controller;

import com.example.cdp.model.Securities;
import com.example.cdp.model.Transaction;
import com.example.cdp.model.Transaction.Action;
import com.example.cdp.model.Transaction.Settlement;
import com.example.cdp.model.Transaction.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class TransactionControllerTest {

  static ObjectMapper mapper = new ObjectMapper();
  private static List<Securities> sampleSecurities;
  private static List<Transaction> sampleTransaction;
  @Autowired
  private MockMvc client;

  @BeforeAll
  public static void setupAll(@Autowired JdbcTemplate jdbcTemplate) {

    mapper.setSerializationInclusion(Include.NON_NULL);

    jdbcTemplate.update("INSERT INTO securities (stock_name, symbol) VALUES (?, ?);", ps -> {
      ps.setString(1, "test_stock");
      ps.setString(2, "test01");
    });

    //1. check insert is success
    //2. retrieve for usage in the future. to get its id
    Long securities_id = jdbcTemplate.queryForObject(
        "SELECT id FROM securities WHERE stock_name = 'test_stock';", Long.class);

    // initialized with more transaction record.
    jdbcTemplate.batchUpdate("""
        INSERT INTO transaction (order_date_time, securities_id, market, action, quantity, price, filled_quantity, status) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?);
        """, new BatchPreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps, int i) throws SQLException {

        ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now().minusDays(1).plusMinutes(i)));
        ps.setLong(2, securities_id);
        ps.setString(3, "SG");
        ps.setString(4, Action.values()[i % Action.values().length].getValue());
        ps.setInt(5, 100 + i);
        ps.setDouble(6, 10d);
        ps.setInt(7, 100 + i);
        ps.setString(8,
            TransactionStatus.values()[i % TransactionStatus.values().length].getValue());
      }

      @Override
      public int getBatchSize() {
        return Action.values().length * TransactionStatus.values().length;
      }
    });

    sampleSecurities = jdbcTemplate.query(
        "SELECT * FROM securities WHERE stock_name = 'test_stock';",
        new BeanPropertyRowMapper<>(com.example.cdp.model.Securities.class));

    sampleTransaction = jdbcTemplate.query("SELECT * FROM transaction t;",
        new BeanPropertyRowMapper<>(Transaction.class) {
          @Override
          public Transaction mapRow(ResultSet rs, int rowNumber) throws SQLException {

            Transaction transaction = super.mapRow(rs, rowNumber);

            transaction.setSecurities(
                sampleSecurities.stream().filter(securities -> {
                      try {
                        return securities.getId().equals(rs.getLong("securities_id"));
                      } catch (SQLException e) {
                        throw new RuntimeException(e);
                      }
                    })
                    .findFirst().orElseThrow(
                        AssertionFailedError::new));

            return transaction;
          }
        });

  }


  @Test
  @DisplayName("GIVEN id that matched with an existing Transaction WHEN Transaction API"
               + " readTransactionById is called THEN OK is returned with the Transaction entity")
  void readTransactionById() throws Exception {

    Long txnId = 1L;

    client.perform(MockMvcRequestBuilders.get("/transactions/id/{id}", txnId))
        .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(
            MockMvcResultMatchers.jsonPath("$.id").value(
                sampleTransaction.stream().filter(transaction -> transaction.getId().equals(txnId))
                    .findFirst().orElseThrow().getId())).andReturn();
  }

  @Test
  @DisplayName("GIVEN id that is not matched with any existing Transaction WHEN Transaction API"
               + " readTransactionById is called THEN NOT_FOUND is returned")
  void readTransactionById_NotFound() throws Exception {

    client.perform(MockMvcRequestBuilders.get("/transactions/id/{id}", 0))
        .andExpect(MockMvcResultMatchers.status().isNotFound()).andReturn();
  }

  @Test
  @DisplayName(
      "GIVEN new/existing Transaction entity WHEN securities API updateTransaction is called THEN"
      + "the Transaction entity is CREATED and added/updated into database")
  void updateTransaction() throws Exception {

    Transaction dummy = new Transaction(null, Timestamp.valueOf(LocalDateTime.now()),
        sampleSecurities.getFirst(), "SG", Action.BUY, 200, 10d, 200, TransactionStatus.FILLED,
        Settlement.CASH, null);

    // 1. Add new Transaction that not exist
    MvcResult result = client.perform(
            MockMvcRequestBuilders.post("/transactions").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dummy)))
        .andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();

    System.out.println(result.getResponse().getContentAsString());

    dummy = mapper.readValue(result.getResponse().getContentAsString(), Transaction.class);
    dummy.setQuantity(250); // modified the Transaction stockName
    dummy.setFilledQuantity(150); // modified the Transaction stockName

    // 2. Update field of an existing Transaction.
    client.perform(
            MockMvcRequestBuilders.post("/transactions").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dummy)))
        .andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(dummy.getId()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.quantity").value(dummy.getQuantity()))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.filledQuantity").value(dummy.getFilledQuantity()));

  }

  @Test
  @DisplayName(
      "GIVEN new/existing Transaction entity WHEN securities API updateTransaction is called THEN"
      + " the Transaction entity is NOT_FOUND nor added/updated into database")
  void updateTransaction_NotFoundId() throws Exception {

    Transaction dummy = new Transaction(1000L, Timestamp.valueOf(LocalDateTime.now()),
        sampleSecurities.getFirst(), "SG", Action.BUY, 200, 10d, 200, TransactionStatus.FILLED,
        Settlement.CASH, null);

    // 1. Add new Transaction that not exist (with specified ID)
    client.perform(
            MockMvcRequestBuilders.post("/transactions").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dummy)))
        .andExpect(MockMvcResultMatchers.status().isNotFound()).andReturn();
  }


  @Test
  @DisplayName(
      "GIVEN Securities stockName which related/not related to some Transaction WHEN Transaction API readTransactionBySecuritiesStockName"
      + " is called THEN OK is returned with list of Transaction which related with securities (by its stockName)")
  void readTransactionBySecuritiesStockName() throws Exception {

    // 1. With some returning transaction record with given stockName
    client.perform(MockMvcRequestBuilders.get("/transactions/stock-name/{stockName}",
            sampleSecurities.getFirst().getStockName()).contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(
            MockMvcResultMatchers.jsonPath("$[*].securities.stockName",
                Matchers.everyItem(Matchers.is(sampleSecurities.getFirst().getStockName()))))
        .andReturn();

    // 2. With None returning transaction record with given stockName
    client.perform(
            MockMvcRequestBuilders.get("/transactions/stock-name/{stockName}", "Not Exist Stock Name"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.empty())).andReturn();
  }


  @ParameterizedTest
  @DisplayName("""
      GIVEN Action WHEN Transaction API readTransactionByAction is called THEN 
      OK is returned with list of Transaction which related with Action (e.g. Buy)
      """)
  @EnumSource(Action.class)
  void readTransactionByAction(Action action) throws Exception {

    // 1. With some returning transaction record with given stockName
    client.perform(
            MockMvcRequestBuilders.get("/transactions/action/{action}", action)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(
            MockMvcResultMatchers.jsonPath("$[*].action",
                Matchers.everyItem(Matchers.is(action.getValue())))).andReturn();
  }

  @Test
  @DisplayName("""
      GIVEN invalid Action WHEN Transaction API readTransactionByAction is called THEN 
      BadRequest is returned.
      """)
  void readTransactionByAction_Invalid() throws Exception {

    // 1. With some invalid value
    client.perform(MockMvcRequestBuilders.get("/transactions/action/{action}", "invalidAction"))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
  }

  @Test
  @DisplayName("""
      GIVEN Stock-Name, Action and TransactionStatus WHEN Transaction API readTransactionByStockNameAndTransactionStatus is called THEN 
      OK is returned with list of Transaction record. 
      """)
  void readTransactionByStockNameAndTransactionStatus() throws Exception {

    client.perform(MockMvcRequestBuilders.get("/transactions/query")
            .queryParam("stock-name", sampleSecurities.getFirst().getStockName())
            .queryParam("status", TransactionStatus.FILLED.getValue())
            .queryParam("action", Action.BUY.getValue()))
        .andExpect(MockMvcResultMatchers.status().isOk()) //
        .andExpect(MockMvcResultMatchers.jsonPath("$[*].securities.stockName",
            Matchers.everyItem(Matchers.is(sampleSecurities.getFirst().getStockName())))) //
        .andExpect(MockMvcResultMatchers.jsonPath("$[*].status",
            Matchers.everyItem(
                Matchers.equalToIgnoringCase(TransactionStatus.FILLED.getValue())))) //
        .andExpect(MockMvcResultMatchers.jsonPath("$[*].action",
            Matchers.everyItem(Matchers.equalToIgnoringCase(Action.BUY.getValue())))).andReturn();

    // Query with no optional request param

    // 1. Without any request param, expecting all transactions to be returned.
    Object[] array = sampleTransaction.stream().map(value -> value.getId().intValue()).toArray();
    client.perform(MockMvcRequestBuilders.get("/transactions/query"))
        .andExpect(MockMvcResultMatchers.status().isOk()) //
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(sampleTransaction.size())))
        .andExpect(MockMvcResultMatchers.jsonPath("$[*].id", Matchers.containsInAnyOrder(array)))
        .andReturn();

    for (Action a : Action.values()) {
      for (TransactionStatus t : TransactionStatus.values()) {
        client.perform(MockMvcRequestBuilders.get("/transactions/query")
                .queryParam("status", t.getValue())
                .queryParam("action", a.getValue()))
            .andExpect(MockMvcResultMatchers.status().isOk()) //
            .andExpect(MockMvcResultMatchers.jsonPath("$[*].status",
                Matchers.everyItem(Matchers.is(t.getValue())))) //
            .andExpect(MockMvcResultMatchers.jsonPath("$[*].action",
                Matchers.everyItem(Matchers.is(a.getValue())))).andReturn();
      }
    }

    for (Action a : Action.values()) {
      client.perform(MockMvcRequestBuilders.get("/transactions/query")
              .queryParam("stock-name", sampleSecurities.getFirst().getStockName())
              .queryParam("action", a.getValue()))
          .andExpect(MockMvcResultMatchers.status().isOk()) //
          .andExpect(MockMvcResultMatchers.jsonPath("$[*].securities.stockName",
              Matchers.everyItem(Matchers.is(sampleSecurities.getFirst().getStockName())))) //
          .andExpect(MockMvcResultMatchers.jsonPath("$[*].action",
              Matchers.everyItem(Matchers.is(a.getValue())))).andReturn();
    }

    for (TransactionStatus t : TransactionStatus.values()) {
      client.perform(MockMvcRequestBuilders.get("/transactions/query")
              .queryParam("stock-name", sampleSecurities.getFirst().getStockName())
              .queryParam("status", t.getValue()))
          .andExpect(MockMvcResultMatchers.status().isOk()) //
          .andExpect(MockMvcResultMatchers.jsonPath("$[*].securities.stockName",
              Matchers.everyItem(Matchers.is(sampleSecurities.getFirst().getStockName())))) //
          .andExpect(MockMvcResultMatchers.jsonPath("$[*].status",
              Matchers.everyItem(Matchers.is(t.getValue())))) //
          .andReturn();
    }


  }
}