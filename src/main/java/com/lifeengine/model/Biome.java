package com.lifeengine.model;

public enum Biome {
    FOREST("Floresta",   1.0, 1.4, 0.5),  
    DESERT("Deserto",    0.7, 0.4, 2.0),  
    WATER("Água",        0.3, 0.0, 0.0),  
    PLAINS("Planície",   1.2, 1.0, 1.0);  

    public final String name;
    public final double speedMod;     
    public final double foodMod;      
    public final double heatDamageMod;

    Biome(String name, double speedMod, double foodMod, double heatDamageMod) {
        this.name = name;
        this.speedMod = speedMod;
        this.foodMod = foodMod;
        this.heatDamageMod = heatDamageMod;
    }
}
