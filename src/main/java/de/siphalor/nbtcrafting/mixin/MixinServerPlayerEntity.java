package de.siphalor.nbtcrafting.mixin;

import de.siphalor.nbtcrafting.util.duck.IServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity implements IServerPlayerEntity {
	@Unique
	private boolean clientModPresent = false;

	@Unique
	@Override
	public boolean hasClientMod() {
		return clientModPresent;
	}

	@Unique
	@Override
	public void setClientModPresent(boolean present) {
		clientModPresent = present;
	}
}
