package de.siphalor.nbtcrafting.mixin;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import de.siphalor.nbtcrafting.ingredient.IngredientEntry;
import de.siphalor.nbtcrafting.ingredient.IngredientMultiStackEntry;
import de.siphalor.nbtcrafting.ingredient.IngredientStackEntry;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

@Mixin(Ingredient.class)
public abstract class IngredientMixin {
	
	private IngredientEntry[] realEntries;
	
	@Shadow
	private ItemStack[] stackArray;
	@Shadow
	private IntList ids;
	
	public IngredientMixin(Stream<? extends IngredientEntry> entries) {
		this.realEntries = entries.toArray(IngredientEntry[]::new);
	}
	
	@Overwrite
	private void createStackArray() {
		if(stackArray != null)
			return;
		stackArray = Arrays.stream(realEntries).flatMap(entry -> entry.getPreviewStacks().stream()).distinct().toArray(ItemStack[]::new);
	}
	
	@Overwrite
	public boolean matches(ItemStack stack) {
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
	
	private static Ingredient ofEntries(Stream<? extends IngredientEntry> entries) {
		try {
			Constructor<Ingredient> constructor = Ingredient.class.getConstructor(Stream.class);
			return constructor.newInstance(entries);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Overwrite
	public static Ingredient ofItems(ItemProvider... arr) {
		return ofEntries(Stream.of(new IngredientMultiStackEntry(Arrays.stream(arr).map(item -> Registry.ITEM.getRawId(item.getItem())).collect(Collectors.toList()), null)));
	}
	
	@Overwrite
	public static Ingredient ofStacks(ItemStack... arr) {
		return ofEntries(Arrays.stream(arr).map(stack -> new IngredientStackEntry(stack)));
	}
	
	@Overwrite
	public static Ingredient fromTag(Tag<Item> tag) {
		return ofEntries(Stream.of(new IngredientMultiStackEntry(tag.values().stream().map(item -> Registry.ITEM.getRawId(item)).collect(Collectors.toList()), null)));
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
		return ofEntries(entries.stream());
	}
	
	// Imported from Mojang
	@Overwrite
	public static Ingredient fromJson(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            throw new JsonSyntaxException("Item cannot be null");
        }
        if (element.isJsonObject()) {
            return ofEntries(Stream.of(realEntryFromJson(element.getAsJsonObject())));
        }
        if (!element.isJsonArray()) {
            throw new JsonSyntaxException("Expected item to be object or array of objects");
        }
        final JsonArray jsonArray2 = element.getAsJsonArray();
        if (jsonArray2.size() == 0) {
            throw new JsonSyntaxException("Item array cannot be empty, at least one item must be defined");
        }
        return ofEntries(StreamSupport.<JsonElement>stream(jsonArray2.spliterator(), false).map(e-> realEntryFromJson(JsonHelper.asObject(e, "item"))));
    }
	
	// Imported from Mojang
	private static IngredientEntry realEntryFromJson(JsonObject jsonObject) {
		if (jsonObject.has("item") && jsonObject.has("tag")) {
            throw new JsonParseException("An ingredient entry is either a tag or an item, not both");
        }
        if (jsonObject.has("item")) {
            final Identifier identifier = new Identifier(JsonHelper.getString(jsonObject, "item"));
            try {
				final Item item = Registry.ITEM.getOptional(identifier).orElseThrow(() -> {
					throw new JsonSyntaxException("Unknown item '" + identifier.toString() + "'");
				});
				return new IngredientStackEntry(Registry.ITEM.getRawId(item), null);
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
        IngredientMultiStackEntry entry = new IngredientMultiStackEntry(tag.values().stream().map(item -> Registry.ITEM.getRawId(item)).collect(Collectors.toList()), null);
        entry.setTag(tag.toString());
        return entry;
	}
	
	/*@Inject(method = "entryFromJson", at = @At("HEAD"))
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
	}*/
}
