package com.lifeengine.model;

public enum CreatureType {
    HERBIVORE("H"),
    PREDATOR("P");

    public final String symbol;

    CreatureType(String symbol) {
        this.symbol = symbol;
    }
}
