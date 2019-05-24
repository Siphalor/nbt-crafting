package de.siphalor.nbtcrafting.mixin;

import de.siphalor.nbtcrafting.util.RecipeUtil;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapelessRecipe.class)
public class MixinShapelessRecipe {
	@Shadow @Final private ItemStack output;

	@Shadow @Final private DefaultedList<Ingredient> input;

	@Inject(method = "method_17729", at = @At("HEAD"), cancellable = true)
	public void craft(CraftingInventory craftingInventory, CallbackInfoReturnable<ItemStack> callbackInfoReturnable) {
        ItemStack result = RecipeUtil.getDollarAppliedOutputStack(output, input, craftingInventory);
        if(result != null) callbackInfoReturnable.setReturnValue(result);
	}
}
