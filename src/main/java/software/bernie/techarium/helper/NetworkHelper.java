package software.bernie.techarium.helper;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import software.bernie.techarium.Techarium;
import software.bernie.techarium.network.NetworkConnection;
import software.bernie.techarium.network.ServerToClientPacket;
import software.bernie.techarium.network.tile.UpdateCoilTypePacket;
import software.bernie.techarium.tile.magneticcoils.MagneticCoilTile;

public class NetworkHelper {
	
	public static<T extends ServerToClientPacket<T>> void sendToAllClient(World level, ServerToClientPacket<T> packet) {
		if (level == null) {
			Techarium.LOGGER.error("Trying to send a packet on a null world !");
			return;
		}
		
		
		if (packet == null) {
			Techarium.LOGGER.error("Trying to send a null packet !");
			return;
		}
		
		for (PlayerEntity player : level.players()) { 
			NetworkConnection.INSTANCE.sendTo(packet, ((ServerPlayerEntity) player).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
		}
	}
}
