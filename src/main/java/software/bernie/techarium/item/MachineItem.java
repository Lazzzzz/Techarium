package software.bernie.techarium.item;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import software.bernie.techarium.tile.base.MachineMasterTile;

import javax.annotation.Nullable;
import java.util.List;

public class MachineItem extends BlockItem {

    private final int tiers;

    public MachineItem(int tiers, Block blockIn, Properties builder) {
        super(blockIn, builder);
        this.tiers = tiers;
    }

    @Override
    public ActionResultType tryPlace(BlockItemUseContext context) {
        ActionResultType result = super.tryPlace(context);
        TileEntity tile = context.getWorld().getTileEntity(context.getPos());
        if(tile instanceof MachineMasterTile<?>){
            MachineMasterTile<?> master = (MachineMasterTile<?>) tile;
            ItemStack stack = context.getItem();
            if(stack.hasTag() && stack.getTag() != null) {
                master.getController().setActiveTier(stack.getTag().getInt("tier"));
            }
        }
        return result;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        int tier = 0;
        if (stack.hasTag() && stack.getTag() != null) {
            tier = stack.getTag().getInt("tier");
        }
        tooltip.add(new TranslationTextComponent(getTranslationKey() + "_tier_" + tier));
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        if(stack.hasTag() && stack.getTag()!= null){
            return super.getTranslationKey(stack) + "_tier_" +stack.getTag().getInt("tier");
        }
        return super.getTranslationKey(stack);
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            for (int x = 1; x <= tiers; ++x) {
                CompoundNBT nbt = new CompoundNBT();
                nbt.putInt("tier", x);
                ItemStack stack = this.getDefaultInstance();
                stack.setTag(nbt);
                items.add(stack);
            }
        }
    }
}
