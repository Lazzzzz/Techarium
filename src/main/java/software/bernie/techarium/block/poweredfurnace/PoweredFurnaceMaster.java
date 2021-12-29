package software.bernie.techarium.block.poweredfurnace;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import software.bernie.techarium.block.base.MachineBlock;
import software.bernie.techarium.tile.arboretum.ArboretumTile;
import software.bernie.techarium.tile.depot.DepotTileEntity;
import software.bernie.techarium.tile.poweredfurnace.PoweredFurnaceTile;
import software.bernie.techarium.trait.block.BlockBehaviours;

public class PoweredFurnaceMaster extends MachineBlock<PoweredFurnaceTile> {
    
	public PoweredFurnaceMaster() {
        super(BlockBehaviours.POWERED_FURNACE, AbstractBlock.Properties.copy(Blocks.IRON_BLOCK));
    }
	
	@Override
	public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(world, pos, state, placer, stack);
        TileEntity tile = world.getBlockEntity(pos);
        if(tile instanceof PoweredFurnaceTile) {
            ((PoweredFurnaceTile) tile).setOpening(true);
        }
	}
	
}
