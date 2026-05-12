package com.heavenlynr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class HeavenlyClientState {

    private static final Set<UUID> holders = new HashSet<>();

    private HeavenlyClientState() {}

    public static synchronized void update(Collection<UUID> newHolders) {
        holders.clear();
        holders.addAll(newHolders);
    }

    public static synchronized boolean hasAbility(UUID uuid) {
        return holders.contains(uuid);
    }
}
