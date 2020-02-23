package de.siphalor.nbtcrafting.mixin.cutting;

import de.siphalor.nbtcrafting.api.RecipeUtil;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CuttingRecipe.class)
public class MixinCuttingRecipe {
	@Shadow @Final protected ItemStack output;

	@Shadow @Final protected Ingredient input;

	@Inject(method = "craft", at = @At("HEAD"), cancellable = true)
	public void craft(Inventory inventory, CallbackInfoReturnable<ItemStack> callbackInfoReturnable) {
		ItemStack result = RecipeUtil.getDollarAppliedOutputStack(output, input, inventory);
		if(result != null)
			callbackInfoReturnable.setReturnValue(result);
	}
}
