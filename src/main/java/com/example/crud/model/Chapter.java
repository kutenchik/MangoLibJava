// src/main/java/com/example/demo/model/Chapter.java

package com.example.crud.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "glava")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long titleId;
    private Integer nomerGlavi;
    private String glava;

    @Column(columnDefinition = "TEXT")
    private String images; // храним через разделитель, например "img1;img2;..."

    @Column(name="last_update")
    private LocalDateTime lastUpdate;
}
