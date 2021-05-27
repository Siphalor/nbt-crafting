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

package de.siphalor.nbtcrafting.compat.mixin;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.RecipeTypeHelper;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(
		value = RegistrySyncManager.class
)
public class MixinRegistrySyncManager {
	private static boolean isRecipeTypeRegistry = false;

	@Inject(
			method = "toTag",
			at = @At(
					value = "NEW",
					target = "net/minecraft/nbt/CompoundTag",
					ordinal = 1
			),
			locals = LocalCapture.CAPTURE_FAILSOFT
	)
	private static void onRegistry(boolean isClientSync, CallbackInfoReturnable<?> callbackInfoReturnable, CompoundTag mainTag, Iterator<?> iterator, Identifier registryId, MutableRegistry<?> registry) {
		isRecipeTypeRegistry = registry == Registry.RECIPE_TYPE || registry == Registry.RECIPE_SERIALIZER;
	}

	@ModifyVariable(
			method = "toTag",
			at = @At(
					value = "INVOKE_ASSIGN",
					target = "Lnet/minecraft/util/registry/MutableRegistry;getId(Ljava/lang/Object;)Lnet/minecraft/util/Identifier;",
					ordinal = 1
			),
			ordinal = 1
	)
	private static Identifier cancelSync(Identifier oldId) {
		if (isRecipeTypeRegistry && RecipeTypeHelper.getSyncBlacklist().contains(oldId)) {
			NbtCrafting.logInfo("Block " + oldId + " from being synced");
			return null;
		}
		return oldId;
	}
}
