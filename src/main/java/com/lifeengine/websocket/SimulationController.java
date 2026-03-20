package com.lifeengine.websocket;

import com.lifeengine.engine.SimulationEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sim")
@CrossOrigin
public class SimulationController {

    @Autowired
    private SimulationEngine engine;

    @PostMapping("/start")
    public Map<String,String> start() {
        engine.start();
        return Map.of("status", "started");
    }

    @PostMapping("/pause")
    public Map<String,String> pause() {
        engine.pause();
        return Map.of("status", "paused");
    }

    @PostMapping("/reset")
    public Map<String,String> reset() {
        engine.restart();
        return Map.of("status", "reset");
    }

    @PostMapping("/crisis")
    public Map<String,String> crisis() {
        engine.triggerCrisis();
        return Map.of("status", "crisis");
    }

    @PostMapping("/speed")
    public Map<String,String> speed(@RequestBody Map<String,Integer> body) {
        engine.setTickDelay(body.get("ms"));
        return Map.of("status", "ok");
    }

    @PostMapping("/params")
    public Map<String,String> params(@RequestBody Map<String,Object> body) {
        if (body.containsKey("foodRate"))  engine.setFoodRate((Integer) body.get("foodRate"));
        if (body.containsKey("reproRate")) engine.setReproRate((Integer) body.get("reproRate"));
        if (body.containsKey("mutBoost"))  engine.setMutBoost(((Number) body.get("mutBoost")).doubleValue());
        return Map.of("status", "ok");
    }
}
