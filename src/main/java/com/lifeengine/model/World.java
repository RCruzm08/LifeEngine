package com.lifeengine.model;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class World {
    public final int width;
    public final int height;

    
    private final List<Creature> creatures = new CopyOnWriteArrayList<>();
    private final List<int[]> foodSources = new CopyOnWriteArrayList<>();

    private final Random random = new Random();
    private int generation = 1;
    private int totalBorn = 0;
    private int totalDead = 0;
    private int foodRate = 20;
    private int reproRate = 15;
    private double mutBoost = 1.0;

    
    private final Biome[][] biomeGrid;

    
    private final int[][] heatmap;

    public World(int width, int height) {
        this.width = width;
        this.height = height;
        this.biomeGrid = new Biome[width][height];
        this.heatmap = new int[width][height];
        generateBiomes();
    }

    
    private void generateBiomes() {
        
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                biomeGrid[x][y] = Biome.PLAINS;

        
        placeCircularBiome(Biome.FOREST, width / 4, height / 4, 8);
        placeCircularBiome(Biome.FOREST, (int)(width * 0.7), (int)(height * 0.6), 7);

        
        placeCircularBiome(Biome.DESERT, (int)(width * 0.75), height / 5, 6);
        placeCircularBiome(Biome.DESERT, width / 6, (int)(height * 0.75), 5);

        placeCircularBiome(Biome.TUNDRA, width / 5, height / 9, 7);
        placeCircularBiome(Biome.TUNDRA, (int)(width * 0.82), (int)(height * 0.86), 6);

        placeCircularBiome(Biome.HIGHLAND, width / 2, (int)(height * 0.22), 6);
        placeCircularBiome(Biome.HIGHLAND, (int)(width * 0.42), (int)(height * 0.72), 7);

        placeCircularBiome(Biome.WETLAND, width / 2, height / 2, 8);
        placeCircularBiome(Biome.WATER, width / 2, height / 2, 4);
        placeRiver(0, height / 3, width - 1, height / 3 + 2);
    }

    private void placeCircularBiome(Biome biome, int cx, int cy, int radius) {
        for (int x = Math.max(0, cx - radius); x < Math.min(width, cx + radius); x++)
            for (int y = Math.max(0, cy - radius); y < Math.min(height, cy + radius); y++)
                if (Math.abs(x - cx) + Math.abs(y - cy) <= radius)
                    biomeGrid[x][y] = biome;
    }

    private void placeRiver(int x1, int y1, int x2, int y2) {
        int steps = Math.max(1, Math.abs(x2 - x1));
        for (int i = 0; i <= steps; i++) {
            int x = x1 + i;
            int yBase = y1 + (int)((double)(y2 - y1) * i / steps);
            for (int dy = -2; dy <= 3; dy++) {
                int y = Math.min(height - 1, Math.max(0, yBase + dy));
                biomeGrid[x][y] = Math.abs(dy) <= 1 ? Biome.WATER : Biome.WETLAND;
            }
        }
    }

    
    public void populate(int count) {
        int herbs = count / 2;
        int preds = count / 4;
        int omnis = count - herbs - preds;
        for (int i = 0; i < herbs; i++) spawnCreature(CreatureType.HERBIVORE);
        for (int i = 0; i < preds; i++) spawnCreature(CreatureType.PREDATOR);
        for (int i = 0; i < omnis; i++) spawnCreature(CreatureType.OMNIVORE);
    }

    private void spawnCreature(CreatureType type) {
        int x, y;
        do {
            x = random.nextInt(width);
            y = random.nextInt(height);
        } while (biomeGrid[x][y] == Biome.WATER);
        creatures.add(new Creature(x, y, type, generation));
        totalBorn++;
    }

    public void addFood(int count) {
        for (int i = 0; i < count; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            
            Biome b = biomeGrid[x][y];
            if (b != Biome.WATER && random.nextDouble() < b.foodMod)
                foodSources.add(new int[]{x, y});
        }
    }

    
    public List<String> tick() {
        List<String> events = new ArrayList<>();

        
        for (int i = 0; i < 3; i++) {
            if (random.nextInt(100) < foodRate) {
                int x = random.nextInt(width);
                int y = random.nextInt(height);
                Biome b = biomeGrid[x][y];
                if (b != Biome.WATER && random.nextDouble() < b.foodMod)
                    foodSources.add(new int[]{x, y});
            }
        }

        
        List<Creature> newborns = new ArrayList<>();
        List<Creature> livingCreatures = new ArrayList<>(creatures);

        for (Creature c : livingCreatures) {
            c.tick(this);
            heatmap[c.getX()][c.getY()] = Math.min(255, heatmap[c.getX()][c.getY()] + 1);

            if (!c.isAlive()) continue;
            if (c.getEnergy() <= 70) continue;
            if (random.nextInt(100) >= reproRate) continue;

            
            Creature partner = nearestSameType(c.getX(), c.getY(), c.getType(), c);
            Creature child;
            int nx = clampX(c.getX() + random.nextInt(3) - 1);
            int ny = clampY(c.getY() + random.nextInt(3) - 1);

            if (partner != null && partner.getEnergy() > 70) {
                child = c.reproduceWith(partner, nx, ny, generation + 1);
                partner.consumeEnergy(15);
            } else {
                child = c.reproduce(nx, ny, generation + 1);
            }

            newborns.add(child);
            c.consumeEnergy(25);
            totalBorn++;

            if (child.justMutated)
                events.add("mutate:" + describeCreature(child.getType())
                        + " gen-" + child.getGeneration()
                        + " spd=" + child.getSpeed()
                        + " perc=" + child.getPerception()
                        + " agg=" + child.getAggression()
                        + " mut#" + child.getMutationCount());
        }
        creatures.addAll(newborns);

        
        long before = creatures.size();
        creatures.removeIf(c -> !c.isAlive());
        int died = (int)(before - creatures.size());
        totalDead += died;
        if (died > 2) events.add("death:" + died + " criaturas morreram.");

        
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                if (heatmap[x][y] > 0) heatmap[x][y]--;

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

    public Creature nearestPrey(int x, int y, CreatureType preyType) {
        Creature best = null;
        int bestDist = Integer.MAX_VALUE;
        for (Creature c : creatures) {
            if (c.getType() == preyType && c.isAlive()) {
                int d = Math.abs(c.getX() - x) + Math.abs(c.getY() - y);
                if (d < bestDist) { bestDist = d; best = c; }
            }
        }
        return best;
    }

    
    public Creature nearestThreat(int x, int y, CreatureType myType) {
        Creature best = null;
        int bestDist = Integer.MAX_VALUE;
        for (Creature c : creatures) {
            boolean isThreat = (myType == CreatureType.HERBIVORE
                    && (c.getType() == CreatureType.PREDATOR || c.getType() == CreatureType.OMNIVORE))
                    || (myType == CreatureType.OMNIVORE && c.getType() == CreatureType.PREDATOR);
            if (isThreat && c.isAlive()) {
                int d = Math.abs(c.getX() - x) + Math.abs(c.getY() - y);
                if (d < bestDist) { bestDist = d; best = c; }
            }
        }
        return best;
    }

    
    private Creature nearestSameType(int x, int y, CreatureType type, Creature self) {
        Creature best = null;
        int bestDist = Integer.MAX_VALUE;
        for (Creature c : creatures) {
            if (c == self || c.getType() != type || !c.isAlive()) continue;
            int d = Math.abs(c.getX() - x) + Math.abs(c.getY() - y);
            if (d < 4 && d < bestDist) { bestDist = d; best = c; }
        }
        return best;
    }

    
    public void triggerCrisis() {
        creatures.stream()
                .filter(c -> c.getType() == CreatureType.PREDATOR)
                .forEach(Creature::kill);
        creatures.removeIf(c -> !c.isAlive());
    }

    public int triggerBloom() {
        int added = 0;
        for (int i = 0; i < 90; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            Biome b = biomeGrid[x][y];
            if (b != Biome.WATER && random.nextDouble() < Math.min(1.0, b.foodMod * 0.85)) {
                foodSources.add(new int[]{x, y});
                added++;
            }
        }
        return added;
    }

    public int addOasis() {
        int cx;
        int cy;
        do {
            cx = random.nextInt(width);
            cy = random.nextInt(height);
        } while (biomeGrid[cx][cy] == Biome.WATER);

        int wetlandRadius = 5 + random.nextInt(4);
        int waterRadius = Math.max(2, wetlandRadius - 3);
        placeCircularBiome(Biome.WETLAND, cx, cy, wetlandRadius);
        placeCircularBiome(Biome.WATER, cx, cy, waterRadius);

        int waterCells = 0;
        for (int x = Math.max(0, cx - waterRadius); x < Math.min(width, cx + waterRadius); x++) {
            for (int y = Math.max(0, cy - waterRadius); y < Math.min(height, cy + waterRadius); y++) {
                if (biomeGrid[x][y] == Biome.WATER) waterCells++;
            }
        }
        return waterCells;
    }

    public int triggerWildfire() {
        int cx;
        int cy;
        do {
            cx = random.nextInt(width);
            cy = random.nextInt(height);
        } while (biomeGrid[cx][cy] == Biome.WATER || biomeGrid[cx][cy] == Biome.WETLAND);

        int radius = 4 + random.nextInt(4);
        int beforeFood = foodSources.size();
        final int fx = cx;
        final int fy = cy;
        final int r2 = radius * radius;
        foodSources.removeIf(f -> {
            int dx = f[0] - fx;
            int dy = f[1] - fy;
            return dx * dx + dy * dy <= r2;
        });

        int killed = 0;
        for (Creature c : creatures) {
            int dx = c.getX() - cx;
            int dy = c.getY() - cy;
            if (dx * dx + dy * dy <= r2 && random.nextDouble() < 0.18) {
                c.kill();
                killed++;
            }
        }
        creatures.removeIf(c -> !c.isAlive());
        totalDead += killed;
        return (beforeFood - foodSources.size()) + killed;
    }

    public Map<String, Long> getBiomeSummary() {
        Map<String, Long> summary = new LinkedHashMap<>();
        for (Biome biome : Biome.values()) summary.put(biome.name(), 0L);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                String key = biomeGrid[x][y].name();
                summary.put(key, summary.get(key) + 1);
            }
        }
        return summary;
    }

    public Biome getBiomeAt(int x, int y) {
        return biomeGrid[clampX(x)][clampY(y)];
    }

    public int getHeatAt(int x, int y) {
        return heatmap[clampX(x)][clampY(y)];
    }

    public int clampX(int x) { return Math.max(0, Math.min(width - 1, x)); }
    public int clampY(int y) { return Math.max(0, Math.min(height - 1, y)); }

    private String describeCreature(CreatureType type) {
        return switch (type) {
            case HERBIVORE -> "Herbívoro";
            case PREDATOR  -> "Predador";
            case OMNIVORE  -> "Onívoro";
        };
    }

    
    public List<Creature> getCreatures()  { return creatures; }
    public List<int[]> getFoodSources()   { return foodSources; }
    public Biome[][] getBiomeGrid()       { return biomeGrid; }
    public int[][] getHeatmap()           { return heatmap; }
    public int getGeneration()            { return generation; }
    public int getTotalBorn()             { return totalBorn; }
    public int getTotalDead()             { return totalDead; }
    public void setFoodRate(int r)        { this.foodRate = r; }
    public void setReproRate(int r)       { this.reproRate = r; }
    public void setMutBoost(double b)     { this.mutBoost = b; }
}
