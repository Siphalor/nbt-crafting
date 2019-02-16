package de.siphalor.nbtcrafting.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.gson.JsonObject;

import de.siphalor.nbtcrafting.Core;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.crafting.ShapedRecipe;
import net.minecraft.util.JsonHelper;

@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeMixin {

	@Inject(
		method = "deserializeItemStack",
		at = @At(value = "INVOKE", target = "com/google/gson/JsonObject.has(Ljava/lang/String;)Z")
	)
	private static void deserializeItemStack(JsonObject json, CallbackInfoReturnable<ItemStack> ci) {
		Core.setLastReadNbt(null);
		if(json.has("data")) {
			Core.setLastReadNbt(Core.parseNbtString(JsonHelper.getString(json, Core.JSON_NBT_KEY)));
			json.remove("data");
		}
	}
	
	@Inject(
		method = "deserializeItemStack", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
	private static void constructDeserializedItemStack(JsonObject json, CallbackInfoReturnable<ItemStack> ci, String id, Item item, int amount) {
		ItemStack stack = new ItemStack(item, amount);
		stack.setTag(Core.useLastReadNbt());
		ci.setReturnValue(stack);
	}
}
