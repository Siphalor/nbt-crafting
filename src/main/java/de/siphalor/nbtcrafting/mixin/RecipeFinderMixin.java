package de.siphalor.nbtcrafting.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.util.registry.Registry;

@Mixin(RecipeFinder.class)
public abstract class RecipeFinderMixin {
	
	private static HashBiMap<Pair<Integer, CompoundTag>, Integer> itemStackMap = HashBiMap.create();
	
	private static Pair<Integer, CompoundTag> getStackPair(ItemStack stack) {
		return new Pair<Integer, CompoundTag>(Registry.ITEM.getRawId(stack.getItem()), stack.getOrCreateTag());
	}
	
	@Shadow
	public abstract void addItem(final ItemStack stack);
	
	@Inject(method = "addItem(Lnet/minecraft/item/ItemStack;)V", at = @At("TAIL"))
	private void onItemAdded(ItemStack stack, CallbackInfo ci) {
		if(!stack.isEmpty() && stack.hasTag()) {
			ItemStack taglessStack = stack.copy();
			taglessStack.setTag(null);
			addItem(taglessStack);
		}
	}
	
	@Overwrite
	public void addNormalItem(final ItemStack stack) {
		addItem(stack);
	}

	@Overwrite
	public static int getItemId(ItemStack stack) {
		Pair<Integer, CompoundTag> hashKey = getStackPair(stack);
		if(itemStackMap.containsKey(hashKey)) {
			return itemStackMap.get(hashKey);
		}
		itemStackMap.put(hashKey, itemStackMap.size());
		return itemStackMap.getOrDefault(hashKey, 0);
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
