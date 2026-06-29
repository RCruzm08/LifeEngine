package com.lifeengine.model;

import java.util.Random;

public class Creature {
    private int x, y;
    private CreatureType type;
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
    private final double heatTolerance;  

    public boolean justMutated = false;
    private int mutationCount = 0;

    
    public Creature(int x, int y, CreatureType type, int generation) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.generation = generation;
        this.energy = 50 + new Random().nextInt(20);
        this.maxAge = 30 + new Random().nextInt(20);
        this.speed = 1 + new Random().nextInt(3);
        this.perception = 2 + new Random().nextInt(5);
        this.mutationRate = 0.1 + new Random().nextDouble() * 0.2;
        this.aggression = type == CreatureType.PREDATOR ? 3 + new Random().nextInt(5)
                : type == CreatureType.OMNIVORE ? 2 + new Random().nextInt(4)
                : 1 + new Random().nextInt(3);
        this.heatTolerance = 0.5 + new Random().nextDouble() * 0.5;
        counter++;
    }

    
    private Creature(int x, int y, CreatureType type, int generation,
                     int speed, int perception, double mutationRate,
                     int aggression, double heatTolerance, int mutationCount) {
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
        this.heatTolerance = heatTolerance;
        this.mutationCount = mutationCount;
        counter++;
    }

    
    public Creature reproduce(int nx, int ny, int nextGeneration) {
        int newSpeed = mutate(speed, 1, 5);
        int newPerception = mutate(perception, 1, 8);
        int newAggression = mutate(aggression, 1, 10);
        double newMutRate = Math.min(0.6, Math.max(0.05,
                mutationRate + (random.nextDouble() - 0.5) * 0.05));
        double newHeatTol = Math.min(1.0, Math.max(0.1,
                heatTolerance + (random.nextDouble() - 0.5) * 0.05));
        boolean mutated = newSpeed != speed || newPerception != perception || newAggression != aggression;
        int newMutCount = mutationCount + (mutated ? 1 : 0);

        Creature child = new Creature(nx, ny, type, nextGeneration,
                newSpeed, newPerception, newMutRate, newAggression, newHeatTol, newMutCount);
        child.justMutated = mutated;
        return child;
    }

    
    public Creature reproduceWith(Creature partner, int nx, int ny, int nextGeneration) {
        int newSpeed = mutateFrom((speed + partner.speed) / 2, 1, 5);
        int newPerception = mutateFrom((perception + partner.perception) / 2, 1, 8);
        int newAggression = mutateFrom((aggression + partner.aggression) / 2, 1, 10);
        double newMutRate = Math.min(0.6, Math.max(0.05,
                (mutationRate + partner.mutationRate) / 2 + (random.nextDouble() - 0.5) * 0.03));
        double newHeatTol = Math.min(1.0, Math.max(0.1,
                (heatTolerance + partner.heatTolerance) / 2 + (random.nextDouble() - 0.5) * 0.04));
        boolean mutated = newSpeed != (speed + partner.speed) / 2
                || newPerception != (perception + partner.perception) / 2
                || newAggression != (aggression + partner.aggression) / 2;
        int newMutCount = Math.max(mutationCount, partner.mutationCount) + (mutated ? 1 : 0);

        Creature child = new Creature(nx, ny, type, nextGeneration,
                newSpeed, newPerception, newMutRate, newAggression, newHeatTol, newMutCount);
        child.justMutated = mutated;
        return child;
    }

    private int mutate(int value, int min, int max) {
        if (random.nextDouble() < mutationRate) {
            return Math.max(min, Math.min(max, value + (random.nextBoolean() ? 1 : -1)));
        }
        return value;
    }

    private int mutateFrom(int value, int min, int max) {
        if (random.nextDouble() < mutationRate) {
            return Math.max(min, Math.min(max, value + (random.nextBoolean() ? 1 : -1)));
        }
        return value;
    }

    
    public void tick(World world) {
        if (!alive) return;
        age++;

        
        Biome biome = world.getBiomeAt(x, y);
        double speedCost = 1 + (speed / 3.0);
        double biomePenalty = switch (biome) {
            case DESERT -> 1.5;
            case WATER -> 2.0;
            case WETLAND -> 1.15;
            case TUNDRA, HIGHLAND -> 1.3;
            default -> 1.0;
        };
        consumeEnergy((int) Math.round(speedCost * biomePenalty));

        if (age >= maxAge || energy <= 0) {
            alive = false;
            return;
        }

        switch (type) {
            case HERBIVORE -> behaveHerbivore(world);
            case PREDATOR  -> behavePredator(world);
            case OMNIVORE  -> behaveOmnivore(world);
        }
    }

    private void behaveHerbivore(World world) {
        
        Creature threat = world.nearestThreat(x, y, type);
        if (threat != null && dist(threat.getX(), threat.getY()) <= perception) {
            fleeFrom(threat.getX(), threat.getY(), world);
            return;
        }
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
        Creature prey = world.nearestPrey(x, y, CreatureType.HERBIVORE);
        if (prey != null && dist(prey.getX(), prey.getY()) <= perception) {
            moveToward(prey.getX(), prey.getY(), world);
            if (x == prey.getX() && y == prey.getY()) {
                prey.kill();
                energy = Math.min(100, energy + 30 + aggression);
            }
        } else {
            moveRandom(world);
        }
    }

    private void behaveOmnivore(World world) {
        
        if (energy < 50) {
            Creature prey = world.nearestPrey(x, y, CreatureType.HERBIVORE);
            if (prey != null && dist(prey.getX(), prey.getY()) <= perception) {
                moveToward(prey.getX(), prey.getY(), world);
                if (x == prey.getX() && y == prey.getY()) {
                    prey.kill();
                    energy = Math.min(100, energy + 20);
                }
                return;
            }
        }
        int[] food = world.nearestFood(x, y);
        if (food != null && dist(food[0], food[1]) <= perception) {
            moveToward(food[0], food[1], world);
            if (x == food[0] && y == food[1])
                if (world.eatFoodAt(x, y)) energy = Math.min(100, energy + 18);
        } else {
            moveRandom(world);
        }
    }

    private void moveToward(int tx, int ty, World world) {
        Biome biome = world.getBiomeAt(x, y);
        int steps = Math.max(1, (int)(speed * biome.speedMod));
        while (steps-- > 0) {
            int dx = Integer.compare(tx, x);
            int dy = Integer.compare(ty, y);
            x = world.clampX(x + dx);
            y = world.clampY(y + dy);
        }
    }

    private void fleeFrom(int tx, int ty, World world) {
        int dx = Integer.compare(x, tx); 
        int dy = Integer.compare(y, ty);
        x = world.clampX(x + dx * speed);
        y = world.clampY(y + dy * speed);
    }

    private void moveRandom(World world) {
        Biome biome = world.getBiomeAt(x, y);
        int step = Math.max(1, (int)(speed * biome.speedMod));
        x = world.clampX(x + random.nextInt(step * 2 + 1) - step);
        y = world.clampY(y + random.nextInt(step * 2 + 1) - step);
    }

    private int dist(int ox, int oy) {
        return Math.abs(ox - x) + Math.abs(oy - y);
    }

    
    public void kill() {
        this.alive = false;
    }

    public void consumeEnergy(int amount) {
        energy = Math.max(0, energy - amount);
    }

    
    public int getX()               { return x; }
    public int getY()               { return y; }
    public int getEnergy()          { return energy; }
    public boolean isAlive()        { return alive; }
    public CreatureType getType()   { return type; }
    public int getAge()             { return age; }
    public int getGeneration()      { return generation; }
    public int getSpeed()           { return speed; }
    public int getPerception()      { return perception; }
    public double getMutationRate() { return mutationRate; }
    public int getAggression()      { return aggression; }
    public double getHeatTolerance(){ return heatTolerance; }
    public int getMutationCount()   { return mutationCount; }
}
