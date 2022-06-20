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

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.recipe.AnvilRecipe;

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

	@Unique
	private ItemStack originalBaseStack;

	@Unique
	private AnvilRecipe recipe = null;

	public MixinAnvilContainer(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
		super(type, syncId, playerInventory, context);
	}

	@Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
	public void updateResult(CallbackInfo callbackInfo) {
		recipe = player.world.getRecipeManager().getFirstMatch(NbtCrafting.ANVIL_RECIPE_TYPE, input, player.world).orElse(null);
		if (recipe != null) {
			ItemStack resultStack = recipe.craft(input);
			repairItemUsage = 1;
			if (userChangedName) {
				if (
						!StringUtils.isBlank(newItemName) &&
								!newItemName.equals(resultStack.getName().getString())
				) {
					resultStack.setCustomName(Text.literal(newItemName));
				}
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

			levelCost.set(recipe.getLevels());
			sendContentUpdates();

			callbackInfo.cancel();
		}
	}

	@Inject(method = "setNewItemName", at = @At("HEAD"))
	public void onNewItemNameSet(String newNewItemName, CallbackInfo callbackInfo) {
		userChangedName = true;
	}

	@Inject(
			method = "canTakeOutput",
			at = @At("HEAD"),
			cancellable = true
	)
	public void canTakeItemsTop(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> cir) {
		if (levelCost.get() <= 0) {
			ItemStack base = getSlot(0).getStack();
			if (!ItemStack.areItemsEqual(getSlot(2).getStack(), base) || !ItemStack.areNbtEqual(getSlot(2).getStack(), base)) {
				cir.setReturnValue(true);
			}
		}
	}

	@Inject(
			method = "onTakeOutput",
			at = @At("HEAD")
	)
	public void onTakeItemTop(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
		originalBaseStack = getSlot(0).getStack();
	}

	@Inject(
			method = "onTakeOutput",
			at = @At("RETURN")
	)
	public void onItemTaken(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
		if (recipe != null && originalBaseStack != null) {
			if (!recipe.getBase().isEmpty()) {
				originalBaseStack.decrement(1);
				getSlot(0).setStack(originalBaseStack);
				stack.onCraft(player.world, player, stack.getCount());
			}
		}
		if (player instanceof ServerPlayerEntity) {
			if (!NbtCrafting.hasClientMod((ServerPlayerEntity) player)) {
				((ServerPlayerEntity) player).networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(
						-1, nextRevision(), 0, stack
				));
			}
		}
	}
}
