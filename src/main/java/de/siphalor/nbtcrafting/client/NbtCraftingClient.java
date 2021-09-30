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

package de.siphalor.nbtcrafting.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.mixin.RecipeManagerAccessor;
import de.siphalor.nbtcrafting.mixin.client.AnvilScreenAccessor;

public class NbtCraftingClient implements ClientModInitializer {
	public static boolean sentModPresent = false;

	public static void sendModPresent() {
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		ClientSidePacketRegistry.INSTANCE.sendToServer(NbtCrafting.PRESENCE_CHANNEL, buffer);
		sentModPresent = true;
	}

	@Override
	public void onInitializeClient() {
		ClientLoginNetworking.registerGlobalReceiver(NbtCrafting.PRESENCE_CHANNEL, (client, handler, buf, listenerAdder) -> {
			return CompletableFuture.completedFuture(new PacketByteBuf(Unpooled.buffer()));
		});

		ClientSidePacketRegistry.INSTANCE.register(NbtCrafting.UPDATE_ANVIL_TEXT_S2C_PACKET_ID, (packetContext, packetByteBuf) -> {
			if (MinecraftClient.getInstance().currentScreen instanceof AnvilScreen) {
				((AnvilScreenAccessor) MinecraftClient.getInstance().currentScreen).getNameField().setText(packetByteBuf.readString());
			} else
				packetByteBuf.readString();
		});

		ClientPlayNetworking.registerGlobalReceiver(NbtCrafting.UPDATE_ADVANCED_RECIPES_PACKET_ID, (client, handler, buf, responseSender) -> {
			RecipeManager recipeManager = handler.getRecipeManager();
			Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipeMap = ((RecipeManagerAccessor) recipeManager).getRecipes();
			recipeMap = new HashMap<>(recipeMap);

			int recipeCount = buf.readVarInt();
			NbtCrafting.advancedIngredientSerializationEnabled.set(true);
			for (int i = 0; i < recipeCount; i++) {
				RecipeSerializer<?> serializer = Registry.RECIPE_SERIALIZER.get(buf.readIdentifier());
				Identifier id = buf.readIdentifier();

				Recipe<?> recipe = serializer.read(id, buf);
				Map<Identifier, Recipe<?>> recipeType = recipeMap.computeIfAbsent(recipe.getType(), rt -> new HashMap<>());
				recipeType.put(id, recipe);
			}
			NbtCrafting.advancedIngredientSerializationEnabled.set(false);

			((RecipeManagerAccessor) recipeManager).setRecipes(ImmutableMap.copyOf(recipeMap));
		});
	}

	public static RecipeManager getClientRecipeManager() {
		return MinecraftClient.getInstance().getNetworkHandler().getRecipeManager();
	}
}
