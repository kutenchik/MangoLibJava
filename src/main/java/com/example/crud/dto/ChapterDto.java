package com.example.crud.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChapterDto {
    private Integer nomerGlavi; // номер главы/серии
    private String glavaName;   // название главы/серии
}