package com.lifeengine.model;

import java.util.Random;

public class Creature {
    private int x, y;
    private final CreatureType type;
    private int energy;
    private boolean alive = true;
    private int age = 0;
    private final int generation;
    private final int maxAge;
    private static int counter = 0;
    private final Random random = new Random();

    private final int speed;
    private final int perception;
    private final double mutationRate;
    private final int aggression;
    public boolean justMutated = false;

    public Creature(int x, int y, CreatureType type, int generation) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.generation = generation;
        this.energy = 50 + random.nextInt(20);
        this.maxAge = 30 + random.nextInt(20);
        this.speed = 1 + random.nextInt(3);
        this.perception = 2 + random.nextInt(5);
        this.mutationRate = 0.1 + random.nextDouble() * 0.2;
        this.aggression = type == CreatureType.PREDATOR ? 3 + random.nextInt(5) : 1 + random.nextInt(3);
        counter++;
    }

    private Creature(int x, int y, CreatureType type, int generation,
                     int speed, int perception, double mutationRate, int aggression) {
        Random rng = new Random();
        this.x = x;
        this.y = y;
        this.type = type;
        this.generation = generation;
        this.energy = 50 + rng.nextInt(20);
        this.maxAge = 30 + rng.nextInt(20);
        this.speed = speed;
        this.perception = perception;
        this.mutationRate = mutationRate;
        this.aggression = aggression;
        counter++;
    }

    public Creature reproduce(int nx, int ny, int nextGeneration) {
        int newSpeed      = mutate(speed, 1, 5);
        int newPerception = mutate(perception, 1, 8);
        int newAggression = mutate(aggression, 1, 10);
        double newMutRate = Math.min(0.6, Math.max(0.05,
                mutationRate + (random.nextDouble() - 0.5) * 0.05));
        Creature child = new Creature(nx, ny, type, nextGeneration,
                newSpeed, newPerception, newMutRate, newAggression);
        child.justMutated = newSpeed != speed || newPerception != perception || newAggression != aggression;
        return child;
    }

    private int mutate(int value, int min, int max) {
        if (random.nextDouble() < mutationRate) {
            int delta = random.nextBoolean() ? 1 : -1;
            return Math.max(min, Math.min(max, value + delta));
        }
        return value;
    }

    public void tick(World world) {
        if (!alive) return;
        age++;
        consumeEnergy(1 + (speed / 3));
        if (age >= maxAge || energy <= 0) {
            alive = false;
            return;
        }
        if (type == CreatureType.HERBIVORE) {
            behaveHerbivore(world);
        } else {
            behavePredator(world);
        }
    }

    private void behaveHerbivore(World world) {
        int[] food = world.nearestFood(x, y);
        if (food != null && dist(food[0], food[1]) <= perception) {
            moveToward(food[0], food[1], world);
            if (x == food[0] && y == food[1])
                if (world.eatFoodAt(x, y)) energy = Math.min(100, energy + 25);
        } else {
            moveRandom(world);
        }
    }

    private void behavePredator(World world) {
        Creature prey = world.nearestPrey(x, y);
        if (prey != null && dist(prey.getX(), prey.getY()) <= perception) {
            moveToward(prey.getX(), prey.getY(), world);
            if (x == prey.getX() && y == prey.getY()) {
                prey.alive = false; 
                energy = Math.min(100, energy + 30 + aggression);
            }
        } else {
            moveRandom(world);
        }
    }

    private void moveToward(int tx, int ty, World world) {
        int steps = speed;
        while (steps-- > 0) {
            int dx = Integer.compare(tx, x);
            int dy = Integer.compare(ty, y);
            x = world.clampX(x + dx);
            y = world.clampY(y + dy);
        }
    }

    private void moveRandom(World world) {
        x = world.clampX(x + random.nextInt(3) - 1);
        y = world.clampY(y + random.nextInt(3) - 1);
    }

    private int dist(int ox, int oy) {
        return Math.abs(ox - x) + Math.abs(oy - y);
    }

    public void consumeEnergy(int amount) {
        energy = Math.max(0, energy - amount);
    }

    public int getX()             { return x; }
    public int getY()             { return y; }
    public int getEnergy()        { return energy; }
    public boolean isAlive()      { return alive; }
    public CreatureType getType() { return type; }
    public int getAge()           { return age; }
    public int getGeneration()    { return generation; }
    public int getSpeed()         { return speed; }
    public int getPerception()    { return perception; }
    public double getMutationRate(){ return mutationRate; }
    public int getAggression()    { return aggression; }
}
