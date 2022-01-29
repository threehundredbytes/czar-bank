package ru.dreadblade.czarbank.domain.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.dreadblade.czarbank.domain.BaseEntity;

import javax.persistence.Entity;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Permission extends BaseEntity {
    private String name;
}
