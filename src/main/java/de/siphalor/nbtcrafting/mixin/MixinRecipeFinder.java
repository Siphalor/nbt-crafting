/*
 * Copyright 2020-2021 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.siphalor.nbtcrafting.mixin;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.datafixers.util.Pair;
import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.util.duck.IItemStack;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
@Mixin(RecipeMatcher.class)
public abstract class MixinRecipeFinder {
	@Shadow
	public abstract void addInput(final ItemStack stack);

	@Shadow
	@Final
	public Int2IntMap inputs;

	@Unique
	private static int currentId = 1;
	@Unique
	private static Int2ObjectMap<Pair<Integer, NbtCompound>> id2StackMap = new Int2ObjectAVLTreeMap<>();
	@Unique
	private static LoadingCache<Pair<Integer, NbtCompound>, Integer> stack2IdMap = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).removalListener(notification -> {
				synchronized (id2StackMap) {
					id2StackMap.remove((Integer) notification.getValue());
				}
			}
	).build(new CacheLoader<Pair<Integer, NbtCompound>, Integer>() {
		@Override
		public Integer load(Pair<Integer, NbtCompound> key) throws Exception {
			synchronized (id2StackMap) {
				id2StackMap.put(currentId, key);
			}
			return currentId++;
		}
	});

	@Unique
	private static Pair<Integer, NbtCompound> getStackPair(ItemStack stack) {
		return new Pair<Integer, NbtCompound>(Registry.ITEM.getRawId(stack.getItem()), NbtUtil.getTagOrEmpty(stack));
	}

	@Inject(method = "match(Lnet/minecraft/recipe/Recipe;Lit/unimi/dsi/fastutil/ints/IntList;I)Z", at = @At("HEAD"))
	public void onFindRecipe(@SuppressWarnings("rawtypes") Recipe recipe, IntList ints, int int_1, CallbackInfoReturnable<Boolean> ci) {
		NbtCrafting.lastRecipeFinder = (RecipeMatcher) (Object) this;
	}

	@Inject(method = "countCrafts(Lnet/minecraft/recipe/Recipe;ILit/unimi/dsi/fastutil/ints/IntList;)I", at = @At("HEAD"))
	public void onCountCrafts(@SuppressWarnings("rawtypes") Recipe recipe, int int_1, IntList ints, CallbackInfoReturnable<Integer> ci) {
		NbtCrafting.lastRecipeFinder = (RecipeMatcher) (Object) this;
	}

	/**
	 * @reason Fixes nbt items to be excluded from matching sometimes? Shouldn't break anything.
	 * @author Siphalor
	 */
	@Overwrite
	public void addUnenchantedInput(final ItemStack stack) {
		addInput(stack);
	}

	/**
	 * @reason Makes this function nbt dependent
	 * @author Siphalor
	 */
	@Overwrite
	public static int getItemId(ItemStack stack) {
		int id = -1;
		if (stack.isEmpty()) {
			id = 0;
		} else {
			Pair<Integer, NbtCompound> stackPair = getStackPair(stack);
			try {
				id = stack2IdMap.get(stackPair);
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		return id;
	}

	/**
	 * @reason Makes this function nbt dependent
	 * @author Siphalor
	 */
	@Overwrite
	public static ItemStack getStackFromId(final int id) {
		synchronized (id2StackMap) {
			if (id2StackMap.containsKey(id)) {
				ItemStack result = new ItemStack(Item.byRawId(id2StackMap.get(id).getFirst()));
				((IItemStack) (Object) result).setRawTag(id2StackMap.get(id).getSecond());
				return result;
			}
		}
		return ItemStack.EMPTY;
	}
}
