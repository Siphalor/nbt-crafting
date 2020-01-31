package de.siphalor.nbtcrafting.mixin.cutting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CuttingRecipe.Serializer.class)
public class MixinCuttingRecipeSerializer {
	private static ItemStack nbtCrafting_resultStack;

	@Redirect(
			method = "read(Lnet/minecraft/util/Identifier;Lcom/google/gson/JsonObject;)Lnet/minecraft/recipe/CuttingRecipe;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/util/JsonHelper;getString(Lcom/google/gson/JsonObject;Ljava/lang/String;)Ljava/lang/String;")
	)
	public String getResultId(JsonObject jsonObject, String key) {
		nbtCrafting_resultStack = null;
		if (jsonObject.has(key)) {
			JsonElement jsonElement = jsonObject.get(key);
			if (jsonElement instanceof JsonObject) {
				nbtCrafting_resultStack = ShapedRecipe.getItemStack((JsonObject) jsonElement);
				return Registry.ITEM.getId(nbtCrafting_resultStack.getItem()).toString();
			}
		}
		return JsonHelper.getString(jsonObject, key);
	}

	@Redirect(
			method = "read(Lnet/minecraft/util/Identifier;Lcom/google/gson/JsonObject;)Lnet/minecraft/recipe/CuttingRecipe;",
			at = @At(
					value = "NEW",
					target = "net/minecraft/item/ItemStack"
			)
	)
	public ItemStack createStack(ItemConvertible itemConvertible, int count) {
		if (nbtCrafting_resultStack == null)
			return new ItemStack(itemConvertible, count);
		return nbtCrafting_resultStack;
	}
}
