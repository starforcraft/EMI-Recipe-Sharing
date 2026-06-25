package com.ultramega.emirecipesharing.fabric.platform;

import com.ultramega.emirecipesharing.Config;
import com.ultramega.emirecipesharing.fabric.ConfigImpl;
import com.ultramega.emirecipesharing.platform.services.IPlatformHelper;

import java.util.function.Supplier;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public <T extends CustomPacketPayload> void sendPacketToServer(final T packet) {
        ClientPlayNetworking.send(packet);
    }

    @Override
    public <T extends CustomPacketPayload> void sendPacketToAllPlayers(final MinecraftServer server, final T packet) {
        for (final ServerPlayer player : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(player, packet);
        }
    }

    @Override
    public Supplier<Config> getConfig() {
        return ConfigImpl::get;
    }
}
