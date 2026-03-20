package com.lifeengine.model;

import java.util.*;

public class World {
    public final int width;
    public final int height;
    private final List<Creature> creatures = new ArrayList<>();
    private final List<int[]> foodSources = new ArrayList<>();
    private final Random random = new Random();
    private int generation = 1;
    private int totalBorn = 0;
    private int totalDead = 0;
    private int foodRate = 20;
    private int reproRate = 15;
    private double mutBoost = 1.0;

    public World(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void populate(int count) {
        for (int i = 0; i < count; i++) {
            CreatureType type = random.nextBoolean() ? CreatureType.HERBIVORE : CreatureType.PREDATOR;
            creatures.add(new Creature(random.nextInt(width), random.nextInt(height), type, generation));
            totalBorn++;
        }
    }

    public void addFood(int count) {
        for (int i = 0; i < count; i++)
            foodSources.add(new int[]{random.nextInt(width), random.nextInt(height)});
    }

    public List<String> tick() {
        List<String> events = new ArrayList<>();

        if (random.nextInt(100) < foodRate)
            foodSources.add(new int[]{random.nextInt(width), random.nextInt(height)});

        List<Creature> newborns = new ArrayList<>();
        for (Creature c : creatures) {
            c.tick(this);
            if (c.isAlive() && c.getEnergy() > 80 && random.nextInt(100) < reproRate) {
                int nx = clampX(c.getX() + random.nextInt(3) - 1);
                int ny = clampY(c.getY() + random.nextInt(3) - 1);
                Creature child = c.reproduce(nx, ny, generation + 1);
                newborns.add(child);
                c.consumeEnergy(30);
                totalBorn++;
                if (child.justMutated)
                    events.add("mutate:" + (c.getType() == CreatureType.HERBIVORE ? "Herbívoro" : "Predador")
                            + " gen-" + child.getGeneration()
                            + " spd=" + child.getSpeed()
                            + " perc=" + child.getPerception()
                            + " agg=" + child.getAggression());
            }
        }
        creatures.addAll(newborns);

        int before = creatures.size();
        creatures.removeIf(c -> !c.isAlive());
        int died = before - creatures.size();
        totalDead += died;
        if (died > 2) events.add("death:" + died + " criaturas morreram.");

        if (totalBorn > 0 && totalBorn % 5 == 0) {
            generation++;
            events.add("event:Nova geração: " + generation);
        }

        return events;
    }

    public boolean eatFoodAt(int x, int y) {
        Iterator<int[]> it = foodSources.iterator();
        while (it.hasNext()) {
            int[] f = it.next();
            if (f[0] == x && f[1] == y) { it.remove(); return true; }
        }
        return false;
    }

    public int[] nearestFood(int x, int y) {
        int[] best = null;
        int bestDist = Integer.MAX_VALUE;
        for (int[] f : foodSources) {
            int d = Math.abs(f[0] - x) + Math.abs(f[1] - y);
            if (d < bestDist) { bestDist = d; best = f; }
        }
        return best;
    }

    public Creature nearestPrey(int x, int y) {
        Creature best = null;
        int bestDist = Integer.MAX_VALUE;
        for (Creature c : creatures) {
            if (c.getType() == CreatureType.HERBIVORE && c.isAlive()) {
                int d = Math.abs(c.getX() - x) + Math.abs(c.getY() - y);
                if (d < bestDist) { bestDist = d; best = c; }
            }
        }
        return best;
    }

    public void triggerCrisis() {
        creatures.stream().filter(c -> c.getType() == CreatureType.PREDATOR)
                .forEach(c -> { try { var f = c.getClass().getDeclaredField("alive"); f.setAccessible(true); f.set(c, false); } catch (Exception ignored) {} });
        creatures.removeIf(c -> !c.isAlive());
    }

    public int clampX(int x) { return Math.max(0, Math.min(width - 1, x)); }
    public int clampY(int y) { return Math.max(0, Math.min(height - 1, y)); }

    public List<Creature> getCreatures()  { return creatures; }
    public List<int[]> getFoodSources()   { return foodSources; }
    public int getGeneration()            { return generation; }
    public int getTotalBorn()             { return totalBorn; }
    public int getTotalDead()             { return totalDead; }
    public void setFoodRate(int r)        { this.foodRate = r; }
    public void setReproRate(int r)       { this.reproRate = r; }
    public void setMutBoost(double b)     { this.mutBoost = b; }
}
