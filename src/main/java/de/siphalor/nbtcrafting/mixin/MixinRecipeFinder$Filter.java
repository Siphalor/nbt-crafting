package de.siphalor.nbtcrafting.mixin;

import de.siphalor.nbtcrafting.Core;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeFinder;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;
import java.util.List;

@Mixin(targets = "net/minecraft/recipe/RecipeFinder$Filter")
public abstract class MixinRecipeFinder$Filter {
    @Shadow(aliases = "field_7552", remap = false) @Final
	private List<Ingredient> ingredients;
	
	@Shadow(aliases = "field_7551", remap = false) @Final
	private int[] inputs;
	
	@Shadow(aliases = "field_7558", remap = false) @Final
	private BitSet bitSet;
	
	@Shadow
	protected abstract int method_7420(final boolean bool, final int int_1, final int int_2);

	@Unique
	private RecipeFinder owner;

	@SuppressWarnings({"UnresolvedMixinReference", "WeakerAccess"})
	@Inject(
		method = "<init>(Lnet/minecraft/recipe/RecipeFinder;Lnet/minecraft/recipe/Recipe;)V",
		at = @At("RETURN")
	)
	public void onConstruct(RecipeFinder recipeFinder, Recipe<?> recipe, CallbackInfo ci) {
		this.bitSet.clear();
		for(int j = 0; j < ingredients.size(); j++) {
			Ingredient ingredient = ingredients.get(j);
			for (int i = 0; i < inputs.length; i++) {
				if(ingredient.test(RecipeFinder.getStackFromId(inputs[i])))
					this.bitSet.set(method_7420(true, i, j));
			}
		}
	}

	/**
	 * @reason Builds the idToAmountMap but with calls to ingredient matches
	 * @author Siphalor
	 */
	@Overwrite
	private int[] method_7422() {
		owner = Core.lastRecipeFinder;
		IntCollection ints = new IntAVLTreeSet();
		for(int id : owner.idToAmountMap.keySet()) {
			for (Ingredient ingredient : ingredients) {
				if (ingredient.test(RecipeFinder.getStackFromId(id)))
					ints.add(id);
			}
		}
		return ints.toIntArray();
	}

	/**
	 * @reason now checks whether the ingredient matches instead of using getId()
	 * @author Siphalor
	 */
	@Overwrite
	private int method_7415() {
		int result = Integer.MAX_VALUE;
		for (final Ingredient ingredient : this.ingredients) {
			int maxPerIngredient = 0;
			for(int id : owner.idToAmountMap.keySet()) {
				if(ingredient.test(RecipeFinder.getStackFromId(id)))
					maxPerIngredient = Math.max(maxPerIngredient, owner.idToAmountMap.get(id));
			}
			if (result > 0) {
				result = Math.min(result, maxPerIngredient);
			}
		}
		return result;
	}
}
