package com.example.rendertestbot.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepo  extends JpaRepository<TestEntity, Long> {
}
