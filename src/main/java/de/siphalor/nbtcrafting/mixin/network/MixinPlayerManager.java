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

package de.siphalor.nbtcrafting.mixin.network;

import de.siphalor.nbtcrafting.NbtCrafting;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {
	@Shadow
	@Final
	private MinecraftServer server;

	@Shadow
	@Final
	private List<ServerPlayerEntity> players;

	@Inject(
			method = "onPlayerConnect",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/packet/s2c/play/CustomPayloadS2CPacket;<init>(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)V"
			)
	)
	public void beforeRegistrySync(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
		NbtCrafting.lastServerPlayerEntity.set(player);
	}

	@Inject(
			method = "onPlayerConnect",
			at = @At(
					value = "RETURN"
			)
	)
	public void afterRecipeSync(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
		if (NbtCrafting.hasClientMod(player)) {
			NbtCrafting.logInfo("Syncing advanced recipe data to player " + player.getEntityName());
			List<PacketByteBuf> packets = NbtCrafting.createAdvancedRecipeSyncPackets(server.getRecipeManager());
			for (PacketByteBuf packet : packets) {
				ServerPlayNetworking.send(player, NbtCrafting.UPDATE_ADVANCED_RECIPES_PACKET_ID, packet);
			}
		} else {
			NbtCrafting.logInfo("Skipping advanced recipe data synchronization for vanillish player " + player.getEntityName());
		}
	}

	@Inject(
			method = "onDataPacksReloaded",
			at = @At("RETURN")
	)
	public void onDataPacksReloaded(CallbackInfo ci) {
		List<ServerPlayerEntity> nbtcPlayers = players.stream().filter(NbtCrafting::hasClientMod).collect(Collectors.toList());
		if (!nbtcPlayers.isEmpty()) {
			NbtCrafting.logInfo("Syncing advanced recipe data to " + nbtcPlayers.size() + " players");
			List<PacketByteBuf> packets = NbtCrafting.createAdvancedRecipeSyncPackets(server.getRecipeManager());
			for (PacketByteBuf packet : packets) {
				for (ServerPlayerEntity player : nbtcPlayers) {
					ServerPlayNetworking.send(player, NbtCrafting.UPDATE_ADVANCED_RECIPES_PACKET_ID, packet);
				}
			}
		} else {
			NbtCrafting.logInfo("No advanced recipe data needs to be synced!");
		}
	}
}
