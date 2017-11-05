package quicksave_quickload;

import static quicksave_quickload.QuickSaveQuickLoadMod.MODID;
import static quicksave_quickload.QuickSaveQuickLoadMod.log;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import quicksave_quickload.handler.SaveLoadHandler;

public class ServerNetworkHandler {

	protected static final FMLEventChannel channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(MODID);

	public enum ServerCommands {
		SAVE_GAME,
		LOAD_GAME;
	}

	public enum ClientCommands {
		LOAD_PLAYER_DATA;
	}

	private MinecraftServer server;

	public ServerNetworkHandler() {
		channel.register(this);
	}

	@SubscribeEvent
	public void onPacketFromClientToServer(FMLNetworkEvent.ServerCustomPacketEvent event) throws IOException {
		ByteBuf data = event.getPacket().payload();
		PacketBuffer byteBufStream = new PacketBuffer(data);
		ServerCommands command = ServerCommands.values()[byteBufStream.readByte()];
		int dimensionId = byteBufStream.readInt();
		WorldServer world = server.getWorld(dimensionId);
		EntityPlayer player = world.getPlayerEntityByUUID(byteBufStream.readUniqueId());
		if(!server.getPlayerList().canSendCommands(player.getGameProfile()))
			log.info("Player " + player.getName() + " are not allowed to send commands.");
		UserListOpsEntry userlistopsentry = (UserListOpsEntry) server.getPlayerList().getOppedPlayers().getEntry(player.getGameProfile());
		if (userlistopsentry == null) {
			if (server.getOpPermissionLevel() < 4) {
				log.info("Player " + player.getName() + " try to launch command but not OP.");
				return;
			}
		} else if (userlistopsentry.getPermissionLevel() < 4) {
			log.info("Player " + player.getName() + " have insufficient permission level.");
			return;
		}
		switch (command) {
			case SAVE_GAME :
				QuickSaveQuickLoadMod.eventHandler.save(player);
				break;
			case LOAD_GAME :
				QuickSaveQuickLoadMod.eventHandler.scheduleLoadOnNextTick(player);
				break;
			default :
				break;
		}
	}

	public void sendNBTTagToPlayer(EntityPlayerMP player, NBTTagCompound tag) {
		ByteBuf bb = Unpooled.buffer(36);
		PacketBuffer byteBufOutputStream = new PacketBuffer(bb);
		byteBufOutputStream.writeByte(ClientCommands.LOAD_PLAYER_DATA.ordinal());
		byteBufOutputStream.writeCompoundTag(tag);
		channel.sendTo(new FMLProxyPacket(byteBufOutputStream, MODID), player);
	}

	public void setServer(MinecraftServer serverIn) {
		this.server = serverIn;
	}
}
