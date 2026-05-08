package com.netheritedetvanilla;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class NetheriteDetVanillaMod implements ModInitializer {

    public static final String MOD_ID = "netherite_det_vanilla";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        registerKnockbackReduction();
        LOGGER.info("netherite_det_vanilla initierad: custom Netherite-recept + halverad knockback resistance");
    }

    private static void registerKnockbackReduction() {
        var armor = List.of(
            Items.NETHERITE_HELMET,
            Items.NETHERITE_CHESTPLATE,
            Items.NETHERITE_LEGGINGS,
            Items.NETHERITE_BOOTS
        );
        DefaultItemComponentEvents.MODIFY.register(context ->
            armor.forEach(item -> context.modify(item, builder -> {
                AttributeModifiersComponent current = item.getComponents()
                    .getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
                builder.add(DataComponentTypes.ATTRIBUTE_MODIFIERS, halveKnockback(current));
            }))
        );
    }

    private static AttributeModifiersComponent halveKnockback(AttributeModifiersComponent original) {
        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();
        for (AttributeModifiersComponent.Entry entry : original.modifiers()) {
            if (isKnockbackResistance(entry.attribute())) {
                builder.add(
                    entry.attribute(),
                    new EntityAttributeModifier(
                        entry.modifier().id(),
                        entry.modifier().value() * 0.5,
                        entry.modifier().operation()),
                    entry.slot());
            } else {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }
        return builder.build();
    }

    private static boolean isKnockbackResistance(RegistryEntry<EntityAttribute> attribute) {
        return attribute.getKey()
            .map(k -> k.getValue().getPath().contains("knockback_resistance"))
            .orElse(false);
    }
}
