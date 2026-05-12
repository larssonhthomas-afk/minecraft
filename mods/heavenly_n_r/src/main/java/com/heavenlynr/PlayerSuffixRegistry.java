package com.heavenlynr;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public final class PlayerSuffixRegistry {

    private static final List<Function<UUID, Text>> providers = new ArrayList<>();

    private PlayerSuffixRegistry() {}

    public static synchronized void register(Function<UUID, Text> provider) {
        providers.add(provider);
    }

    /** Returns combined suffix text, or null if no provider yielded a suffix for this UUID. */
    public static synchronized Text buildSuffixes(UUID uuid) {
        MutableText result = null;
        for (Function<UUID, Text> provider : providers) {
            Text suffix = provider.apply(uuid);
            if (suffix != null) {
                if (result == null) result = Text.empty();
                result = result.append(suffix);
            }
        }
        return result;
    }
}
