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
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_id_sequence")
    @SequenceGenerator(name = "role_id_sequence", allocationSize = 1)
    private Long id;

    @Column(length = 100, nullable = false, unique = true)
    private String name;

    @Singular(value = "addPermission")
    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.MERGE })
    @JoinTable(name = "role_permission",
            joinColumns = { @JoinColumn(name = "ROLE_ID", referencedColumnName = "ID") },
            inverseJoinColumns = { @JoinColumn(name = "PERMISSION_ID", referencedColumnName = "ID") })
    private Set<Permission> permissions = Collections.emptySet();
}