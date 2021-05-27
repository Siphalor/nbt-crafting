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

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.mixin.client.AnvilScreenAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeManager;

@Environment(EnvType.CLIENT)
public class NbtCraftingClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(NbtCrafting.UPDATE_ANVIL_TEXT_S2C_PACKET_ID, (client, handler, buf, responseSender) -> {
			if (MinecraftClient.getInstance().currentScreen instanceof AnvilScreen) {
				((AnvilScreenAccessor) MinecraftClient.getInstance().currentScreen).getNameField().setText(buf.readString());
			}
		});
	}

	public static RecipeManager getClientRecipeManager() {
		return MinecraftClient.getInstance().getNetworkHandler().getRecipeManager();
	}
}
