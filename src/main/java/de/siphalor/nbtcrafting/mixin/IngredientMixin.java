package de.siphalor.nbtcrafting.mixin;

import com.google.common.base.Predicates;
import com.google.gson.*;
import de.siphalor.nbtcrafting.Core;
import de.siphalor.nbtcrafting.ingredient.*;
import de.siphalor.nbtcrafting.util.ICloneable;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.crafting.ShapedRecipe;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings("ALL")
@Mixin(Ingredient.class)
public abstract class IngredientMixin implements IIngredient, ICloneable {
	
	private IngredientEntry[] advancedEntries;
	
	@Shadow
	private ItemStack[] stackArray;
	@Shadow
	private IntList ids;

	@Shadow
	private void createStackArray() {};
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	@Inject(method = "<init>", at = @At("RETURN"))
	private void onConstruct(@SuppressWarnings("rawtypes") Stream stream, CallbackInfo ci) {
		advancedEntries = null;
	}
	
	@Inject(method = "createStackArray", at = @At("HEAD"), cancellable = true)
	private void createStackArray(CallbackInfo callbackInfo) {
		if(advancedEntries != null) {
			callbackInfo.cancel();
			if (stackArray != null)
				return;
			stackArray = Arrays.stream(advancedEntries).flatMap(entry -> entry.getPreviewStacks().stream()).distinct().toArray(ItemStack[]::new);
			return;
		}
	}

    @Inject(method = "method_8093", at = @At("HEAD"), cancellable = true)
	public void matches(ItemStack stack, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if(advancedEntries != null) {
			if (advancedEntries.length == 0) {
                callbackInfoReturnable.setReturnValue(stack.isEmpty());
                return;
			}
			for (int i = 0; i < advancedEntries.length; i++) {
				if (advancedEntries[i].matches(stack)) {
					callbackInfoReturnable.setReturnValue(true);
                    return;
				}
			}
            callbackInfoReturnable.setReturnValue(false);
		}
	}
	
	@Inject(method = "write", at = @At("HEAD"), cancellable = true)
	public void write(PacketByteBuf buf, CallbackInfo callbackInfo) {
		if(!Core.vanillaCompatibility) {
			buf.writeBoolean(advancedEntries != null);
			if (advancedEntries != null) {
				buf.writeVarInt(advancedEntries.length);
				for (int i = 0; i < advancedEntries.length; i++) {
					IngredientEntry entry = advancedEntries[i];
					buf.writeBoolean(entry instanceof IngredientMultiStackEntry);
					entry.write(buf);
				}
				callbackInfo.cancel();
			}
		}
	}
	
	@Inject(method = "toJson", at = @At("HEAD"), cancellable = true)
	public void toJson(CallbackInfoReturnable<JsonElement> callbackInfoReturnable) {
		if(advancedEntries != null) {
			if (advancedEntries.length == 1) {
				callbackInfoReturnable.setReturnValue(advancedEntries[0].toJson());
				return;
			}
			JsonArray array = new JsonArray();
			for (int i = 0; i < advancedEntries.length; i++) {
				array.add(advancedEntries[i].toJson());
			}
            callbackInfoReturnable.setReturnValue(array);
		}
	}
	
	@Inject(method = "isEmpty", at = @At("HEAD"), cancellable = true)
	public void isEmpty(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if(advancedEntries != null) {
			callbackInfoReturnable.setReturnValue(advancedEntries.length == 0);
		}
	}
	
	private static Ingredient ofAdvancedEntries(Stream<? extends IngredientEntry> entries) {
		if(entries == null)
			System.out.println("ERROR");
		try {
			Ingredient ingredient;
			ingredient = (Ingredient)((ICloneable)(Object)Ingredient.EMPTY).clone();
			((IIngredient)(Object)ingredient).setAdvancedEntries(entries);
			return ingredient;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return Ingredient.EMPTY;
	}
	
	/*@Overwrite
	public static Ingredient ofStacks(ItemStack... arr) {
		return ofAdvancedEntries(Arrays.stream(arr).map(stack -> new IngredientStackEntry(stack)));
	}*/

	@Inject(method = "fromPacket", at = @At("HEAD"), cancellable = true)
	private static void fromPacket(PacketByteBuf buf, CallbackInfoReturnable<Ingredient> callbackInfoReturnable) {
		if(buf.readBoolean()) {
			ArrayList<IngredientEntry> entries = new ArrayList<IngredientEntry>();
			int length = buf.readVarInt();
			for (int i = 0; i < length; i++) {
				if (buf.readBoolean())
					entries.add(IngredientMultiStackEntry.read(buf));
				else
					entries.add(IngredientStackEntry.read(buf));
			}
			callbackInfoReturnable.setReturnValue(ofAdvancedEntries(entries.stream()));
		}
	}

	@Inject(method = "fromJson", at = @At("HEAD"), cancellable = true)
	private static void fromJson(JsonElement element, CallbackInfoReturnable<Ingredient> callbackInfoReturnable) {
        if(element == null || element.isJsonNull()) {
            throw new JsonSyntaxException("Item cannot be null");
        }
        if(element.isJsonObject()) {
        	if(element.getAsJsonObject().has("data") || element.getAsJsonObject().has("remainder"))
				callbackInfoReturnable.setReturnValue(ofAdvancedEntries(Stream.of(advancedEntryFromJson(element.getAsJsonObject()))));
        } else if(element.isJsonArray()) {
	        final JsonArray jsonArray = element.getAsJsonArray();
	        if (jsonArray.size() == 0) {
		        throw new JsonSyntaxException("Item array cannot be empty, at least one item must be defined");
	        }
	        callbackInfoReturnable.setReturnValue(ofAdvancedEntries(StreamSupport.<JsonElement>stream(jsonArray.spliterator(), false).map(e-> advancedEntryFromJson(JsonHelper.asObject(e, "item")))));
        }
    }

	private static IngredientEntry advancedEntryFromJson(JsonObject jsonObject) {
		if (jsonObject.has("item") && jsonObject.has("tag")) {
            throw new JsonParseException("An ingredient entry is either a tag or an item, not both");
        }
        if (jsonObject.has("item")) {
            final Identifier identifier = new Identifier(JsonHelper.getString(jsonObject, "item"));
            try {
				final Item item = Registry.ITEM.getOrEmpty(identifier).orElseThrow(() -> {
					throw new JsonSyntaxException("Unknown item '" + identifier.toString() + "'");
				});
				IngredientStackEntry entry = new IngredientStackEntry(Registry.ITEM.getRawId(item), loadIngredientEntryCondition(jsonObject));
				if(jsonObject.has("remainder")) {
					entry.setRecipeRemainder(ShapedRecipe.getItemStack(JsonHelper.getObject(jsonObject, "remainder")));
				}
				return entry;
            } catch (Throwable e) {
            	e.printStackTrace();
            	return null;
            }
        }
        if (!jsonObject.has("tag")) {
            throw new JsonParseException("An ingredient entry needs either a tag or an item");
        }
        final Identifier identifier2 = new Identifier(JsonHelper.getString(jsonObject, "tag"));
        final Tag<Item> tag = ItemTags.getContainer().get(identifier2);
        if (tag == null) {
            throw new JsonSyntaxException("Unknown item tag '" + identifier2 + "'");
        }
        IngredientMultiStackEntry entry = new IngredientMultiStackEntry(tag.values().stream().map(item -> Registry.ITEM.getRawId(item)).collect(Collectors.toList()), loadIngredientEntryCondition(jsonObject));
        entry.setTag(tag.toString());
        if(jsonObject.has("remainder")) {
        	entry.setRecipeRemainder(ShapedRecipe.getItemStack(JsonHelper.getObject(jsonObject, "remainder")));
        }
        return entry;
	}

	private static IngredientEntryCondition loadIngredientEntryCondition(JsonObject jsonObject) {
		if(jsonObject.has("data")) {
			if(jsonObject.get("data").isJsonPrimitive() && jsonObject.getAsJsonPrimitive("data").isString()) {
				throw new JsonParseException("The data tag on recipes cannot be a string anymore; See the wiki for more information ;)");
			}
			if(jsonObject.get("data").isJsonObject()) {
				return IngredientEntryCondition.fromJson(jsonObject.get("data").getAsJsonObject());
			}
		}
		return new IngredientEntryCondition();
	}

	@Override
	public void setAdvancedEntries(Stream<? extends IngredientEntry> entries) {
		advancedEntries = entries.filter(Predicates.notNull()).toArray(IngredientEntry[]::new);
	}

	@Override
	public ItemStack getRecipeRemainder(ItemStack stack, HashMap<String, CompoundTag> reference) {
		if(advancedEntries != null) {
			for(IngredientEntry entry : advancedEntries) {
				if(entry.matches(stack)) {
					ItemStack remainder = entry.getRecipeRemainder(stack, reference);
					if(remainder != null) {
						return remainder;
					}
				}
			}
		}
		return null;
	}
}
