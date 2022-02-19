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

package de.siphalor.nbtcrafting.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.siphalor.nbtcrafting.NbtCrafting;

@Mixin(ServerStatHandler.class)
public class MixinServerStatHandler {
	@Inject(method = "setStat", at = @At("TAIL"))
	public void setStat(PlayerEntity playerEntity, Stat<?> stat, int value, CallbackInfo callbackInfo) {
		if (playerEntity instanceof ServerPlayerEntity) {
			NbtCrafting.STAT_CHANGED_CRITERION.trigger((ServerPlayerEntity) playerEntity, stat, value);
		}
	}
}
