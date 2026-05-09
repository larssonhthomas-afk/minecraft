package com.rankedsmprank.persistence;

import com.rankedsmprank.inventory.BagInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class BagDataStore {

    public record SavedBag(int usableSlots, Map<Integer, ItemStack> items) {}

    private final Path path;

    public BagDataStore(Path path) {
        this.path = path;
    }

    public void save(Map<UUID, BagInventory> inventories, RegistryWrapper.WrapperLookup registries) throws IOException {
        RegistryOps<NbtElement> ops = registries.getOps(NbtOps.INSTANCE);
        NbtCompound root = new NbtCompound();

        for (Map.Entry<UUID, BagInventory> entry : inventories.entrySet()) {
            BagInventory inv = entry.getValue();
            NbtCompound playerNbt = new NbtCompound();
            playerNbt.putInt("Slots", inv.getUsableSlots());

            NbtList items = new NbtList();
            for (int i = 0; i < inv.getUsableSlots(); i++) {
                ItemStack stack = inv.getStack(i);
                if (!stack.isEmpty()) {
                    NbtElement itemNbt = ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();
                    NbtCompound slotNbt = new NbtCompound();
                    slotNbt.putByte("Slot", (byte) i);
                    slotNbt.put("Item", itemNbt);
                    items.add(slotNbt);
                }
            }
            playerNbt.put("Items", items);
            root.put(entry.getKey().toString(), playerNbt);
        }

        Path parent = path.getParent();
        if (parent != null) Files.createDirectories(parent);
        NbtIo.writeCompressed(root, path);
    }

    public Map<UUID, SavedBag> load(RegistryWrapper.WrapperLookup registries) throws IOException {
        if (!Files.exists(path)) return Map.of();
        RegistryOps<NbtElement> ops = registries.getOps(NbtOps.INSTANCE);
        NbtCompound root = NbtIo.readCompressed(path, NbtSizeTracker.of(0x4000000L));
        Map<UUID, SavedBag> result = new LinkedHashMap<>();

        for (String uuidStr : root.getKeys()) {
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                continue;
            }
            NbtCompound playerNbt = root.getCompound(uuidStr);
            int usableSlots = playerNbt.getInt("Slots");
            if (usableSlots <= 0) continue;

            NbtList itemList = playerNbt.getList("Items", NbtElement.COMPOUND_TYPE);
            Map<Integer, ItemStack> items = new LinkedHashMap<>();
            for (int i = 0; i < itemList.size(); i++) {
                NbtCompound slotNbt = itemList.getCompound(i);
                int slot = slotNbt.getByte("Slot") & 0xFF;
                NbtElement itemNbt = slotNbt.get("Item");
                if (itemNbt != null) {
                    ItemStack.CODEC.parse(ops, itemNbt).result()
                            .ifPresent(stack -> items.put(slot, stack));
                }
            }
            result.put(uuid, new SavedBag(usableSlots, items));
        }
        return result;
    }
}
