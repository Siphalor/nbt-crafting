package de.siphalor.nbtcrafting.compat.mixin;

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
			return null;
		}
		return oldId;
	}
}
