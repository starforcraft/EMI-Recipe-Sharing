package com.ultramega.emirecipesharing.neoforge.platform;

import com.ultramega.emirecipesharing.Config;
import com.ultramega.emirecipesharing.platform.services.IPlatformHelper;

import java.util.function.Supplier;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public <T extends CustomPacketPayload> void sendPacketToServer(final T packet) {
        PacketDistributor.sendToServer(packet);
    }

    @Override
    public <T extends CustomPacketPayload> void sendPacketToAllPlayers(final MinecraftServer server, final T packet) {
        PacketDistributor.sendToAllPlayers(packet);
    }

    @Override
    public Supplier<Config> getConfig() {
        return () -> ConfigImpl.INSTANCE;
    }
}
