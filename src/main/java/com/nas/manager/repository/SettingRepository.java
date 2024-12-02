package com.nas.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nas.manager.model.Setting;

public interface SettingRepository extends JpaRepository<Setting, Long> {
} 