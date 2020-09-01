package de.siphalor.nbtcrafting.mixin.network;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.ServerRecipe;
import de.siphalor.nbtcrafting.util.duck.IServerPlayerEntity;
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
	@Shadow
	private List<Recipe<?>> recipes;

	@Inject(method = "write", at = @At("HEAD"), cancellable = true)
	public void onWrite(PacketByteBuf buf, CallbackInfo callbackInfo) {
		if (!((IServerPlayerEntity) NbtCrafting.lastServerPlayerEntity).hasClientMod()) {
			List<Recipe<?>> syncRecipes = recipes.stream().filter(recipe -> !(recipe instanceof ServerRecipe)).collect(Collectors.toList());
			buf.writeVarInt(syncRecipes.size());
			for (Recipe<?> recipe : syncRecipes) {
				SynchronizeRecipesS2CPacket.writeRecipe(recipe, buf);
			}
			callbackInfo.cancel();
		}
	}
}
