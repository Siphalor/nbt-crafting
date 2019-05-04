package de.siphalor.nbtcrafting.mixin;

import de.siphalor.nbtcrafting.brewing.BrewingRecipe;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.container.Slot;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/container/BrewingStandContainer$SlotIngredient")
public abstract class MixinBrewingSlotIngredient extends Slot {
	public MixinBrewingSlotIngredient(Inventory inventory_1, int int_1, int int_2, int int_3) {
		super(inventory_1, int_1, int_2, int_3);
	}

	@Inject(method = "canInsert(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
	public void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		Object gameInstance = FabricLoader.getInstance().getGameInstance();
		RecipeManager recipeManager;
		if(gameInstance instanceof MinecraftClient) {
			recipeManager = ((MinecraftClient) gameInstance).getServer().getRecipeManager();
		} else if(gameInstance instanceof MinecraftServer) {
			recipeManager = ((MinecraftServer) gameInstance).getRecipeManager();
		} else {
			return;
		}
        if(BrewingRecipe.existsMatchingIngredient(stack, recipeManager))
        	callbackInfoReturnable.setReturnValue(true);
	}
}
