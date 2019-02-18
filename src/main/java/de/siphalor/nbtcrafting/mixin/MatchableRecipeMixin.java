package de.siphalor.nbtcrafting.mixin;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.siphalor.nbtcrafting.Core;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeFinder;

@Mixin(targets = "net/minecraft/recipe/RecipeFinder$MatchableRecipe")
public abstract class MatchableRecipeMixin {
	
	private RecipeFinder owner;

	@Shadow(aliases = "field_7552")
	private List<Ingredient> ingredients;
	
	@Shadow(aliases = "field_7551")
	private int[] inputs;
	
	@Shadow(aliases = "field_7558")
	private BitSet bitSet;
	
	@Shadow
	protected abstract int method_7420(final boolean bool, final int int_1, final int int_2);
	
	@Inject(
		method = "<init>",
		//at = @At(value = "FIELD", target = "Lnet/minecraft/recipe/RecipeFinder$MatchableRecipe;field_7558:Ljava/util/BitSet;", shift = Shift.AFTER),
		at = @At("RETURN"),
		cancellable = true
	)
	public void onConstruct(RecipeFinder rf, Recipe<?> recipe, CallbackInfo ci) {
		this.bitSet.clear();
		for(int j = 0; j < ingredients.size(); j++) {
			Ingredient ingredient = (Ingredient) ingredients.get(j);
			for (int i = 0; i < inputs.length; i++) {
				if(ingredient.matches(RecipeFinder.getStackFromId(inputs[i])))
					this.bitSet.set(method_7420(true, i, j));
			}
		}
		ci.cancel();
	}
	
	@Overwrite
	private int[] method_7422() {
		owner = Core.lastRecipeFinder;
		IntCollection ints = new IntAVLTreeSet();
		for(int id : owner.idToAmountMap.keySet()) {
			for (Iterator<Ingredient> iterator = ingredients.iterator(); iterator.hasNext();) {
				Ingredient ingredient = (Ingredient) iterator.next();
				if(ingredient.matches(RecipeFinder.getStackFromId(id)))
					ints.add(id);
			}
		}
		return ints.toIntArray();
	}
}
