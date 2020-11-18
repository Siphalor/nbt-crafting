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

package de.siphalor.nbtcrafting.mixin;

import de.siphalor.nbtcrafting.NbtCrafting;
import net.minecraft.container.CraftingResultSlot;
import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CraftingResultSlot.class)
public abstract class MixinCraftingResultSlot extends Slot {
	public MixinCraftingResultSlot(Inventory inventory_1, int int_1, int int_2, int int_3) {
		super(inventory_1, int_1, int_2, int_3);
	}

	@Inject(method = "onTakeItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/CraftingInventory;setInvStack(ILnet/minecraft/item/ItemStack;)V", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void onTakeItem(PlayerEntity playerEntity, ItemStack stack, CallbackInfoReturnable<ItemStack> cir, DefaultedList<?> defaultedList, int index, ItemStack old, ItemStack remainder) {
		if (playerEntity instanceof ServerPlayerEntity && !NbtCrafting.hasClientMod((ServerPlayerEntity) playerEntity)) {
			((ServerPlayerEntity) playerEntity).onContainerSlotUpdate(playerEntity.container, index + 1, remainder);
		}
	}
}
