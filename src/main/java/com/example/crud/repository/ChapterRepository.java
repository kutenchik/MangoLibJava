package com.example.crud.repository;

import com.example.crud.model.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChapterRepository extends JpaRepository<Chapter,Long> {
    // при необходимости свои методы
}