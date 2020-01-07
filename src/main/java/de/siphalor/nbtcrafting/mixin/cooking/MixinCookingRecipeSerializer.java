package de.siphalor.nbtcrafting.mixin.cooking;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import de.siphalor.nbtcrafting.util.duck.IItemStack;
import net.minecraft.datafixers.NbtOps;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.CookingRecipeSerializer;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CookingRecipeSerializer.class)
public abstract class MixinCookingRecipeSerializer {
	@Unique
	private CompoundTag resultTag = null;

	@Redirect(method = "read(Lnet/minecraft/util/Identifier;Lcom/google/gson/JsonObject;)Lnet/minecraft/recipe/AbstractCookingRecipe;", at = @At(value = "INVOKE", target = "net/minecraft/util/JsonHelper.getString(Lcom/google/gson/JsonObject;Ljava/lang/String;)Ljava/lang/String;", ordinal = 0))
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

    @Inject(method = "read(Lnet/minecraft/util/Identifier;Lcom/google/gson/JsonObject;)Lnet/minecraft/recipe/AbstractCookingRecipe;", at = @At(value = "TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	public void onRecipeReady(Identifier identifier, JsonObject jsonObject, CallbackInfoReturnable<AbstractCookingRecipe> callbackInfoReturnable, String group, JsonElement ingredientJson, Ingredient ingredient, String itemId, Identifier itemIdentifier, ItemStack stack, float experience, int cookingTime) {
	    //noinspection ConstantConditions
	    ((IItemStack)(Object) stack).setRawTag(resultTag);
    }
}
