package ru.dreadblade.czarbank.domain;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Currency extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "currency_id_sequence")
    private Long id;

    @Column(length = 3, nullable = false, unique = true, updatable = false)
    private String code;

    @Column(length = 4, nullable = false, unique = true, updatable = false)
    private String symbol;
}
