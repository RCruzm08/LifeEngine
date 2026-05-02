package com.lifeengine.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeengine.model.*;
import com.lifeengine.websocket.SimulationWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

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
        world.populate(30);
        world.addFood(60);
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

    
    public Map<String, Object> getSnapshot() {
        List<Creature> all = world.getCreatures();
        List<Map<String, Object>> cList = new ArrayList<>();
        for (Creature c : all) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("x",     c.getX());
            m.put("y",     c.getY());
            m.put("type",  c.getType().name());
            m.put("energy",c.getEnergy());
            m.put("age",   c.getAge());
            m.put("speed", c.getSpeed());
            m.put("perc",  c.getPerception());
            m.put("agg",   c.getAggression());
            m.put("mut",   c.getMutationRate());
            m.put("mutCount", c.getMutationCount());
            cList.add(m);
        }
        Map<String, Object> snap = new LinkedHashMap<>();
        snap.put("cycle",      cycle);
        snap.put("generation", world.getGeneration());
        snap.put("totalBorn",  world.getTotalBorn());
        snap.put("totalDead",  world.getTotalDead());
        snap.put("creatures",  cList);
        return snap;
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
            long herbs  = all.stream().filter(c -> c.getType() == CreatureType.HERBIVORE).count();
            long preds  = all.stream().filter(c -> c.getType() == CreatureType.PREDATOR).count();
            long omnis  = all.stream().filter(c -> c.getType() == CreatureType.OMNIVORE).count();
            double avgEnergy = all.stream().mapToInt(Creature::getEnergy).average().orElse(0);
            double avgSpeed  = all.stream().mapToInt(Creature::getSpeed).average().orElse(0);
            double avgPerc   = all.stream().mapToInt(Creature::getPerception).average().orElse(0);
            double avgMut    = all.stream().mapToDouble(Creature::getMutationRate).average().orElse(0);

            
            List<Map<String, Object>> cList = new ArrayList<>();
            for (Creature c : all) {
                Map<String, Object> m = new HashMap<>();
                m.put("x", c.getX()); m.put("y", c.getY());
                m.put("t", c.getType().symbol);
                m.put("e", c.getEnergy());
                m.put("mut", c.justMutated);
                m.put("mc", c.getMutationCount());
                m.put("biome", world.getBiomeAt(c.getX(), c.getY()).name());
                cList.add(m);
            }

            
            Biome[][] bg = world.getBiomeGrid();
            List<String> biomeRows = new ArrayList<>();
            for (int y = 0; y < ROWS; y++) {
                StringBuilder sb = new StringBuilder();
                for (int x = 0; x < COLS; x++) {
                    sb.append(switch (bg[x][y]) {
                        case FOREST  -> "F";
                        case DESERT  -> "D";
                        case WATER   -> "W";
                        case PLAINS  -> "P";
                    });
                }
                biomeRows.add(sb.toString());
            }

            
            List<int[]> hotCells = new ArrayList<>();
            int[][] hm = world.getHeatmap();
            for (int x = 0; x < COLS; x++)
                for (int y = 0; y < ROWS; y++)
                    if (hm[x][y] > 0) hotCells.add(new int[]{x, y, hm[x][y]});

            List<int[]> foodList = world.getFoodSources();

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("cycle",      cycle);
            payload.put("gen",        world.getGeneration());
            payload.put("herbs",      herbs);
            payload.put("preds",      preds);
            payload.put("omnis",      omnis);
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
            payload.put("biomeMap",   biomeRows);
            payload.put("heatmap",    hotCells);
            payload.put("events",     events);

            wsHandler.broadcast(mapper.writeValueAsString(payload));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() { return running; }
    public int getCycle()      { return cycle; }
}
