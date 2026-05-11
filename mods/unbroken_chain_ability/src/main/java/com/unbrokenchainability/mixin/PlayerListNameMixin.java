package com.unbrokenchainability.mixin;

import com.unbrokenchainability.UnbrokenChainAbilityMod;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerListNameMixin {

    @Inject(method = "getPlayerListName()Lnet/minecraft/text/Text;", at = @At("RETURN"), cancellable = true)
    private void unbrokenChainAbility$addUChainToTabList(CallbackInfoReturnable<Text> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (UnbrokenChainAbilityMod.dataStore() == null) return;
        if (!UnbrokenChainAbilityMod.dataStore().hasAbility(player.getUuid())) return;

        Text current = cir.getReturnValue();
        Text base = current != null ? current : player.getName();
        Text suffix = Text.literal(" [⛓]")
                .styled(s -> s.withColor(Formatting.GOLD).withBold(true));
        cir.setReturnValue(Text.empty().append(base).append(suffix));
    }
}
