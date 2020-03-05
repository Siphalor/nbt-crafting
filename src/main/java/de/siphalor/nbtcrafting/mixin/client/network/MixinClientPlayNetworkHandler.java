package de.siphalor.nbtcrafting.mixin.client.network;

import de.siphalor.nbtcrafting.client.NbtCraftingClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

	@Inject(method = "onSynchronizeRecipes", at = @At("RETURN"))
	public void onGameJoin(SynchronizeRecipesS2CPacket packet, CallbackInfo callbackInfo) {
		if(!NbtCraftingClient.sentModPresent) {
            NbtCraftingClient.sendModPresent();
		}
	}
}
