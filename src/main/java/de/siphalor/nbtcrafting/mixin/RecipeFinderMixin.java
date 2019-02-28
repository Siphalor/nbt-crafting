package de.siphalor.nbtcrafting.mixin;

import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import de.siphalor.nbtcrafting.Core;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("ALL")
@Mixin(RecipeFinder.class)
public abstract class RecipeFinderMixin {
	
	private static HashBiMap<Pair<Integer, CompoundTag>, Integer> itemStackMap = HashBiMap.create();
	
	private static Pair<Integer, CompoundTag> getStackPair(ItemStack stack) {
		return new Pair<Integer, CompoundTag>(Registry.ITEM.getRawId(stack.getItem()), stack.getOrCreateTag());
	}

	@Inject(method = "findRecipe(Lnet/minecraft/recipe/Recipe;Lit/unimi/dsi/fastutil/ints/IntList;I)Z", at = @At("HEAD"))
	public void onFindRecipe(@SuppressWarnings("rawtypes") Recipe recipe, IntList ints, int int_1, CallbackInfoReturnable<Boolean> ci) {
		Core.lastRecipeFinder = (RecipeFinder)(Object)this;
	}	
	@Inject(method = "countRecipeCrafts(Lnet/minecraft/recipe/Recipe;ILit/unimi/dsi/fastutil/ints/IntList;)I", at = @At("HEAD"))
	public void onCountCrafts(@SuppressWarnings("rawtypes") Recipe recipe, int int_1, IntList ints, CallbackInfoReturnable<Integer> ci) {
		Core.lastRecipeFinder = (RecipeFinder)(Object)this;
	}
	
	@Shadow
	public abstract void addItem(final ItemStack stack);
	
	@Overwrite
	public void addNormalItem(final ItemStack stack) {
		addItem(stack);
	}

	@Overwrite
	public static int getItemId(ItemStack stack) {
		Pair<Integer, CompoundTag> stackPair = getStackPair(stack);
		if(itemStackMap.containsKey(stackPair)) {
			return itemStackMap.get(stackPair);
		}
		itemStackMap.put(stackPair, itemStackMap.size());
		return itemStackMap.getOrDefault(stackPair, 0);
	}
	
	@Overwrite
	public static ItemStack getStackFromId(final int id) {
		if(itemStackMap.containsValue(id)) {
			ItemStack result = new ItemStack(Item.byRawId(itemStackMap.inverse().get(id).getFirst()));
			result.setTag(itemStackMap.inverse().get(id).getSecond());
			return result;
		}
		return ItemStack.EMPTY;
	}
}
