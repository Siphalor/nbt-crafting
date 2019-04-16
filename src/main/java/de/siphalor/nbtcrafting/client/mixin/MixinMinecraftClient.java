package de.siphalor.nbtcrafting.client.mixin;

import de.siphalor.nbtcrafting.client.ClientCore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
	@Inject(method = "disconnect(Lnet/minecraft/client/gui/Screen;)V", at = @At("HEAD"))
	public void onDisconnect(Screen screen, CallbackInfo callbackInfo) {
		ClientCore.sentModPresent = false;
	}
}
