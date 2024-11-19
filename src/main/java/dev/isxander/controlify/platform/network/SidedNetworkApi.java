package dev.isxander.controlify.platform.network;

import net.minecraft.resources.ResourceLocation;

public interface SidedNetworkApi {
    static C2SNetworkApi C2S() {
        //? if fabric
        return dev.isxander.controlify.platform.network.fabric.C2SNetworkApiFabric.INSTANCE;
        //? if neoforge
        /*return dev.isxander.controlify.platform.network.neoforge.C2SNetworkApiNeoforge.INSTANCE;*/
        //? if forge
        /*return dev.isxander.controlify.platform.network.forge.C2SNetworkApiForge.INSTANCE;*/
    }

    static S2CNetworkApi S2C() {
        //? if fabric
        return dev.isxander.controlify.platform.network.fabric.S2CNetworkApiFabric.INSTANCE;
        //? if neoforge
        /*return dev.isxander.controlify.platform.network.neoforge.S2CNetworkApiNeoforge.INSTANCE;*/
        //? if forge
        /*return dev.isxander.controlify.platform.network.forge.S2CNetworkApiForge.INSTANCE;*/
    }

    <T> void registerPacket(ResourceLocation channel, ControlifyPacketCodec<T> handler);
}
