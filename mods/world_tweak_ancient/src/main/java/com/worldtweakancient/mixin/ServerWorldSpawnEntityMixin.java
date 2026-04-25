package com.worldtweakancient.mixin;

import com.worldtweakancient.WorldTweakAncientMod;
import com.worldtweakancient.logic.EntitySpawnBlockList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public abstract class ServerWorldSpawnEntityMixin {

    @Inject(method = "spawnEntity(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void worldTweakAncient$blockSpawn(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        EntitySpawnBlockList list = WorldTweakAncientMod.spawnBlockList();
        if (list == null) return;
        EntityType<?> type = entity.getType();
        Identifier id = Registries.ENTITY_TYPE.getId(type);
        if (id != null && list.isSpawnBlocked(id.toString())) {
            cir.setReturnValue(false);
        }
    }
}
