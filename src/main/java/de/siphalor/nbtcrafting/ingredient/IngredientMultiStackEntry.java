package de.siphalor.nbtcrafting.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.siphalor.nbtcrafting.util.IItemStack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class IngredientMultiStackEntry extends IngredientEntry {
	
	private IngredientEntryCondition condition;
	private IntList itemIds;
	private String tag;

	public IngredientMultiStackEntry(Collection<Integer> items, IngredientEntryCondition condition) {
		super();
		this.condition = condition;
		this.itemIds = new IntArrayList(items);
		this.tag = "";
	}
	
	@Override
	public boolean matches(ItemStack stack) {
		return itemIds.contains(Registry.ITEM.getRawId(stack.getItem())) && condition.matches(stack);
	}

	@Override
	public JsonElement toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("tag", tag);
		condition.addToJson(json);
		return json;
	}

	@Override
	public Collection<ItemStack> getPreviewStacks(boolean nbt) {
		CompoundTag tag = condition.getPreviewTag();
		Collection<ItemStack> stacks = itemIds.stream().map(id -> new ItemStack(Registry.ITEM.get(id))).collect(Collectors.toList());
		if(nbt) {
			for (ItemStack itemStack : stacks) {
				((IItemStack) (Object) itemStack).setRawTag(tag);
			}
		}
		return stacks;
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeVarInt(itemIds.size());
		for(int i = 0; i < itemIds.size(); i++) {
			buf.writeVarInt(itemIds.getInt(i));
		}
		this.condition.write(buf);
		buf.writeBoolean(remainder != null);
		if(remainder != null)
			buf.writeItemStack(remainder);
	}

	public static IngredientMultiStackEntry read(PacketByteBuf buf) {
		int length = buf.readVarInt();
		ArrayList<Integer> ids = new ArrayList<>();
		for(int i = 0; i < length; i++) {
			ids.add(buf.readVarInt());
		}
		IngredientMultiStackEntry entry = new IngredientMultiStackEntry(ids, IngredientEntryCondition.read(buf));
		if(buf.readBoolean())
			entry.setRecipeRemainder(buf.readItemStack());
		return entry;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

}
