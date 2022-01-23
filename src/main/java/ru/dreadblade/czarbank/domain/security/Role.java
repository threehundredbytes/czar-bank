package ru.dreadblade.czarbank.domain.security;

import lombok.*;
import ru.dreadblade.czarbank.domain.BaseEntity;

import javax.persistence.*;
import java.util.Collections;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Role extends BaseEntity {
    private String name;

    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.MERGE })
    @JoinTable(name = "role_permission",
            joinColumns = { @JoinColumn(name = "ROLE_ID", referencedColumnName = "ID") },
            inverseJoinColumns = { @JoinColumn(name = "PERMISSION_ID", referencedColumnName = "ID") })
    Set<Permission> permissions = Collections.emptySet();
}
