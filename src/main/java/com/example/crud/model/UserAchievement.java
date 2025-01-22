// src/main/java/com/example/demo/model/UserAchievement.java

package com.example.crud.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_achievements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAchievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long achievementId;
}
