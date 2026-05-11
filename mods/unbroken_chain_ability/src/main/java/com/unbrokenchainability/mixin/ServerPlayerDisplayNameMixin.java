package com.unbrokenchainability.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

// ServerPlayerEntity does not override getDisplayName(); nametag suffix handled by PlayerDisplayNameMixin on PlayerEntity.
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerDisplayNameMixin {
}
