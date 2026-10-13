package com.exemplo.luz;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/** Cliente -> servidor: "ajuste o alcance deste bloco para N". */
public record AlcancePayload(BlockPos pos, int alcance) implements CustomPayload {
    public static final CustomPayload.Id<AlcancePayload> ID =
            new CustomPayload.Id<>(Identifier.of(LuzAjustavel.MOD_ID, "set_alcance"));

    public static final PacketCodec<RegistryByteBuf, AlcancePayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, AlcancePayload::pos,
            PacketCodecs.VAR_INT, AlcancePayload::alcance,
            AlcancePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
