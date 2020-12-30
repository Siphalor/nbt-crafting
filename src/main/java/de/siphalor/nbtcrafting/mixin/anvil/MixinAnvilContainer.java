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

package de.siphalor.nbtcrafting.mixin.anvil;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.recipe.AnvilRecipe;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(AnvilScreenHandler.class)
public abstract class MixinAnvilContainer extends ForgingScreenHandler {

	@Shadow
	private String newItemName;

	@Shadow
	@Final
	private Property levelCost;

	@Shadow
	private int repairItemUsage;

	@Unique
	private boolean userChangedName = false;

	public MixinAnvilContainer(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
		super(type, syncId, playerInventory, context);
	}

	@Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
	public void updateResult(CallbackInfo callbackInfo) {
		Optional<AnvilRecipe> optionalAnvilRecipe = player.world.getRecipeManager().getFirstMatch(NbtCrafting.ANVIL_RECIPE_TYPE, input, player.world);
		if (optionalAnvilRecipe.isPresent()) {
			ItemStack resultStack = optionalAnvilRecipe.get().craft(input);
			repairItemUsage = 1;
			if (userChangedName) {
				if (!newItemName.equals(resultStack.getName().getString()))
					resultStack.setCustomName(new LiteralText(newItemName));
				userChangedName = false;
			} else {
				newItemName = resultStack.getName().getString();
				if (player instanceof ServerPlayerEntity) {
					if (NbtCrafting.hasClientMod((ServerPlayerEntity) player)) {
						PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
						packetByteBuf.writeString(newItemName);
						ServerPlayNetworking.send((ServerPlayerEntity) player, NbtCrafting.UPDATE_ANVIL_TEXT_S2C_PACKET_ID, packetByteBuf);
					}
				}
			}

			output.setStack(0, resultStack);
			resultStack.onCraft(player.world, player, resultStack.getCount());

			levelCost.set(optionalAnvilRecipe.get().getLevels());
			sendContentUpdates();

			callbackInfo.cancel();
		}
	}

	@Inject(method = "setNewItemName", at = @At("HEAD"))
	public void onNewItemNameSet(String newNewItemName, CallbackInfo callbackInfo) {
		userChangedName = true;
	}
}
