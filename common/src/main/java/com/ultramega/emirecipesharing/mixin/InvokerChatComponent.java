package com.ultramega.emirecipesharing.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChatComponent.class)
public interface InvokerChatComponent {
    @Invoker(value = "getWidth", remap = false)
    int emirs$getWidth();

    @Invoker(value = "getHeight", remap = false)
    int emirs$getHeight();
}
