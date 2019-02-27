package de.siphalor.nbtcrafting.mixin;

import de.siphalor.nbtcrafting.ingredient.IIngredient;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Recipe.class)
public interface RecipeMixin {
	@Shadow
	DefaultedList<Ingredient> getPreviewInputs();

	@Overwrite
	default DefaultedList<ItemStack> getRemainingStacks(Inventory inventory) {
		final DefaultedList<ItemStack> stackList = DefaultedList.<ItemStack>create(inventory.getInvSize(), ItemStack.EMPTY);
		main:
		for(int i = 0; i < stackList.size(); ++i) {
			ItemStack itemStack = inventory.getInvStack(i);
			for(Ingredient ingredient : getPreviewInputs()) {
				if(ingredient.matches(itemStack)) {
					ItemStack remainder = ((IIngredient)(Object) ingredient).getRecipeRemainder(itemStack);
					if(remainder != null) {
						stackList.set(i, remainder.copy());
						continue main;
					}
				}
			}
			if (itemStack.getItem().hasRecipeRemainder()) {
				stackList.set(i, new ItemStack(itemStack.getItem().getRecipeRemainder()));
			}
		}
		return stackList;
	}
}
