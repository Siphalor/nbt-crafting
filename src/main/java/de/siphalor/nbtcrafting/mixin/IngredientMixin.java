package de.siphalor.nbtcrafting.mixin;

import com.google.common.base.Predicates;
import com.google.gson.*;
import de.siphalor.nbtcrafting.ingredient.*;
import de.siphalor.nbtcrafting.util.ICloneable;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.crafting.ShapedRecipe;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings("ALL")
@Mixin(Ingredient.class)
public abstract class IngredientMixin implements IIngredient, ICloneable {
	
	private IngredientEntry[] realEntries;
	
	@Shadow
	private ItemStack[] stackArray;
	@Shadow
	private IntList ids;
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	@Inject(method = "<init>", at = @At("RETURN"))
	private void onConstruct(@SuppressWarnings("rawtypes") Stream stream, CallbackInfo ci) {
		realEntries = new IngredientEntry[0];
	}
	
	@Overwrite
	private void createStackArray() {
		if(stackArray != null)
			return;
		stackArray = Arrays.stream(realEntries).flatMap(entry -> entry.getPreviewStacks().stream()).distinct().toArray(ItemStack[]::new);
	}
	
	@Overwrite
	public boolean matches(ItemStack stack) {
		if(realEntries.length == 0) {
			return stack.isEmpty();
		}
		for (int i = 0; i < realEntries.length; i++) {
			if(realEntries[i].matches(stack))
				return true;
		}
		return false;
	}
	
	@Overwrite
	public IntList getIds() {
		if(ids == null) {
			createStackArray();
			ids = new IntArrayList(Arrays.stream(stackArray).map(stack -> RecipeFinder.getItemId(stack)).iterator());
		}
		return ids;
	}
	
	@Overwrite
	public void write(PacketByteBuf buf) {
		buf.writeVarInt(realEntries.length);
		for (int i = 0; i < realEntries.length; i++) {
			IngredientEntry entry = realEntries[i];
			buf.writeBoolean(entry instanceof IngredientMultiStackEntry);
			entry.write(buf);
		}
	}
	
	@Overwrite
	public JsonElement toJson() {
		if(realEntries.length == 1) {
			return realEntries[0].toJson();
		}
		JsonArray array = new JsonArray();
		for (int i = 0; i < realEntries.length; i++) {
			array.add(realEntries[i].toJson());
		}
		return array;
	}
	
	@Overwrite
	public boolean isEmpty() {
		return realEntries.length == 0;
	}
	
	private static Ingredient ofRealEntries(Stream<? extends IngredientEntry> entries) {
		if(entries == null)
			System.out.println("ERROR");
		try {
			Ingredient ingredient;
			ingredient = (Ingredient)((ICloneable)(Object)Ingredient.EMPTY).clone();
			((IIngredient)(Object)ingredient).setRealEntries(entries);
			return ingredient;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return Ingredient.EMPTY;
	}
	
	@Overwrite
	public static Ingredient ofItems(ItemProvider... arr) {
		return ofRealEntries(Stream.of(new IngredientMultiStackEntry(Arrays.stream(arr).map(item -> Registry.ITEM.getRawId(item.getItem())).collect(Collectors.toList()), new IngredientEntryCondition())));
	}
	
	@Overwrite
	public static Ingredient ofStacks(ItemStack... arr) {
		return ofRealEntries(Arrays.stream(arr).map(stack -> new IngredientStackEntry(stack)));
	}
	
	@Overwrite
	public static Ingredient fromTag(Tag<Item> tag) {
		return ofRealEntries(Stream.of(new IngredientMultiStackEntry(tag.values().stream().map(item -> Registry.ITEM.getRawId(item)).collect(Collectors.toList()), new IngredientEntryCondition())));
	}
	
	@Overwrite
	public static Ingredient fromPacket(PacketByteBuf buf) {
		ArrayList<IngredientEntry> entries = new ArrayList<IngredientEntry>();
		int length = buf.readVarInt();
		for(int i = 0; i < length; i++) {
			if(buf.readBoolean())
				entries.add(IngredientMultiStackEntry.read(buf));
			else
				entries.add(IngredientStackEntry.read(buf));
		}
		return ofRealEntries(entries.stream());
	}
	
	// Imported from Mojang
	@Overwrite
	public static Ingredient fromJson(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            throw new JsonSyntaxException("Item cannot be null");
        }
        if (element.isJsonObject()) {
            return ofRealEntries(Stream.of(realEntryFromJson(element.getAsJsonObject())));
        }
        if (!element.isJsonArray()) {
            throw new JsonSyntaxException("Expected item to be object or array of objects");
        }
        final JsonArray jsonArray = element.getAsJsonArray();
        if (jsonArray.size() == 0) {
            throw new JsonSyntaxException("Item array cannot be empty, at least one item must be defined");
        }
        return ofRealEntries(StreamSupport.<JsonElement>stream(jsonArray.spliterator(), false).map(e->realEntryFromJson(JsonHelper.asObject(e, "item"))));
    }
	
	// Imported from Mojang
	private static IngredientEntry realEntryFromJson(JsonObject jsonObject) {
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
					entry.setRecipeRemainder(ShapedRecipe.deserializeItemStack(JsonHelper.getObject(jsonObject, "remainder")));
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
        	entry.setRecipeRemainder(ShapedRecipe.deserializeItemStack(JsonHelper.getObject(jsonObject, "remainder")));
        }
        return entry;
	}
	
	private static IngredientEntryCondition loadIngredientEntryCondition(JsonObject jsonObject) {
		if(jsonObject.has("data")) {
			if(JsonHelper.isString(jsonObject.get("data"))) {
				throw new JsonParseException("The data tag on recipes cannot be a string anymore; See the wiki for more information ;)");
			}
			if(jsonObject.get("data").isJsonObject()) {
				return IngredientEntryCondition.fromJson(jsonObject.get("data").getAsJsonObject());
			}
		}
		return new IngredientEntryCondition();
	}
	
	@Override
	public void setRealEntries(Stream<? extends IngredientEntry> entries) {
		realEntries = entries.filter(Predicates.notNull()).toArray(IngredientEntry[]::new);
	}

	@Override
	public ItemStack getRecipeRemainder(ItemStack stack) {
		for(IngredientEntry entry : realEntries) {
			if(entry.matches(stack)) {
				ItemStack remainder = entry.getRecipeRemainder();
				if (remainder != null) {
					return remainder;
				}
			}
		}
		return null;
	}
}
