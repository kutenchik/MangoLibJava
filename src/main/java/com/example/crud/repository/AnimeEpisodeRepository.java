package com.example.crud.repository;

import com.example.crud.model.AnimeEpisode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimeEpisodeRepository extends JpaRepository<AnimeEpisode,Long> {
}