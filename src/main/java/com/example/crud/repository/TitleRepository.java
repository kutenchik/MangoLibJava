package com.example.crud.repository;

import com.example.crud.model.Title;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TitleRepository extends JpaRepository<Title,Long> {
    Optional<Title> findByNameOnEnglish(String nameOnEnglish);
}