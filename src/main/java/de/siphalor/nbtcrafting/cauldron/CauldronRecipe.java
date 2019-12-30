package de.siphalor.nbtcrafting.cauldron;

import com.google.common.collect.ImmutableMap;
import de.siphalor.nbtcrafting.Core;
import de.siphalor.nbtcrafting.dollars.Dollar;
import de.siphalor.nbtcrafting.dollars.DollarException;
import de.siphalor.nbtcrafting.dollars.DollarParser;
import de.siphalor.nbtcrafting.ingredient.IIngredient;
import de.siphalor.nbtcrafting.util.NbtHelper;
import de.siphalor.nbtcrafting.util.ServerRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.World;

import java.util.Map;

public class CauldronRecipe implements Recipe<TemporaryCauldronInventory>, ServerRecipe {
	private Identifier identifier;
	public Ingredient input;
	public ItemStack output;
	public int levels;
	private Dollar[] outputDollars;

	public CauldronRecipe(Identifier id, Ingredient ingredient, ItemStack output, int levels) {
		this.identifier = id;
		this.input = ingredient;
		this.output = output;
		this.levels = levels;
		outputDollars = DollarParser.extractDollarsFromCopy(output.getTag());
	}

	public void write(PacketByteBuf packetByteBuf) {
		packetByteBuf.writeIdentifier(identifier);
		input.write(packetByteBuf);
		packetByteBuf.writeItemStack(output);
		packetByteBuf.writeShort(levels);
	}

	public static CauldronRecipe from(PacketByteBuf packetByteBuf) {
		Identifier identifier = packetByteBuf.readIdentifier();
		Ingredient input = Ingredient.fromPacket(packetByteBuf);
		ItemStack output = packetByteBuf.readItemStack();
		int levels = packetByteBuf.readShort();
		return new CauldronRecipe(identifier, input, output, levels);
	}

	@Override
	public boolean matches(TemporaryCauldronInventory inventory, World world) {
		return inventory.getLevel() >= levels && input.test(inventory.getInvStack(0));
	}

	@Override
	public ItemStack craft(TemporaryCauldronInventory inventory) {
		inventory.setLevel(inventory.getLevel() - levels);

		Map<String, CompoundTag> reference = ImmutableMap.of("i0", NbtHelper.getTagOrEmpty(inventory.getInvStack(0)));

		ItemStack remainder = ((IIngredient)(Object) input).getRecipeRemainder(inventory.getInvStack(0), reference);
		if(remainder != null)
			inventory.setInvStack(0, remainder);

		ItemStack result = output.copy();
		for(Dollar dollar : outputDollars) {
			try {
				dollar.apply(result, reference);
			} catch (DollarException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
	public boolean fits(int i, int i1) {
		return false;
	}

	@Override
	public ItemStack getOutput() {
		return output;
	}

	@Override
	public Identifier getId() {
		return identifier;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return Core.CAULDRON_RECIPE_SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return Core.CAULDRON_RECIPE_TYPE;
	}
}
