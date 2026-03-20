package com.lifeengine.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeengine.model.Creature;
import com.lifeengine.model.CreatureType;
import com.lifeengine.model.World;
import com.lifeengine.websocket.SimulationWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class SimulationEngine {

    private static final int COLS = 80;
    private static final int ROWS = 44;

    private World world;
    private int cycle = 0;
    private boolean running = false;
    private int tickDelay = 300;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> tickFuture;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private SimulationWebSocketHandler wsHandler;

    public SimulationEngine() {
        reset();
    }

    public void reset() {
        cycle = 0;
        world = new World(COLS, ROWS);
        world.populate(28); // 28 criaturas iniciais
        world.addFood(45);  // 45 fontes de comida
    }

    public void start() {
        if (running) return;
        running = true;
        scheduleTick();
    }

    public void pause() {
        running = false;
        if (tickFuture != null) tickFuture.cancel(false);
    }

    public void restart() {
        pause();
        reset();
        cycle = 0;
        start();
    }

    public void setTickDelay(int ms) {
        this.tickDelay = ms;
        if (running) {
            if (tickFuture != null) tickFuture.cancel(false);
            scheduleTick();
        }
    }

    public void setFoodRate(int r)   { world.setFoodRate(r); }
    public void setReproRate(int r)  { world.setReproRate(r); }
    public void setMutBoost(double b){ world.setMutBoost(b); }

    public void triggerCrisis() {
        world.triggerCrisis();
        broadcastState(List.of("event:⚠ Crise! Todos os predadores foram eliminados."));
    }

    private void scheduleTick() {
        tickFuture = scheduler.scheduleAtFixedRate(this::tick, 0, tickDelay, TimeUnit.MILLISECONDS);
    }

    private void tick() {
        if (!running) return;
        try {
            cycle++;
            List<String> events = world.tick();

            if (world.getCreatures().isEmpty()) {
                events.add("event:☠ Extinção total no ciclo " + cycle + "!");
                pause();
            }

            broadcastState(events);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcastState(List<String> events) {
        try {
            List<Creature> all = world.getCreatures();
            long herbs = all.stream().filter(c -> c.getType() == CreatureType.HERBIVORE).count();
            long preds = all.stream().filter(c -> c.getType() == CreatureType.PREDATOR).count();
            double avgEnergy = all.stream().mapToInt(Creature::getEnergy).average().orElse(0);
            double avgSpeed  = all.stream().mapToInt(Creature::getSpeed).average().orElse(0);
            double avgPerc   = all.stream().mapToInt(Creature::getPerception).average().orElse(0);
            double avgMut    = all.stream().mapToDouble(Creature::getMutationRate).average().orElse(0);

            List<Map<String,Object>> cList = new ArrayList<>();
            for (Creature c : all) {
                Map<String,Object> m = new HashMap<>();
                m.put("x", c.getX()); m.put("y", c.getY());
                m.put("t", c.getType().symbol);
                m.put("e", c.getEnergy());
                m.put("mut", c.justMutated);
                cList.add(m);
            }

            List<int[]> foodList = world.getFoodSources();

            Map<String,Object> payload = new LinkedHashMap<>();
            payload.put("cycle",      cycle);
            payload.put("gen",        world.getGeneration());
            payload.put("herbs",      herbs);
            payload.put("preds",      preds);
            payload.put("food",       foodList.size());
            payload.put("born",       world.getTotalBorn());
            payload.put("dead",       world.getTotalDead());
            payload.put("avgEnergy",  Math.round(avgEnergy * 10.0) / 10.0);
            payload.put("avgSpeed",   Math.round(avgSpeed  * 100.0) / 100.0);
            payload.put("avgPerc",    Math.round(avgPerc   * 100.0) / 100.0);
            payload.put("avgMut",     Math.round(avgMut    * 1000.0) / 1000.0);
            payload.put("running",    running);
            payload.put("creatures",  cList);
            payload.put("foodCoords", foodList);
            payload.put("events",     events);

            wsHandler.broadcast(mapper.writeValueAsString(payload));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() { return running; }
    public int getCycle()      { return cycle; }
}
