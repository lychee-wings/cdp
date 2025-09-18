package com.example.cdp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import java.sql.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceDaily {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Past
  private Date date;
  @Positive
  private Double open;
  @Positive
  private Double high;
  @Positive
  private Double low;
  @Positive
  private Double close;
  @Positive
  private Double adjClose;
  @Positive
  private Long volume;
  @NotNull
  @ManyToOne
  @JoinColumn(name = "securities_id")
  private Securities securities;

}
