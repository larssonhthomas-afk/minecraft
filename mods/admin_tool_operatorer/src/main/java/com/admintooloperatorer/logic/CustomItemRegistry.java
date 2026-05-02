package com.admintooloperatorer.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry of all valid custom item IDs for /give customitem.
 * Base items are fixed; enchant-book IDs are extensible at runtime.
 */
public final class CustomItemRegistry {

    private static final List<String> BASE_ITEMS = List.of(
            "neutral_player_head",
            "warden_key",
            "whiter_key",
            "dragon_key",
            "immunity",
            "extinguish",
            "endurance"
    );

    private static final List<String> ENCHANT_BOOKS = new ArrayList<>(List.of("unbroken_chain"));

    private CustomItemRegistry() {}

    public static List<String> allItemIds() {
        List<String> all = new ArrayList<>(BASE_ITEMS);
        all.addAll(ENCHANT_BOOKS);
        return Collections.unmodifiableList(all);
    }

    public static boolean isValid(String id) {
        if (id == null) return false;
        return BASE_ITEMS.stream().anyMatch(id::equalsIgnoreCase)
                || ENCHANT_BOOKS.stream().anyMatch(id::equalsIgnoreCase);
    }

    /** Called by future enchant mods to add their book ID to this command. */
    public static void registerEnchantBook(String id) {
        if (ENCHANT_BOOKS.stream().noneMatch(id::equalsIgnoreCase)) {
            ENCHANT_BOOKS.add(id);
        }
    }
}
