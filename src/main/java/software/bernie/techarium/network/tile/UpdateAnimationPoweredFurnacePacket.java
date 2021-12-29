package software.bernie.techarium.network.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import software.bernie.techarium.machine.controller.MachineController;
import software.bernie.techarium.network.ServerToClientPacket;
import software.bernie.techarium.recipe.recipe.PoweredFurnaceRecipe;
import software.bernie.techarium.tile.poweredfurnace.PoweredFurnaceTile;

public class UpdateAnimationPoweredFurnacePacket extends ServerToClientPacket<UpdateAnimationPoweredFurnacePacket> {
	
	private BlockPos pos;
	private PoweredFurnaceTile.animationState state;
	
    public UpdateAnimationPoweredFurnacePacket() {}
	
	public UpdateAnimationPoweredFurnacePacket(BlockPos pos, PoweredFurnaceTile.animationState state) {
		this.pos = pos;
		this.state = state;
	}
	
	@Override
	public void write(PacketBuffer writeInto) {
		writeInto.writeBlockPos(pos);
		writeInto.writeInt(state.ordinal());
	}
	
	@Override
	public UpdateAnimationPoweredFurnacePacket create(PacketBuffer readFrom) {
		pos = readFrom.readBlockPos();
		state = PoweredFurnaceTile.animationState.values()[readFrom.readInt()];
		return new UpdateAnimationPoweredFurnacePacket(pos, state);
	}

	@Override
	public void doAction(Context context) {
		context.enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			ClientWorld level = mc.level;
			if (level != null) {
				TileEntity te = level.getBlockEntity(pos);
				
				if (te instanceof PoweredFurnaceTile) {
					((PoweredFurnaceTile) te).setState(state);
				}
			}
		});

		context.setPacketHandled(true);
	}	
}
