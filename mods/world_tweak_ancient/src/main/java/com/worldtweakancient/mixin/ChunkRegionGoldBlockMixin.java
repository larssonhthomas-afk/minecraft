package com.worldtweakancient.mixin;

import com.worldtweakancient.WorldTweakAncientMod;
import com.worldtweakancient.logic.NetherWorldGenBlockBanList;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkRegion.class)
public abstract class ChunkRegionGoldBlockMixin {

    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void worldTweakAncient$blockBannedNetherBlocks(
            BlockPos pos, BlockState state, int flags, int maxUpdateDepth,
            CallbackInfoReturnable<Boolean> cir) {
        NetherWorldGenBlockBanList list = WorldTweakAncientMod.blockBanList();
        if (list == null) return;
        ChunkRegion self = (ChunkRegion) (Object) this;
        ServerWorld world = self.toServerWorld();
        RegistryKey<World> dimension = world.getRegistryKey();
        Identifier dimensionId = dimension.getValue();
        Identifier blockId = Registries.BLOCK.getId(state.getBlock());
        if (dimensionId == null || blockId == null) return;
        if (list.shouldBlockInNether(dimensionId.toString(), blockId.toString())) {
            cir.setReturnValue(true);
        }
    }
}
