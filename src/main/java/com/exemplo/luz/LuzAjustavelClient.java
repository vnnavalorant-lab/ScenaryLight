package com.exemplo.luz;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.render.BlockRenderLayer;

public class LuzAjustavelClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Pinta o bloco conforme a cor escolhida
        ColorProviderRegistry.BLOCK.register(
                (state, world, pos, tintIndex) -> LuzBlock.rgb(state.get(LuzBlock.COR)),
                LuzAjustavel.LUZ_BLOCK);

        BlockRenderLayerMap.INSTANCE.putBlock(LuzAjustavel.LUZ_BLOCK, BlockRenderLayer.CUTOUT);
    }
}
