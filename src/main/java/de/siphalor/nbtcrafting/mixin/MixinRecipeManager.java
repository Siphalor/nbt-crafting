package de.siphalor.nbtcrafting.mixin;

import de.siphalor.nbtcrafting.util.duck.IRecipeManager;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class MixinRecipeManager implements IRecipeManager {
	@Shadow protected abstract <C extends Inventory, T extends Recipe<C>> Map<Identifier, Recipe<C>> getAllOfType(RecipeType<T> type);

	@Override
	public <C extends Inventory, T extends Recipe<C>> Map<Identifier, Recipe<C>> nbtCrafting$getAllOfType(RecipeType<T> type) {
		return getAllOfType(type);
	}
}
