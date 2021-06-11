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

import de.siphalor.nbtcrafting.advancement.StatChangedCriterion;
import de.siphalor.nbtcrafting.api.RecipeTypeHelper;
import de.siphalor.nbtcrafting.mixin.advancement.MixinCriterions;
import de.siphalor.nbtcrafting.recipe.AnvilRecipe;
import de.siphalor.nbtcrafting.recipe.BrewingRecipe;
import de.siphalor.nbtcrafting.recipe.cauldron.CauldronRecipe;
import de.siphalor.nbtcrafting.recipe.cauldron.CauldronRecipeSerializer;
import de.siphalor.nbtcrafting.util.duck.IServerPlayerEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.client.network.packet.SynchronizeRecipesS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NbtCrafting implements ModInitializer {
	public static final String MOD_ID = "nbtcrafting";
	public static final String MOD_NAME = "Nbt Crafting";

	private static final String LOG_PREFIX = "[" + MOD_NAME + "] ";
	private static final Logger LOGGER = LogManager.getLogger();

	public static final Identifier PRESENCE_PACKET_ID = new Identifier(MOD_ID, "present");
	public static final Identifier UPDATE_ANVIL_TEXT_S2C_PACKET_ID = new Identifier(MOD_ID, "update_anvil_text");

	public static final RecipeType<AnvilRecipe> ANVIL_RECIPE_TYPE = registerRecipeType("anvil");
	public static final RecipeSerializer<AnvilRecipe> ANVIL_RECIPE_SERIALIZER = registerRecipeSerializer("anvil", AnvilRecipe.SERIALIZER);

	public static final RecipeType<BrewingRecipe> BREWING_RECIPE_TYPE = registerRecipeType("brewing");
	public static final RecipeSerializer<BrewingRecipe> BREWING_RECIPE_SERIALIZER = registerRecipeSerializer("brewing", BrewingRecipe.SERIALIZER);

	public static final RecipeType<CauldronRecipe> CAULDRON_RECIPE_TYPE = registerRecipeType("cauldron");
	public static final CauldronRecipeSerializer CAULDRON_RECIPE_SERIALIZER = registerRecipeSerializer("cauldron", new CauldronRecipeSerializer());

	public static final StatChangedCriterion STAT_CHANGED_CRITERION = MixinCriterions.registerCriterion(new StatChangedCriterion());

	private static boolean lastReadNbtPresent = false;
	private static CompoundTag lastReadNbt;

	public static RecipeFinder lastRecipeFinder;
	public static ServerPlayerEntity lastServerPlayerEntity;

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

	public static void setLastReadNbt(CompoundTag nbt) {
		lastReadNbt = nbt;
		lastReadNbtPresent = true;
	}

	public static CompoundTag useLastReadNbt() {
		CompoundTag result = null;
		if (lastReadNbt != null) {
			result = lastReadNbt.copy();
			lastReadNbt = null;
		}
		lastReadNbtPresent = false;
		return result;
	}

	@Override
	public void onInitialize() {
		ServerSidePacketRegistry.INSTANCE.register(PRESENCE_PACKET_ID, (packetContext, packetByteBuf) ->
			packetContext.getTaskQueue().execute(() -> {
				ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) packetContext.getPlayer();
				((IServerPlayerEntity) serverPlayerEntity).nbtCrafting$setClientModPresent(true);
				serverPlayerEntity.networkHandler.sendPacket(new SynchronizeRecipesS2CPacket(serverPlayerEntity.server.getRecipeManager().values()));
				serverPlayerEntity.getRecipeBook().sendInitRecipesPacket(serverPlayerEntity);
			})
		);
	}

	public static boolean hasClientMod(ServerPlayerEntity playerEntity) {
		if (!(playerEntity instanceof IServerPlayerEntity))
			return false;
		return ((IServerPlayerEntity) playerEntity).nbtCrafting$hasClientMod();
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
