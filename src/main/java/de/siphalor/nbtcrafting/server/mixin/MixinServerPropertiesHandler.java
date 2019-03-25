package de.siphalor.nbtcrafting.server.mixin;

import de.siphalor.nbtcrafting.Core;
import net.minecraft.server.dedicated.AbstractPropertiesHandler;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Properties;

@Mixin(ServerPropertiesHandler.class)
public abstract class MixinServerPropertiesHandler extends AbstractPropertiesHandler<ServerPropertiesHandler> {

	public MixinServerPropertiesHandler(Properties properties_1) {
		super(properties_1);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	public void onConstructed(Properties properties, CallbackInfo callbackInfo) {
		Core.vanillaCompatibility = this.parseBoolean("nbt-crafting.vanilla-compatibility", false);
	}
}
