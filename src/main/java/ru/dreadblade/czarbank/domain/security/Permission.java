package ru.dreadblade.czarbank.domain.security;

import lombok.*;
import ru.dreadblade.czarbank.domain.BaseEntity;

import javax.persistence.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Permission extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "permission_id_sequence")
    @SequenceGenerator(name = "permission_id_sequence", allocationSize = 1)
    private Long id;

    @Column(length = 100, nullable = false, unique = true, updatable = false)
    private String name;
}
