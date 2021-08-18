package ru.dreadblade.czarbank.domain.security;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.MERGE })
    @JoinTable(name = "role_permission",
            joinColumns = { @JoinColumn(name = "ROLE_ID", referencedColumnName = "ID") },
            inverseJoinColumns = { @JoinColumn(name = "PERMISSION_ID", referencedColumnName = "ID") })
    Set<Permission> permissions;
}
