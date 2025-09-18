package com.example.cdp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Past
  private Timestamp orderDateTime;
  @NotNull
  @ManyToOne
  @JoinColumn(name = "securities_id")
  private Securities securities;
  private String market;
  @NotNull
  @Enumerated(EnumType.STRING)
  private Action action;
  @Positive
  private Integer quantity;
  @Positive
  private Double price;
  private Integer filledQuantity;
  @NotNull
  @Enumerated(EnumType.STRING)
  private TransactionStatus status;
  @Enumerated(EnumType.STRING)
  private Settlement settlement;

  private Double fees;

  @AllArgsConstructor
  @Getter
  public enum TransactionStatus {
    EXPIRED("EXPIRED"), FILLED("FILLED"), CANCELLED("CANCELLED");
    private final String value;

    public static TransactionStatus byText(String value) {
      for (TransactionStatus e : TransactionStatus.values()) {
        if (e.value.equalsIgnoreCase(value)) {
          return e;
        }
      }
      throw new IllegalArgumentException("No enum value is found!");
    }
  }

  @AllArgsConstructor
  @Getter
  public enum Action {
    SELL("SELL"), BUY("BUY");

    private final String value;

    public static Action byText(String value) {
      for (Action e : Action.values()) {
        if (e.value.equalsIgnoreCase(value)) {
          return e;
        }
      }
      throw new IllegalArgumentException("No enum value is found!");
    }
  }

  @AllArgsConstructor
  @Getter
  public enum Settlement {
    CPF("CPF"), CASH("CASH"), CASH_UPFRONT("CASH UPFRONT"), Other("NULL");

    private final String value;

    public static Settlement byText(String value) {
      for (Settlement e : Settlement.values()) {
        if (e.value.equalsIgnoreCase(value)) {
          return e;
        }
      }
      return Settlement.Other;
    }
  }

}
