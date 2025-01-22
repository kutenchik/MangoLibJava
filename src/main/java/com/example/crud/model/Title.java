package com.example.crud.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "titles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Title {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long titleId;

    private String nameOnRussian;
    private String nameOnEnglish;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer year;
    private String cover;
    private String genres;

    @Builder.Default
    private Integer amountOfViews = 0;

    private String type;

    // Эти поля не хранятся в таблице titles, а будут вычисляться в сервисе
    @Transient
    private Integer glavaCount;

    @Transient
    private Double rating;
}
