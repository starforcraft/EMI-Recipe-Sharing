package com.ultramega.emirecipesharing.network;

import com.ultramega.emirecipesharing.Constants;
import com.ultramega.emirecipesharing.client.ClientRecipeShareManager;
import com.ultramega.emirecipesharing.platform.Services;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

public record ShareRecipePacket(ResourceLocation categoryId, ResourceLocation recipeId, String sharerName) implements CustomPacketPayload {
    public static final Type<ShareRecipePacket> TYPE = new Type<>(Constants.modLoc("share_recipe_data"));

    public static final StreamCodec<ByteBuf, ShareRecipePacket> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC, ShareRecipePacket::categoryId,
        ResourceLocation.STREAM_CODEC, ShareRecipePacket::recipeId,
        ByteBufCodecs.stringUtf8(16), ShareRecipePacket::sharerName,
        ShareRecipePacket::new
    );

    private static final long SHARE_COOLDOWN_MILLIS = 3_000L;
    private static final Map<UUID, Long> LAST_SHARE_TIMES = new ConcurrentHashMap<>();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static String getSharerName(@Nullable final Player player) {
        if (player != null) {
            final String name = player.getName().getString();
            // Safety in case some modded heuristics allows player names longer than 16 chars
            return name.substring(0, Math.min(16, name.length()));
        }
        return Component.translatable("misc.emirecipesharing.unknown").getString();
    }

    public static void handleServer(final ShareRecipePacket payload, @Nullable final Player player, @Nullable final MinecraftServer server) {
        if (player != null && isOnCooldown(player)) {
            return;
        }

        Services.PLATFORM.sendPacketToAllPlayers(server, payload);
    }

    public static void handleClient(final ShareRecipePacket payload, final PacketContext ctx) {
        ClientRecipeShareManager.receive(payload, ctx.getPlayer());
    }

    private static boolean isOnCooldown(final Player player) {
        final UUID playerId = player.getUUID();
        final long now = System.currentTimeMillis();

        LAST_SHARE_TIMES.entrySet().removeIf(entry -> now - entry.getValue() >= SHARE_COOLDOWN_MILLIS);

        final Long lastShareTime = LAST_SHARE_TIMES.get(playerId);
        if (lastShareTime != null) {
            final long elapsed = now - lastShareTime;
            if (elapsed < SHARE_COOLDOWN_MILLIS) {
                final long remainingSeconds = (SHARE_COOLDOWN_MILLIS - elapsed + 999L) / 1_000L;
                player.sendSystemMessage(Component.translatable("misc.emirecipesharing.share_cooldown", remainingSeconds));
                return true;
            }
        }

        LAST_SHARE_TIMES.put(playerId, now);
        return false;
    }
}
