package de.siphalor.nbtcrafting.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import de.siphalor.nbtcrafting.Core;
import net.minecraft.datafixers.NbtOps;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ShapedRecipe.class)
public abstract class MixinShapedRecipe {

	@Inject(
		method = "getItemStack",
		at = @At(value = "INVOKE", target = "com/google/gson/JsonObject.has(Ljava/lang/String;)Z", remap = false)
	)
	private static void deserializeItemStack(JsonObject json, CallbackInfoReturnable<ItemStack> ci) {
		Core.setLastReadNbt(null);
		if(json.has("data")) {
			if(!json.get("data").isJsonObject())
				throw new JsonParseException("The recipe's output data tag must be a JSON object.");
			Core.setLastReadNbt((CompoundTag) Dynamic.convert(JsonOps.INSTANCE, NbtOps.INSTANCE, json.getAsJsonObject("data")));
			json.remove("data");
		}
	}
	
	@Inject(
		method = "getItemStack", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private static void constructDeserializedItemStack(JsonObject json, CallbackInfoReturnable<ItemStack> ci, String id, Item item, int amount) {
		ItemStack stack = new ItemStack(item, amount);
		stack.setTag(Core.useLastReadNbt());
		ci.setReturnValue(stack);
	}
}
