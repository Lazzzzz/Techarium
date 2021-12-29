package software.bernie.techarium.recipe.serializer;

import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistryEntry;
import software.bernie.techarium.recipe.recipe.ArboretumRecipe;
import software.bernie.techarium.recipe.recipe.PoweredFurnaceRecipe;
import software.bernie.techarium.util.loot.ChancedItemStackList;
import software.bernie.techarium.util.JsonCodecUtils;
import software.bernie.techarium.util.Utils;

import javax.annotation.Nullable;

public class PoweredFurnaceRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<PoweredFurnaceRecipe> {

    @Override
    public PoweredFurnaceRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        Ingredient ingredient = Utils.deserializeIngredient(json, "ingredient");
        ChancedItemStackList output = ChancedItemStackList.fromJSON(json.get("output").getAsJsonArray());
        
        int maxProgress = JSONUtils.getAsInt(json, "maxProgress");
        int ticksPerProgress = JSONUtils.getAsInt(json, "progressPerTick");
        int energy = JSONUtils.getAsInt(json, "rfPerTick");
        
        return new PoweredFurnaceRecipe(recipeId, ingredient, output, ticksPerProgress, maxProgress, energy);
    }

    @Nullable
    @Override
    public PoweredFurnaceRecipe fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
        Ingredient ingredient = Ingredient.fromNetwork(buffer);
        ChancedItemStackList output = ChancedItemStackList.read(buffer);
        int maxProgress = buffer.readInt();
        int ticksPerProgress = buffer.readInt();
        int energy = buffer.readInt();
        return new PoweredFurnaceRecipe(recipeId, ingredient, output, ticksPerProgress, maxProgress, energy);
    }

    @Override
    public void toNetwork(PacketBuffer buffer, PoweredFurnaceRecipe recipe) {
        recipe.getIngredient().toNetwork(buffer);
        recipe.getOutput().write(buffer);
        buffer.writeInt(recipe.getMaxProgress());
        buffer.writeInt(recipe.getProgressPerTick());
        buffer.writeInt(recipe.getRfPerTick());
    }
}
