package ru.dreadblade.czarbank.domain.security;

import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.dreadblade.czarbank.domain.BaseEntity;

import javax.persistence.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class RecoveryCode extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recovery_code_sequence")
    @SequenceGenerator(name = "recovery_code_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(length = 19, nullable = false, updatable = false)
    private String code;

    @Builder.Default
    private Boolean isUsed = false;
}
