package com.heavenlynr.mixin;

import com.heavenlynr.PlayerSuffixRegistry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ServerPlayerEntity.class, priority = 2000)
public abstract class PlayerListNameMixin {

    @Inject(method = "getPlayerListName()Lnet/minecraft/text/Text;", at = @At("RETURN"), cancellable = true)
    private void heavenlyNR$fixTabListName(CallbackInfoReturnable<Text> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (PlayerSuffixRegistry.buildSuffixes(player.getUuid()) == null) return;
        cir.setReturnValue(player.getDisplayName());
    }
}
