package com.ultramega.emirecipesharing.fabric;

import com.ultramega.emirecipesharing.network.ShareRecipePacket;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ModInitializerImpl implements ModInitializer {
    @Override
    public void onInitialize() {
        ConfigImpl.register();
        this.registerPackets();
        this.registerPacketHandlers();
    }

    private void registerPackets() {
        PayloadTypeRegistry.playC2S().register(ShareRecipePacket.TYPE, ShareRecipePacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ShareRecipePacket.TYPE, ShareRecipePacket.STREAM_CODEC);
    }

    private void registerPacketHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(
            ShareRecipePacket.TYPE,
            (packet, ctx) -> ShareRecipePacket.handleServer(packet, ctx.player(), ctx.server())
        );
    }
}
