package de.siphalor.nbtcrafting.mixin.network;

import de.siphalor.nbtcrafting.NbtCrafting;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler {
	@Shadow public ServerPlayerEntity player;

	@Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"))
	public void onSendPacket(Packet<?> packet, GenericFutureListener<?> futureListener, CallbackInfo callbackInfo) {
		NbtCrafting.lastServerPlayerEntity = player;
	}
}
