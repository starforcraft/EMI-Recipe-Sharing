package com.ultramega.emirecipesharing.neoforge;

import com.ultramega.emirecipesharing.neoforge.platform.ConfigImpl;
import com.ultramega.emirecipesharing.network.ShareRecipePacket;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static com.ultramega.emirecipesharing.Constants.MOD_ID;

@Mod(MOD_ID)
public class ModInitializerImpl {
    public ModInitializerImpl(final IEventBus eventBus, final ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, ConfigImpl.INSTANCE.getSpec());
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }

        eventBus.addListener(this::registerPackets);
    }

    private void registerPackets(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MOD_ID);

        registrar.playBidirectional(
            ShareRecipePacket.TYPE,
            ShareRecipePacket.STREAM_CODEC,
            new DirectionalPayloadHandler<>(
                (packet, ctx) -> ShareRecipePacket.handleServer(packet, ctx.player(), null),
                (packet, ctx) -> ShareRecipePacket.handleClient(packet, ctx::player)
            )
        );
    }
}
