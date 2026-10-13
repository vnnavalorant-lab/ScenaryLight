package com.exemplo.luz;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class LuzAjustavel implements ModInitializer {
    public static final String MOD_ID = "luzajustavel";

    public static Block LUZ_BLOCK;
    public static Item LUZ_ITEM;

    @Override
    public void onInitialize() {
        Identifier id = Identifier.of(MOD_ID, "luz_ajustavel");

        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, id);
        LUZ_BLOCK = Registry.register(Registries.BLOCK, blockKey,
                new LuzBlock(AbstractBlock.Settings.create()
                        .registryKey(blockKey)
                        .strength(0.3f)
                        .sounds(BlockSoundGroup.GLASS)
                        .nonOpaque()
                        .pistonBehavior(PistonBehavior.DESTROY)
                        // O nivel de luz vem da propriedade "alcance"
                        .luminance(state -> state.get(LuzBlock.ALCANCE))));

        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);
        LUZ_ITEM = Registry.register(Registries.ITEM, itemKey,
                new BlockItem(LUZ_BLOCK, new Item.Settings()
                        .registryKey(itemKey)
                        .useBlockPrefixedTranslationKey()));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
                .register(entries -> entries.add(LUZ_ITEM));
    }
}
