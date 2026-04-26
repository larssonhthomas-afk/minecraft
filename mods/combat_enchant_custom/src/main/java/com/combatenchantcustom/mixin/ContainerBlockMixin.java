package com.combatenchantcustom.mixin;

import com.combatenchantcustom.integration.ItemMarker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class ContainerBlockMixin {

    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void combatEnchantCustom$blockChainSword(int slotIndex, int button,
            SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        ScreenHandler self = (ScreenHandler) (Object) this;

        if (!(self instanceof GenericContainerScreenHandler)
                && !(self instanceof ShulkerBoxScreenHandler)) return;

        // Block placing a chain sword from the cursor into any slot
        if (ItemMarker.hasChainEnchant(self.getCursorStack())) {
            ci.cancel();
            return;
        }

        // Block shift-clicking a chain sword from the player's own inventory into the container
        if (actionType == SlotActionType.QUICK_MOVE
                && slotIndex >= 0 && slotIndex < self.slots.size()) {
            Slot slot = self.slots.get(slotIndex);
            if (slot.inventory instanceof PlayerInventory
                    && ItemMarker.hasChainEnchant(slot.getStack())) {
                ci.cancel();
            }
        }
    }
}
