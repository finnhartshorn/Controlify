package dev.isxander.controlify.platform.network.forge;

import dev.isxander.controlify.platform.network.ControlifyPacketCodec;
import dev.isxander.controlify.platform.network.S2CNetworkApi;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class S2CNetworkApiForge implements S2CNetworkApi {
    public static final S2CNetworkApiForge INSTANCE = new S2CNetworkApiForge();

    @Override
    public <T> void sendPacket(ServerPlayer recipient, ResourceLocation channel, T packet) {
        // TODO
    }

    @Override
    public <T> void listenForPacket(ResourceLocation channel, PacketListener<T> listener) {
        // TODO
    }

    @Override
    public <T> void registerPacket(ResourceLocation channel, ControlifyPacketCodec<T> handler) {
        // TODO
    }
}
