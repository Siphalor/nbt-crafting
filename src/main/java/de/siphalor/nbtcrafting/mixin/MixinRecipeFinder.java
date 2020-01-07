package de.siphalor.nbtcrafting.mixin;

import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.util.duck.IItemStack;
import de.siphalor.nbtcrafting.util.nbt.NbtHelper;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("ALL")
@Mixin(RecipeFinder.class)
public abstract class MixinRecipeFinder {
	@Shadow
	public abstract void addItem(final ItemStack stack);

	@Shadow @Final public Int2IntMap idToAmountMap;

	@Unique
	private static HashBiMap<Pair<Integer, CompoundTag>, Integer> itemStackMap = HashBiMap.create();

	@Unique
	private static Pair<Integer, CompoundTag> getStackPair(ItemStack stack) {
		return new Pair<Integer, CompoundTag>(Registry.ITEM.getRawId(stack.getItem()), NbtHelper.getTagOrEmpty(stack));
	}

	@Inject(method = "findRecipe(Lnet/minecraft/recipe/Recipe;Lit/unimi/dsi/fastutil/ints/IntList;I)Z", at = @At("HEAD"))
	public void onFindRecipe(@SuppressWarnings("rawtypes") Recipe recipe, IntList ints, int int_1, CallbackInfoReturnable<Boolean> ci) {
		NbtCrafting.lastRecipeFinder = (RecipeFinder)(Object)this;
	}	
	@Inject(method = "countRecipeCrafts(Lnet/minecraft/recipe/Recipe;ILit/unimi/dsi/fastutil/ints/IntList;)I", at = @At("HEAD"))
	public void onCountCrafts(@SuppressWarnings("rawtypes") Recipe recipe, int int_1, IntList ints, CallbackInfoReturnable<Integer> ci) {
		NbtCrafting.lastRecipeFinder = (RecipeFinder)(Object)this;
	}

	/**
	 * @reason Fixes nbt items to be excluded from matching sometimes? Shouldn't break anything.
	 * @author Siphalor
	 */
	@Overwrite
	public void addNormalItem(final ItemStack stack) {
		addItem(stack);
	}

	/**
	 * @reason Makes this function nbt dependent
     * @author Siphalor
	 */
	@Overwrite
	public static int getItemId(ItemStack stack) {
		Pair<Integer, CompoundTag> stackPair = getStackPair(stack);
		if(itemStackMap.containsKey(stackPair)) {
			return itemStackMap.get(stackPair);
		}
		itemStackMap.put(stackPair, itemStackMap.size() + 1);
		return itemStackMap.getOrDefault(stackPair, 0);
	}

	/**
	 * @reason Makes this function nbt dependent
     * @author Siphalor
	 */
	@Overwrite
	public static ItemStack getStackFromId(final int id) {
		if(itemStackMap.containsValue(id)) {
			ItemStack result = new ItemStack(Item.byRawId(itemStackMap.inverse().get(id).getFirst()));
			((IItemStack)(Object) result).setRawTag(itemStackMap.inverse().get(id).getSecond());
			return result;
		}
		return ItemStack.EMPTY;
	}
}
