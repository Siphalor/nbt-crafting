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

package de.siphalor.nbtcrafting3.mixin.cutting;

import java.util.List;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.screen.StonecutterScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.siphalor.nbtcrafting3.api.nbt.NbtUtil;

@Mixin(StonecutterScreenHandler.class)
public class MixinStonecutterContainer {
	@Shadow
	private List<StonecuttingRecipe> availableRecipes;

	@Inject(method = "updateInput", at = @At("TAIL"))
	private void onInputUpdated(Inventory inventory, ItemStack input, CallbackInfo callbackInfo) {
		availableRecipes.sort((a, b) -> {
			ItemStack s1 = a.getOutput();
			ItemStack s2 = b.getOutput();
			int comp = s1.getTranslationKey().compareTo(s2.getTranslationKey());
			if (comp != 0)
				return comp;
			return NbtUtil.getTagOrEmpty(s1).toString().compareTo(NbtUtil.getTagOrEmpty(s2).toString());
		});
	}
}
