package com.lifeengine;

import com.lifeengine.engine.SimulationEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class LifeEngineApplication {

    @Autowired
    private SimulationEngine engine;

    public static void main(String[] args) {
        SpringApplication.run(LifeEngineApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        engine.start();
    }
}
