// src/main/java/com/example/demo/model/AnimeEpisode.java

package com.example.crud.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "anime_serii")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnimeEpisode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long titleId;
    private Integer nomerGlavi; // фактически номер серии
    private String glava; // название серии

    @Column(columnDefinition = "TEXT")
    private String htmlCode;

    private LocalDateTime lastUpdate;
}
