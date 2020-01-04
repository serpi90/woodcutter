package ovh.corail.woodcutter.recipe;

import com.google.gson.JsonObject;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SingleItemRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;
import ovh.corail.woodcutter.registry.ModBlocks;
import ovh.corail.woodcutter.registry.ModRecipeSerializers;
import ovh.corail.woodcutter.registry.ModRecipeTypes;

public class WoodcuttingRecipe extends SingleItemRecipe {

    public WoodcuttingRecipe(ResourceLocation id, String group, Ingredient ingredient, ItemStack result) {
        super(ModRecipeTypes.WOODCUTTING, ModRecipeSerializers.WOODCUTTING, id, group, ingredient, result);
    }

    @Override
    public boolean matches(IInventory inv, World worldIn) {
        return this.ingredient.test(inv.getStackInSlot(0));
    }

    @Override
    public ItemStack getIcon() {
        return ModBlocks.createRandomStack();
    }

    public static class Serializer<T extends WoodcuttingRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {
        final IRecipeFactory<T> factory;

        public Serializer(IRecipeFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        @SuppressWarnings("deprecation")
        public T read(ResourceLocation recipeId, JsonObject json) {
            String s = JSONUtils.getString(json, "group", "");
            Ingredient ingredient;
            if (JSONUtils.isJsonArray(json, "ingredient")) {
                ingredient = Ingredient.deserialize(JSONUtils.getJsonArray(json, "ingredient"));
            } else {
                ingredient = Ingredient.deserialize(JSONUtils.getJsonObject(json, "ingredient"));
            }

            String s1 = JSONUtils.getString(json, "result");
            int i = JSONUtils.getInt(json, "count");
            ItemStack itemstack = new ItemStack(Registry.ITEM.getOrDefault(new ResourceLocation(s1)), i);
            return this.factory.create(recipeId, s, ingredient, itemstack);
        }

        @Override
        public T read(ResourceLocation recipeId, PacketBuffer buffer) {
            String s = buffer.readString(32767);
            Ingredient ingredient = Ingredient.read(buffer);
            ItemStack itemstack = buffer.readItemStack();
            return this.factory.create(recipeId, s, ingredient, itemstack);
        }

        @Override
        public void write(PacketBuffer buffer, T recipe) {
            buffer.writeString(recipe.group);
            recipe.ingredient.write(buffer);
            buffer.writeItemStack(recipe.result);
        }

        public interface IRecipeFactory<T extends WoodcuttingRecipe> {
            T create(ResourceLocation id, String group, Ingredient ingredient, ItemStack result);
        }
    }
}
