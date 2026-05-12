package com.heavenlynr;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public final class HeavenlyClientMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(
                HeavenlyHolderSyncPacket.ID,
                (payload, context) -> context.client().execute(
                        () -> HeavenlyClientState.update(payload.holders())));
    }
}
