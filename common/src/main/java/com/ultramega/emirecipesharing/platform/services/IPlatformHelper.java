package com.ultramega.emirecipesharing.platform.services;

import com.ultramega.emirecipesharing.Config;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;

public interface IPlatformHelper {
    <T extends CustomPacketPayload> void sendPacketToServer(T packet);

    <T extends CustomPacketPayload> void sendPacketToAllPlayers(@Nullable MinecraftServer server, T packet);

    Supplier<Config> getConfig();
}
