package com.heavenlynr;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record HeavenlyHolderSyncPacket(List<UUID> holders) implements CustomPayload {

    public static final CustomPayload.Id<HeavenlyHolderSyncPacket> ID =
            new CustomPayload.Id<>(Identifier.of("heavenly_n_r", "holder_sync"));

    public static final PacketCodec<PacketByteBuf, HeavenlyHolderSyncPacket> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeVarInt(value.holders().size());
                for (UUID uuid : value.holders()) buf.writeUuid(uuid);
            },
            buf -> {
                int size = buf.readVarInt();
                List<UUID> list = new ArrayList<>(size);
                for (int i = 0; i < size; i++) list.add(buf.readUuid());
                return new HeavenlyHolderSyncPacket(list);
            }
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
