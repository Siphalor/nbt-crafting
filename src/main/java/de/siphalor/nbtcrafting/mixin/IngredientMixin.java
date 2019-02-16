package de.siphalor.nbtcrafting.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.gson.JsonObject;
import de.siphalor.nbtcrafting.Core;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.JsonHelper;

@Mixin(Ingredient.class)
public abstract class IngredientMixin {
	
	@Inject(method = "entryFromJson", at = @At("HEAD"))
	private static void stackFromJsonMixin(JsonObject json, CallbackInfoReturnable<Object> ci) {
		Core.setLastReadNbt(null);
		if(JsonHelper.hasString(json, Core.JSON_NBT_KEY)) {
			Core.setLastReadNbt(Core.parseNbtString(JsonHelper.getString(json, Core.JSON_NBT_KEY)));
		}
	}
	
	@Inject(method = "matches", at = @At(value = "RETURN", ordinal = 2), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
	public void matches(ItemStack stackReference, CallbackInfoReturnable<Boolean> ci, ItemStack stackArray[], int int_1, int int_2, ItemStack currentStack) {
		if(currentStack.hasTag() && stackReference.hasTag()) {
			for(String key : currentStack.getTag().getKeys()) {
				if(!stackReference.getTag().containsKey(key))
					ci.setReturnValue(false);
				if(!currentStack.getTag().getTag(key).equals(stackReference.getTag().getTag(key)))
					ci.setReturnValue(false);
			}
		}
	}
}
