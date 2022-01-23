package ru.dreadblade.czarbank.domain.security;

import lombok.*;
import ru.dreadblade.czarbank.domain.BaseEntity;

import javax.persistence.Entity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Permission extends BaseEntity {
    private String name;
}
