package de.siphalor.nbtcrafting.mixin.network;

import de.siphalor.nbtcrafting.Core;
import de.siphalor.nbtcrafting.util.IServerPlayerEntity;
import de.siphalor.nbtcrafting.util.ServerRecipe;
import net.minecraft.client.network.packet.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(SynchronizeRecipesS2CPacket.class)
public abstract class MixinSynchronizeRecipesS2CPacket {
	@Shadow private List<Recipe<?>> recipes;

	@Shadow
	public static <T extends Recipe<?>> void writeRecipe(T recipe, PacketByteBuf buf) {}

	@Inject(method = "write", at = @At("HEAD"), cancellable = true)
	public void onWrite(PacketByteBuf buf, CallbackInfo callbackInfo) {
		if(!((IServerPlayerEntity) Core.lastServerPlayerEntity).nbtCrafting_hasClientMod()) {
			List<Recipe> syncRecipes = recipes.stream().filter(recipe -> !(recipe instanceof ServerRecipe)).collect(Collectors.toList());
			buf.writeVarInt(syncRecipes.size());
			syncRecipes.forEach(recipe -> writeRecipe(recipe, buf));
			callbackInfo.cancel();
		}
	}
}
