package de.siphalor.nbtcrafting.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.gson.JsonObject;

import de.siphalor.nbtcrafting.Core;
import net.minecraft.item.ItemStack;

@Mixin(targets = {"net.minecraft.recipe.Ingredient$StackEntry"})
public abstract class StackEntryMixin {
	
	@Shadow
	private ItemStack stack;

	@Inject(method = "<init>(Lnet/minecraft/item/ItemStack;)V", at = @At("RETURN"))
	public void construct(ItemStack stack, CallbackInfo ci) {
		if(Core.hasLastReadNbt())
			stack.setTag(Core.useLastReadNbt());
	}
	
	@Inject(method = "toJson()Lcom/google/gson/JsonObject;", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
	public void toJson(CallbackInfoReturnable<JsonObject> ci, JsonObject result) {
		result.addProperty(Core.JSON_NBT_KEY, stack.getTag().asString());
	}
}
