package com.glowingplayerheadom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.world.World;

// Abstract subclass solely to expose Entity.FLAGS (protected) without a Mixin accessor.
abstract class EntityFlagsHelper extends Entity {
    static final TrackedData<Byte> FLAGS_KEY = FLAGS;

    EntityFlagsHelper(EntityType<?> type, World world) {
        super(type, world);
    }
}
