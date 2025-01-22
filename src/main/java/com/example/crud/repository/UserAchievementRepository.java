package com.example.crud.repository;

import com.example.crud.model.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAchievementRepository extends JpaRepository<UserAchievement,Long> {
}