package com.worldtweakancient.logic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class EnchantmentBanList {

    public static final String BREACH = "minecraft:breach";

    private final Set<String> banned;

    public EnchantmentBanList(Set<String> banned) {
        if (banned == null) {
            throw new IllegalArgumentException("banned får inte vara null");
        }
        for (String id : banned) {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("banned får inte innehålla null/blanka ID:n");
            }
        }
        this.banned = Collections.unmodifiableSet(new HashSet<>(banned));
    }

    public static EnchantmentBanList defaults() {
        Set<String> set = new HashSet<>();
        set.add(BREACH);
        return new EnchantmentBanList(set);
    }

    public boolean isBanned(String enchantmentId) {
        if (enchantmentId == null) return false;
        return banned.contains(enchantmentId);
    }

    public Set<String> banned() {
        return banned;
    }
}
