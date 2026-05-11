package com.unbrokenchainability.logic;

public record ItemView(String itemId, String customName, boolean hasPlayerProfile) {

    public static ItemView empty() {
        return new ItemView("minecraft:air", null, false);
    }

    public boolean isEmpty() {
        return "minecraft:air".equals(itemId);
    }
}
