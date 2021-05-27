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

package de.siphalor.nbtcrafting.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.InputSlotFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InputSlotFiller.class)
public abstract class MixinInputSlotFiller {
	@Shadow
	protected PlayerInventory inventory;

	@Redirect(method = "fillInputSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;indexOf(Lnet/minecraft/item/ItemStack;)I"))
	private int playerInventoryFindStack(PlayerInventory inventory, ItemStack stack) {
		for (int i = 0; i < inventory.main.size(); i++) {
			ItemStack stack2 = inventory.main.get(i);
			if (stack.getItem() == stack2.getItem() && ItemStack.areTagsEqual(stack, stack2))
				return i;
		}
		if (!stack.hasTag()) {
			for (int i = 0; i < inventory.main.size(); i++) {
				ItemStack stack2 = inventory.main.get(i);
				if (stack2.hasTag() && stack.isItemEqualIgnoreDamage(stack2))
					return i;
			}
		}
		return -1;
	}
}
