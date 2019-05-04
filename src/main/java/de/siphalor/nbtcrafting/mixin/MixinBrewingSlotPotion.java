package de.siphalor.nbtcrafting.mixin;

import de.siphalor.nbtcrafting.brewing.BrewingRecipe;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/container/BrewingStandContainer$SlotPotion")
public class MixinBrewingSlotPotion {
	@Inject(method = "matches", at = @At("HEAD"), cancellable = true)
	private static void matches(ItemStack stack, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		Object gameInstance = FabricLoader.getInstance().getGameInstance();
		RecipeManager recipeManager;
		if(gameInstance instanceof MinecraftClient) {
			recipeManager = ((MinecraftClient) gameInstance).getServer().getRecipeManager();
		} else if(gameInstance instanceof MinecraftServer) {
			recipeManager = ((MinecraftServer) gameInstance).getRecipeManager();
		} else {
			return;
		}
        if(BrewingRecipe.existsMatchingBase(stack, recipeManager))
        	callbackInfoReturnable.setReturnValue(true);
	}
}
