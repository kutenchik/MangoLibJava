// src/main/java/com/example/demo/model/Achievement.java

package com.example.crud.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "achievements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long achievementId;

    private String achievemntName;
    private String achievementDesc;
    private String achievementImage;
}
