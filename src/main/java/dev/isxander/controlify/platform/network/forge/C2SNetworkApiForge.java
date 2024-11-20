//? if forge {
/*package dev.isxander.controlify.platform.network.forge;

import dev.isxander.controlify.platform.network.C2SNetworkApi;
import dev.isxander.controlify.platform.network.ControlifyPacketCodec;
import dev.isxander.controlify.platform.network.PacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class C2SNetworkApiForge implements C2SNetworkApi {
    public static final C2SNetworkApiForge INSTANCE = new C2SNetworkApiForge();

    @Override
    public <T> void sendPacket(ResourceLocation channel, T packet) {
        // TODO
    }

    @Override
    public <T> PacketPayload createPayload(ResourceLocation channel, T packet) {
        return null;
    }

    @Override
    public <T> void listenForPacket(ResourceLocation channel, PacketListener<T> listener) {
        // TODO
    }

    @Override
    public <T> void registerPacket(ResourceLocation channel, ControlifyPacketCodec<T> handler) {
        // TODO
    }

    private IEventBus getModEventBus() {
        return FMLJavaModLoadingContext.get().getModEventBus();
    }
}
*///?}
