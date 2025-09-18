package com.example.cdp.controller;

import com.example.cdp.model.Securities;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
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
class SecuritiesControllerTest {

  static ObjectMapper mapper = new ObjectMapper();
  private static Securities sampleSecurities;
  @Autowired
  private MockMvc client;

  @BeforeAll
  public static void setupAll(@Autowired JdbcTemplate jdbcTemplate) {
    mapper.setSerializationInclusion(Include.NON_NULL);
    jdbcTemplate.batchUpdate(
        "INSERT INTO securities (stock_name, symbol) VALUES ('test_stock', 'test01');");

    sampleSecurities = jdbcTemplate.queryForObject("SELECT * FROM securities WHERE id = ?;",
        new BeanPropertyRowMapper<>(Securities.class), 1);
  }

  @Test
  @DisplayName(
      "GIVEN id that matched with an existing Securities WHEN Securities API"
      + " readSecuritiesById is called THEN OK is returned with the Securities entity")
  void readSecuritiesById() throws Exception {

    client.perform(MockMvcRequestBuilders.get("/securities/id/{id}", sampleSecurities.getId()))
        .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(
            MockMvcResultMatchers.content().json(mapper.writeValueAsString(sampleSecurities)));
  }

  @Test
  @DisplayName(
      "GIVEN id that is not matched with any existing Securities WHEN Securities API"
      + " readSecuritiesById is called THEN NOT_FOUND is returned")
  void readSecuritiesById_NotFound() throws Exception {
    client.perform(MockMvcRequestBuilders.get("/securities/id/{id}", 0))
        .andExpect(MockMvcResultMatchers.status().isNotFound());
  }


  @Test
  @DisplayName("GIVEN stockName that matched with some existing Securities WHEN Securities API"
               + " readSecuritiesByStockName is called THEN OK is returned with list of some Securities entities")
  void readSecuritiesByStockName() throws Exception {
    client.perform(MockMvcRequestBuilders.get("/securities/stock-name/{stockName}",
            sampleSecurities.getStockName())).andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content()
            .json(mapper.writeValueAsString(List.of(sampleSecurities))));
  }

  @Test
  @DisplayName(
      "GIVEN stockName that is not matched with any existing Securities WHEN Securities API"
      + " readSecuritiesByStockName is called THEN OK is returned with empty list of Securities")
  void readSecuritiesByStockName_NoMatch() throws Exception {
    client.perform(MockMvcRequestBuilders.get("/securities/stock-name/{stockName}", "NoExist"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(List.of())));
  }

  @Test
  @DisplayName("WHEN Securities API readAllSecurities is called THEN"
               + " OK is returned with list of all Securities entities")
  void readAllSecurities() throws Exception {

    MvcResult mvcResult = client.perform(MockMvcRequestBuilders.get("/securities"))
        .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content()
            .json(mapper.writeValueAsString(List.of(sampleSecurities)))).andReturn();

  }

  @Test
  @DisplayName(
      "GIVEN symbol that matched with some existing Securities WHEN Securities API readSecuritiesBySymbol is called THEN"
      + " OK is returned with list of some Securities entities")
  void readSecuritiesBySymbol() throws Exception {
    client.perform(
            MockMvcRequestBuilders.get("/securities/symbol/{symbol}", sampleSecurities.getSymbol()))
        .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(
            MockMvcResultMatchers.content().json(mapper.writeValueAsString(List.of(sampleSecurities))));
  }

  @Test
  @DisplayName(
      "GIVEN symbol that is not matched with any existing Securities WHEN Securities API readSecuritiesBySymbol is called THEN"
      + " OK is returned with empty list of Securities")
  void readSecuritiesBySymbol_NoMatch() throws Exception {
    client.perform(MockMvcRequestBuilders.get("/securities/symbol/NoExist"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(List.of())));
  }


  @Test
  @DisplayName(
      "GIVEN new/existing Securities entity WHEN Securities API updateSecurities is called THEN"
      + "the Securities entity is CREATED and added/updated into database")
  void updateSecurities() throws Exception {
    Securities dummy = new Securities(null, "dummy", "dummy");

    // 1. Add new Securities that not exist
    MvcResult result = client.perform(
            MockMvcRequestBuilders.post("/securities").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dummy)))
        .andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();

    dummy = mapper.readValue(result.getResponse().getContentAsString(), Securities.class);
    dummy.setStockName(dummy.getStockName() + dummy.getId()); // modified the Securities stockName

    // 2. Update field of an existing Securities.
    client.perform(
            MockMvcRequestBuilders.post("/securities").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dummy)))
        .andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(dummy.getId()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.stockName").value(dummy.getStockName()));
  }

  @Test
  @DisplayName(
      "GIVEN non existence Securities entity WHEN Securities API updateSecurities is called THEN"
      + " the Securities entity is NOT_FOUND nor added/updated into database")
  void updateSecurities_NotFoundId() throws Exception {
    Securities dummy = new Securities(1000L, "dummy", "dummy");

    // 1. Add new Securities that not exist (with specified ID)
    MvcResult result = client.perform(
            MockMvcRequestBuilders.post("/securities").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dummy)))
        .andExpect(MockMvcResultMatchers.status().isNotFound()).andReturn();

  }
}