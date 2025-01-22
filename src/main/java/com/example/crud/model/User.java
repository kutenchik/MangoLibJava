// src/main/java/com/example/demo/model/User.java

package com.example.crud.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String username;

    @Column(nullable=false)
    private String password;

    @Column(nullable=false, unique=true)
    private String email;

    private String profilePic;
    @Column(columnDefinition = "TEXT DEFAULT 'Пусто'")
    private String description;
    @Builder.Default
    private String status = "Пользователь";

    @Builder.Default
    private Integer exp = 0;
}
