package de.siphalor.nbtcrafting.mixin.network;

import de.siphalor.nbtcrafting.network.ServerNetworkHandlerAccess;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerLoginNetworkHandler.class)
public class MixinServerLoginHandler implements ServerNetworkHandlerAccess {
	@Shadow
	@Final
	ClientConnection connection;

	@Override
	public ClientConnection nbtCrafting$getConnection() {
		return this.connection;
	}
}
