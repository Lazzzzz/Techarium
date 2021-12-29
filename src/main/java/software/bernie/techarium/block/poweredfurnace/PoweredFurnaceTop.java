package software.bernie.techarium.block.poweredfurnace;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import software.bernie.techarium.block.base.MachineBlock;
import software.bernie.techarium.tile.poweredfurnace.PoweredFurnaceTile;
import software.bernie.techarium.tile.poweredfurnace.PoweredFurnaceTile.animationState;
import software.bernie.techarium.tile.slaves.TopEnabledOnlySlave;
import software.bernie.techarium.trait.block.BlockBehaviours;

public class PoweredFurnaceTop extends MachineBlock<TopEnabledOnlySlave> {
    public PoweredFurnaceTop() {
        super(BlockBehaviours.createSlave(BlockBehaviours.POWERED_FURNACE), Properties.of(Material.METAL).strength(3.5f).harvestLevel(2).harvestTool(ToolType.PICKAXE).noOcclusion().requiresCorrectToolForDrops().noDrops());
    }

    @Override
    public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
    	TileEntity tile = world.getBlockEntity(pos.below());
    	
    	if (tile instanceof PoweredFurnaceTile) {
    		if (((PoweredFurnaceTile) tile).getState() == animationState.WORKING) { 
    			Vector3d vec = entity.position();
    			AxisAlignedBB box = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 0.75f, pos.getZ() + 1);
    			if (box.contains(vec))
    				entity.setDeltaMovement(entity.getDeltaMovement().add(0,0.6f,0));
    		}
    	}  
    }
    
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
    	TileEntity tile = world.getBlockEntity(pos.below());
    	
    	if (tile instanceof PoweredFurnaceTile) {
    		if (((PoweredFurnaceTile) tile).getState() == animationState.IDLE)
    			return VoxelShapes.empty();
    	}    	
        return box(0,0,0,16,12,16);
    }
}
