package com.axecleavingcleaving.mixin;

import com.axecleavingcleaving.AxeCleavingCleavingMod;
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
    private void axc$blockCleavingAxe(int slotIndex, int button,
            SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        ScreenHandler self = (ScreenHandler)(Object)this;

        if (!(self instanceof GenericContainerScreenHandler)
                && !(self instanceof ShulkerBoxScreenHandler)) return;

        // Block dragging/placing a Cleaving axe from the cursor into any slot
        if (AxeCleavingCleavingMod.hasCleaving(self.getCursorStack())) {
            ci.cancel();
            return;
        }

        // Block shift-clicking a Cleaving axe from the player inventory into the container
        if (actionType == SlotActionType.QUICK_MOVE
                && slotIndex >= 0 && slotIndex < self.slots.size()) {
            Slot slot = self.slots.get(slotIndex);
            if (slot.inventory instanceof PlayerInventory
                    && AxeCleavingCleavingMod.hasCleaving(slot.getStack())) {
                ci.cancel();
            }
        }
    }
}
