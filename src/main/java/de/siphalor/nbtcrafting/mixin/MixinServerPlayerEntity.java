package de.siphalor.nbtcrafting.mixin;

import de.siphalor.nbtcrafting.util.IServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity implements IServerPlayerEntity {
	private boolean nbtCrafting_clientModPresent = false;

	@Override
	public boolean nbtCrafting_hasClientMod() {
		return nbtCrafting_clientModPresent;
	}

	@Override
	public void nbtCrafting_setClientModPresent(boolean present) {
		nbtCrafting_clientModPresent = present;
	}
}
