/*
 * Copyright 2020-2022 Siphalor
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

package de.siphalor.nbtcrafting.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.mixin.RecipeManagerAccessor;
import de.siphalor.nbtcrafting.mixin.client.AnvilScreenAccessor;

public class NbtCraftingClient implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("nbt_crafting_client");

	@Override
	public void onInitializeClient() {
		ClientLoginNetworking.registerGlobalReceiver(NbtCrafting.PRESENCE_CHANNEL, (client, handler, buf, listenerAdder) -> {
			return CompletableFuture.completedFuture(new PacketByteBuf(Unpooled.buffer()));
		});

		ClientPlayNetworking.registerGlobalReceiver(NbtCrafting.UPDATE_ANVIL_TEXT_S2C_PACKET_ID, (client, handler, buf, responseSender) -> {
			if (MinecraftClient.getInstance().currentScreen instanceof AnvilScreen) {
				((AnvilScreenAccessor) MinecraftClient.getInstance().currentScreen).getNameField().setText(buf.readString());
			} else
				buf.readString();
		});

		ClientPlayNetworking.registerGlobalReceiver(NbtCrafting.UPDATE_ADVANCED_RECIPES_PACKET_ID, NbtCraftingClient::receiveAdvancedRecipePacket);
		LOGGER.info("[NBT Crafting Client] Initialized!");
	}

	private static synchronized void receiveAdvancedRecipePacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		RecipeManager recipeManager = handler.getRecipeManager();
		Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipeMap = ((RecipeManagerAccessor) recipeManager).getRecipes();
		recipeMap = new HashMap<>(recipeMap);

		NbtCrafting.advancedIngredientSerializationEnabled.set(true);
		while (buf.isReadable()) {
			readRecipe(buf, recipeMap);
		}
		NbtCrafting.advancedIngredientSerializationEnabled.set(false);

		((RecipeManagerAccessor) recipeManager).setRecipes(ImmutableMap.copyOf(recipeMap));
	}

	private static void readRecipe(PacketByteBuf buf, Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes) {
		Identifier serializerId = buf.readIdentifier();
		RecipeSerializer<?> serializer = Registry.RECIPE_SERIALIZER.get(serializerId);
		if (serializer == null)
		{
			throw new IllegalStateException("Missing key in registry: " + serializerId);
		}

		Identifier recipeId = buf.readIdentifier();

		Recipe<?> recipe = serializer.read(recipeId, buf);
		Map<Identifier, Recipe<?>> recipeType = recipes.computeIfAbsent(recipe.getType(), rt -> new HashMap<>());
		recipeType.put(recipeId, recipe);
	}

	public static RecipeManager getClientRecipeManager() {
		return MinecraftClient.getInstance().getNetworkHandler().getRecipeManager();
	}
}
