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

package de.siphalor.nbtcrafting.mixin.network;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.ServerRecipe;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(SynchronizeRecipesS2CPacket.class)
public abstract class MixinSynchronizeRecipesS2CPacket {
	@Shadow
	private List<Recipe<?>> recipes;

	@Inject(method = "write", at = @At("HEAD"), cancellable = true)
	public void onWrite(PacketByteBuf buf, CallbackInfo callbackInfo) {
		if (NbtCrafting.hasClientMod(NbtCrafting.lastServerPlayerEntity)) {
			List<Recipe<?>> syncRecipes = recipes.stream().filter(recipe -> !(recipe instanceof ServerRecipe)).collect(Collectors.toList());
			buf.writeVarInt(syncRecipes.size());
			for (Recipe<?> recipe : syncRecipes) {
				SynchronizeRecipesS2CPacket.writeRecipe(buf, recipe);
			}
			callbackInfo.cancel();
		}
	}
}
