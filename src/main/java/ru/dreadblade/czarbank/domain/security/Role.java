package ru.dreadblade.czarbank.domain.security;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.dreadblade.czarbank.domain.BaseEntity;

import javax.persistence.*;
import java.util.Collections;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Role extends BaseEntity {
    private String name;

    @Singular(value = "addPermission")
    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.MERGE })
    @JoinTable(name = "role_permission",
            joinColumns = { @JoinColumn(name = "ROLE_ID", referencedColumnName = "ID") },
            inverseJoinColumns = { @JoinColumn(name = "PERMISSION_ID", referencedColumnName = "ID") })
    private Set<Permission> permissions = Collections.emptySet();
}
