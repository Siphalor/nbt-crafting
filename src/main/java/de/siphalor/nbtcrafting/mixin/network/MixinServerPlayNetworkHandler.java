package de.siphalor.nbtcrafting.mixin.network;


import de.siphalor.nbtcrafting.network.ServerNetworkHandlerAccess;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler implements ServerNetworkHandlerAccess {
	@Shadow
	@Final
	private ClientConnection connection;

	@Override
	public ClientConnection nbtCrafting$getConnection() {
		return this.connection;
	}
}
