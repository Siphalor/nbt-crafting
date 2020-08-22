package de.siphalor.nbtcrafting.mixin.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.JsonPreprocessor;
import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.util.duck.IItemStack;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ShapedRecipe.class)
public abstract class MixinShapedRecipe {
	@Shadow
	@Final
	private ItemStack output;

	@Shadow
	@Final
	private DefaultedList<Ingredient> inputs;

	@Inject(method = "getItemStack", at = @At("HEAD"))
	private static void handlePotions(JsonObject json, CallbackInfoReturnable<ItemStack> ci) {
		if (json.has("potion")) {
			Identifier identifier = new Identifier(JsonHelper.getString(json, "potion"));
			if (!Registry.POTION.getOrEmpty(identifier).isPresent())
				throw new JsonParseException("The given resulting potion does not exist!");
			JsonObject dataObject;
			if (!json.has("data")) {
				dataObject = new JsonObject();
				json.add("data", dataObject);
			} else
				dataObject = JsonHelper.getObject(json, "data");
			dataObject.addProperty("Potion", identifier.toString());
			json.addProperty("item", "minecraft:potion");
		}
	}

	@Inject(
			method = "getItemStack",
			at = @At(value = "INVOKE", target = "com/google/gson/JsonObject.has(Ljava/lang/String;)Z", remap = false)
	)
	private static void deserializeItemStack(JsonObject json, CallbackInfoReturnable<ItemStack> ci) {
		NbtCrafting.clearLastReadNbt();
		if (json.has("data")) {
			if (JsonHelper.hasString(json, "data")) {
				try {
					NbtCrafting.setLastReadNbt(new StringNbtReader(new StringReader(json.get("data").getAsString())).parseCompoundTag());
				} catch (CommandSyntaxException e) {
					e.printStackTrace();
				}
			} else {
				NbtCrafting.setLastReadNbt((CompoundTag) NbtUtil.asTag(JsonPreprocessor.process(JsonHelper.getObject(json, "data"))));
			}
			json.remove("data");
		}
	}

	@Inject(
			method = "getItemStack", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private static void constructDeserializedItemStack(JsonObject json, CallbackInfoReturnable<ItemStack> ci, String id, Item item, int amount) {
		ItemStack stack = new ItemStack(item, amount);
		if (NbtCrafting.hasLastReadNbt()) {
			CompoundTag lastReadNbt = NbtCrafting.useLastReadNbt();

			//noinspection ConstantConditions
			((IItemStack) (Object) stack).setRawTag(lastReadNbt);
		}
		ci.setReturnValue(stack);
	}

	@Inject(method = "craft", at = @At("HEAD"), cancellable = true)
	public void craft(CraftingInventory craftingInventory, CallbackInfoReturnable<ItemStack> callbackInfoReturnable) {
		ItemStack result = RecipeUtil.getDollarAppliedResult(output, inputs, craftingInventory);
		if (result != null) callbackInfoReturnable.setReturnValue(result);
	}
}
