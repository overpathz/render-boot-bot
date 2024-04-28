package com.example.rendertestbot.controller;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepo  extends JpaRepository<TestEntity, Long> {
}
