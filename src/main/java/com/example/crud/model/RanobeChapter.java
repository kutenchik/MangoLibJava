// src/main/java/com/example/demo/model/RanobeChapter.java

package com.example.crud.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ranobe_glavi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RanobeChapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long titleId;
    private Integer nomerGlavi;
    private String glava;

    @Column(columnDefinition = "TEXT[]")
    private String[] contentGlavi;

    private LocalDateTime lastUpdate;
}
