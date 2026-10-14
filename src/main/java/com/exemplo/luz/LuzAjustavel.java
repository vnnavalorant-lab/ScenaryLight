package com.exemplo.luz;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class LuzAjustavel implements ModInitializer {
    public static final String MOD_ID = "luzajustavel";

    public static Block LUZ_BLOCK;
    public static Item LUZ_ITEM;

    @Override
    public void onInitialize() {
        // ---- Bloco ----
        Identifier id = Identifier.of(MOD_ID, "luz_ajustavel");
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, id);
        LUZ_BLOCK = Registry.register(Registries.BLOCK, blockKey,
                new LuzBlock(AbstractBlock.Settings.create()
                        .registryKey(blockKey)
                        .strength(0.3f)
                        .sounds(BlockSoundGroup.GLASS)
                        .nonOpaque()
                        .pistonBehavior(PistonBehavior.DESTROY)
                        // vanilla so vai ate 15; acima disso a luz extra vem do shader
                        .luminance(state -> Math.min(15, state.get(LuzBlock.ALCANCE)))));

        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);
        LUZ_ITEM = Registry.register(Registries.ITEM, itemKey,
                new BlockItem(LUZ_BLOCK, new Item.Settings()
                        .registryKey(itemKey)
                        .useBlockPrefixedTranslationKey()));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
                .register(entries -> entries.add(LUZ_ITEM));

        // ---- Rede: o cliente pede um novo alcance, o servidor valida e aplica ----
        PayloadTypeRegistry.playC2S().register(AlcancePayload.ID, AlcancePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(AlcancePayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            ItemStack mao = player.getMainHandStack();
            if (!(mao.isEmpty() || mao.isOf(LUZ_ITEM))) return;

            BlockPos pos = payload.pos();
            if (player.squaredDistanceTo(Vec3d.ofCenter(pos)) > 128.0 * 128.0) return;

            int novo = Math.max(1, Math.min(LuzBlock.MAX_ALCANCE, payload.alcance()));
            for (ServerWorld w : context.server().getWorlds()) {
                BlockState s = w.getBlockState(pos);
                if (s.isOf(LUZ_BLOCK) && s.get(LuzBlock.ALCANCE) != novo) {
                    w.setBlockState(pos, s.with(LuzBlock.ALCANCE, novo));
                }
            }
        });
    }
}
