package com.example.cdp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class BatchError {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  Timestamp createdAt;

  //  @Column(columnDefinition = "json")
  String originalValue;

  Integer lineNumber;

  String errorMsg;

  String source;

  Long jobId;

}
