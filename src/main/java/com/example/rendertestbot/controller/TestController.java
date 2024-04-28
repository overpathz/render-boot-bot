package com.example.rendertestbot.controller;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/testing")
public class TestController {

    private final TestRepo testRepo;

    @GetMapping("/create")
    public void createOne() {
        TestEntity testEntity = new TestEntity();
        testEntity.setName("Name " + ThreadLocalRandom.current().nextInt(1000));
        testRepo.save(testEntity);
    }

    @GetMapping("/get")
    public List<TestEntity> getAll() {
        return testRepo.findAll();
    }

    @PostConstruct
    public void init() {
        log.info("Executing postconstruct");
        log.info("Getting all entities: {}", testRepo.findAll());
        log.info("Creating entity..");
        TestEntity testEntity = new TestEntity();
        testEntity.setName("Name " + ThreadLocalRandom.current().nextInt(1000));
        testRepo.save(testEntity);
        log.info("Getting all entities: {}", testRepo.findAll());
    }
}
