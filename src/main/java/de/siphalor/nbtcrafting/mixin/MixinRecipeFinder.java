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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
@Mixin(RecipeFinder.class)
public abstract class MixinRecipeFinder {
	@Shadow
	public abstract void addItem(final ItemStack stack);

	@Shadow
	@Final
	public Int2IntMap idToAmountMap;

	@Unique
	private static int currentId = 1;
	@Unique
	private static Int2ObjectMap<Pair<Integer, CompoundTag>> id2StackMap = new Int2ObjectAVLTreeMap<>();
	@Unique
	private static LoadingCache<Pair<Integer, CompoundTag>, Integer> stack2IdMap = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).removalListener(notification ->
			id2StackMap.remove((Integer) notification.getValue())
	).build(new CacheLoader<Pair<Integer, CompoundTag>, Integer>() {
		@Override
		public Integer load(Pair<Integer, CompoundTag> key) throws Exception {
			id2StackMap.put(currentId, key);
			return currentId++;
		}
	});

	@Unique
	private static Pair<Integer, CompoundTag> getStackPair(ItemStack stack) {
		return new Pair<Integer, CompoundTag>(Registry.ITEM.getRawId(stack.getItem()), NbtUtil.getTagOrEmpty(stack));
	}

	@Inject(method = "findRecipe(Lnet/minecraft/recipe/Recipe;Lit/unimi/dsi/fastutil/ints/IntList;I)Z", at = @At("HEAD"))
	public void onFindRecipe(@SuppressWarnings("rawtypes") Recipe recipe, IntList ints, int int_1, CallbackInfoReturnable<Boolean> ci) {
		NbtCrafting.lastRecipeFinder = (RecipeFinder) (Object) this;
	}

	@Inject(method = "countRecipeCrafts(Lnet/minecraft/recipe/Recipe;ILit/unimi/dsi/fastutil/ints/IntList;)I", at = @At("HEAD"))
	public void onCountCrafts(@SuppressWarnings("rawtypes") Recipe recipe, int int_1, IntList ints, CallbackInfoReturnable<Integer> ci) {
		NbtCrafting.lastRecipeFinder = (RecipeFinder) (Object) this;
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
		int id = -1;
		if (stack.isEmpty()) {
			id = 0;
		} else {
			Pair<Integer, CompoundTag> stackPair = getStackPair(stack);
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
		if (id2StackMap.containsKey(id)) {
			ItemStack result = new ItemStack(Item.byRawId(id2StackMap.get(id).getFirst()));
			((IItemStack) (Object) result).setRawTag(id2StackMap.get(id).getSecond());
			return result;
		}
		return ItemStack.EMPTY;
	}
}
