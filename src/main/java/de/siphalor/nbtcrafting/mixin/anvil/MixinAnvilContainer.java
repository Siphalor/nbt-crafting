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

package de.siphalor.nbtcrafting.mixin.anvil;

import de.siphalor.nbtcrafting.util.IAnvilContainer;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.container.AnvilContainer;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerType;
import net.minecraft.container.Property;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.PacketByteBuf;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.recipe.AnvilRecipe;

@Mixin(AnvilContainer.class)
public abstract class MixinAnvilContainer extends Container implements IAnvilContainer {
	@Shadow
	@Final
	private PlayerEntity player;

	@Shadow
	@Final
	private Inventory inventory;

	@Shadow
	@Final
	private Inventory result;

	@Shadow
	@Final
	private Property levelCost;

	@Shadow
	private int repairItemUsage;

	@Shadow
	private String newItemName;

	@Unique
	private boolean userChangedName = false;

	@Unique
	private AnvilRecipe recipe;

	protected MixinAnvilContainer(ContainerType<?> containerType_1, int int_1) {
		super(containerType_1, int_1);
	}

	@Override
	public Property getLevelCost() {
		return levelCost;
	}

	@Override
	public AnvilRecipe nbtcrafting$getRecipe() {
		return recipe;
	}

	@Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
	public void updateResult(CallbackInfo callbackInfo) {
		recipe = player.world.getRecipeManager().getFirstMatch(NbtCrafting.ANVIL_RECIPE_TYPE, inventory, player.world).orElse(null);
		if (recipe != null) {
			ItemStack resultStack = recipe.craft(inventory);
			repairItemUsage = 1;
			if (userChangedName) {
				if (
						!StringUtils.isBlank(newItemName) &&
								!newItemName.equals(resultStack.getName().getString())
				) {
					resultStack.setCustomName(new LiteralText(newItemName));
				}
				userChangedName = false;
			} else {
				newItemName = resultStack.getName().getString();
				if (player instanceof ServerPlayerEntity) {
					if (NbtCrafting.hasClientMod((ServerPlayerEntity) player)) {
						PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
						packetByteBuf.writeString(newItemName);
						ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, NbtCrafting.UPDATE_ANVIL_TEXT_S2C_PACKET_ID, packetByteBuf);
					}
				}
			}

			result.setInvStack(0, resultStack);

			levelCost.set(recipe.getLevels());
			sendContentUpdates();

			callbackInfo.cancel();
		}
	}

	@Inject(method = "setNewItemName", at = @At("HEAD"))
	public void onNewItemNameSet(String newNewItemName, CallbackInfo callbackInfo) {
		userChangedName = true;
	}
}
