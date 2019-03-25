package de.siphalor.nbtcrafting.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import net.minecraft.datafixers.NbtOps;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.cooking.CookingRecipe;
import net.minecraft.recipe.cooking.CookingRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CookingRecipeSerializer.class)
public abstract class MixinCookingRecipeSerializer {
	private CompoundTag resultTag = null;

	@Redirect(method = "method_17736", at = @At(value = "INVOKE", target = "net/minecraft/util/JsonHelper.getString(Lcom/google/gson/JsonObject;Ljava/lang/String;)Ljava/lang/String;", ordinal = 0))
	public String getItemIdentifier(JsonObject jsonObject, String resultPropertyName) {
        resultTag = null;
		if(!jsonObject.has(resultPropertyName) || !jsonObject.get(resultPropertyName).isJsonObject()) {
			return JsonHelper.getString(jsonObject, resultPropertyName);
		}
		JsonObject itemCompound = jsonObject.getAsJsonObject(resultPropertyName);
		if(itemCompound.has("data")) {
            resultTag = (CompoundTag) Dynamic.convert(JsonOps.INSTANCE, NbtOps.INSTANCE, itemCompound.get("data"));
		}
		return JsonHelper.getString(itemCompound, "item");
	}

    @Inject(method = "method_17736", at = @At(value = "TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	public void onRecipeReady(Identifier identifier, JsonObject jsonObject, CallbackInfoReturnable<CookingRecipe> callbackInfoReturnable, String group, JsonElement ingredientJson, Ingredient ingredient, String itemId, Identifier itemIdentifier, ItemStack stack, float experience, int cookingTime) {
		stack.setTag(resultTag);
    }
}
