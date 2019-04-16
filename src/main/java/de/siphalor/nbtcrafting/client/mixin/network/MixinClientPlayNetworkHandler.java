package de.siphalor.nbtcrafting.client.mixin.network;

import de.siphalor.nbtcrafting.client.ClientCore;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.SynchronizeRecipesS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

	@Inject(method = "onSynchronizeRecipes", at = @At("RETURN"))
	public void onGameJoin(SynchronizeRecipesS2CPacket packet, CallbackInfo callbackInfo) {
		if(!ClientCore.sentModPresent) {
            ClientCore.sendModPresent();
		}
	}
}
