package dev.eredrin.abilities.common;

import dev.eredrin.abilities.EredrinAbilities;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Пакет клиент -> сервер: применить способность с номером ability. */
public record UseAbilityPayload(int ability) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UseAbilityPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(EredrinAbilities.MOD_ID, "use_ability"));

    public static final StreamCodec<ByteBuf, UseAbilityPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, UseAbilityPayload::ability, UseAbilityPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
