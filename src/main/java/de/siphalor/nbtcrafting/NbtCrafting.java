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

package de.siphalor.nbtcrafting;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.datafixers.util.Pair;
import de.siphalor.nbtcrafting.advancement.StatChangedCriterion;
import de.siphalor.nbtcrafting.api.RecipeTypeHelper;
import de.siphalor.nbtcrafting.mixin.advancement.MixinCriterions;
import de.siphalor.nbtcrafting.recipe.AnvilRecipe;
import de.siphalor.nbtcrafting.recipe.BrewingRecipe;
import de.siphalor.nbtcrafting.recipe.IngredientRecipe;
import de.siphalor.nbtcrafting.recipe.cauldron.CauldronRecipe;
import de.siphalor.nbtcrafting.recipe.cauldron.CauldronRecipeSerializer;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class NbtCrafting implements ModInitializer {
	public static final String MOD_ID = "nbtcrafting";
	public static final String MOD_NAME = "Nbt Crafting";

	private static final String LOG_PREFIX = "[" + MOD_NAME + "] ";
	private static final Logger LOGGER = LogManager.getLogger();

	public static final Identifier UPDATE_ANVIL_TEXT_S2C_PACKET_ID = new Identifier(MOD_ID, "update_anvil_text");

	public static final RecipeType<AnvilRecipe> ANVIL_RECIPE_TYPE = registerRecipeType("anvil");
	@SuppressWarnings("unused")
	public static final RecipeSerializer<AnvilRecipe> ANVIL_RECIPE_SERIALIZER = registerRecipeSerializer("anvil", AnvilRecipe.SERIALIZER);

	public static final RecipeType<BrewingRecipe> BREWING_RECIPE_TYPE = registerRecipeType("brewing");
	@SuppressWarnings("unused")
	public static final RecipeSerializer<BrewingRecipe> BREWING_RECIPE_SERIALIZER = registerRecipeSerializer("brewing", BrewingRecipe.SERIALIZER);

	public static final RecipeType<CauldronRecipe> CAULDRON_RECIPE_TYPE = registerRecipeType("cauldron");
	public static final CauldronRecipeSerializer CAULDRON_RECIPE_SERIALIZER = registerRecipeSerializer("cauldron", new CauldronRecipeSerializer());

	public static final RecipeType<IngredientRecipe<Inventory>> SMITHING_RECIPE_TYPE = registerRecipeType("smithing");
	@SuppressWarnings("unused")
	public static final RecipeSerializer<IngredientRecipe<Inventory>> SMITHING_RECIPE_SERIALIZER = registerRecipeSerializer("smithing", new IngredientRecipe.Serializer<>((id, base, ingredient, result, serializer) -> new IngredientRecipe<>(id, base, ingredient, result, SMITHING_RECIPE_TYPE, serializer)));

	public static final StatChangedCriterion STAT_CHANGED_CRITERION = MixinCriterions.registerCriterion(new StatChangedCriterion());

	private static boolean lastReadNbtPresent = false;
	private static NbtCompound lastReadNbt;

	public static RecipeMatcher lastRecipeFinder;
	public static ServerPlayerEntity lastServerPlayerEntity;

	private static int currentStackId = 1;
	public static final Int2ObjectMap<Pair<Integer, NbtCompound>> id2StackMap = new Int2ObjectAVLTreeMap<>();
	public static final LoadingCache<Pair<Integer, NbtCompound>, Integer> stack2IdMap = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).removalListener(notification -> {
				synchronized (id2StackMap) {
					id2StackMap.remove((int) notification.getValue());
				}
			}
	).build(new CacheLoader<>() {
		@Override
		public Integer load(Pair<Integer, NbtCompound> key) {
			synchronized (id2StackMap) {
				id2StackMap.put(currentStackId, key);
			}
			return currentStackId++;
		}
	});

	public static void logInfo(String message) {
		LOGGER.info(LOG_PREFIX + message);
	}

	public static void logWarn(String message) {
		LOGGER.warn(LOG_PREFIX + message);
	}

	public static void logError(String message) {
		LOGGER.error(LOG_PREFIX + message);
	}

	@SuppressWarnings("unused")
	public static boolean hasLastReadNbt() {
		return lastReadNbtPresent;
	}

	@SuppressWarnings("unused")
	public static void clearLastReadNbt() {
		lastReadNbt = null;
		lastReadNbtPresent = false;
	}

	public static void setLastReadNbt(NbtCompound nbt) {
		lastReadNbt = nbt;
		lastReadNbtPresent = true;
	}

	public static NbtCompound useLastReadNbt() {
		NbtCompound result = null;
		if (lastReadNbt != null) {
			result = lastReadNbt.copy();
			lastReadNbt = null;
		}
		lastReadNbtPresent = false;
		return result;
	}

	@Override
	public void onInitialize() {
	}

	public static boolean hasClientMod(ServerPlayerEntity playerEntity) {
		return ServerPlayNetworking.canSend(playerEntity, UPDATE_ANVIL_TEXT_S2C_PACKET_ID);
	}

	public static <T extends Recipe<?>> RecipeType<T> registerRecipeType(String name) {
		Identifier recipeTypeId = new Identifier(MOD_ID, name);
		RecipeTypeHelper.addToSyncBlacklist(recipeTypeId);
		return Registry.register(Registry.RECIPE_TYPE, recipeTypeId, new RecipeType<T>() {
			@Override
			public String toString() {
				return MOD_ID + ":" + name;
			}
		});
	}

	public static <S extends RecipeSerializer<T>, T extends Recipe<?>> S registerRecipeSerializer(String name, S recipeSerializer) {
		return Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(MOD_ID, name), recipeSerializer);
	}
}
