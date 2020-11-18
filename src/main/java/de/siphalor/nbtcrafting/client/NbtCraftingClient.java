/*
 * Copyright 2020 Siphalor
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
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.network.PacketByteBuf;

@Environment(EnvType.CLIENT)
public class NbtCraftingClient implements ClientModInitializer {
	public static boolean sentModPresent = false;

	public static void sendModPresent() {
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		ClientSidePacketRegistry.INSTANCE.sendToServer(NbtCrafting.PRESENCE_PACKET_ID, buffer);
		sentModPresent = true;
	}

	@Override
	public void onInitializeClient() {
		ClientSidePacketRegistry.INSTANCE.register(NbtCrafting.UPDATE_ANVIL_TEXT_S2C_PACKET_ID, (packetContext, packetByteBuf) -> {
			if (MinecraftClient.getInstance().currentScreen instanceof AnvilScreen) {
				((AnvilScreenAccessor) MinecraftClient.getInstance().currentScreen).getNameField().setText(packetByteBuf.readString());
			} else
				packetByteBuf.readString();
		});
	}
}
