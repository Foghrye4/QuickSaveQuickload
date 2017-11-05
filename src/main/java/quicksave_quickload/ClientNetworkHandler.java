package quicksave_quickload;

import static quicksave_quickload.QuickSaveQuickLoadMod.MODID;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(value = Side.CLIENT)
public class ClientNetworkHandler extends ServerNetworkHandler {

	@SubscribeEvent
	public void onPacketFromServerToClient(FMLNetworkEvent.ClientCustomPacketEvent event) throws IOException {
		ByteBuf data = event.getPacket().payload();
		PacketBuffer byteBufInputStream = new PacketBuffer(data);
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.player;
		switch (ClientCommands.values()[byteBufInputStream.readByte()]) {
			case LOAD_PLAYER_DATA :
				NBTTagCompound tag = byteBufInputStream.readCompoundTag();
				player.readFromNBT(tag);
				break;
			default :
				break;
		}
	}

	public void sendPacketSaveGame() {
		this.sendServerCommandPacket(ServerCommands.SAVE_GAME);
	}

	public void sendPacketLoadGame() {
		this.sendServerCommandPacket(ServerCommands.LOAD_GAME);
	}

	private void sendServerCommandPacket(ServerCommands command) {
		ByteBuf bb = Unpooled.buffer(36);
		PacketBuffer byteBufOutputStream = new PacketBuffer(bb);
		Minecraft mc = Minecraft.getMinecraft();
		byteBufOutputStream.writeByte(command.ordinal());
		byteBufOutputStream.writeInt(mc.world.provider.getDimension());
		byteBufOutputStream.writeUniqueId(mc.player.getUniqueID());
		channel.sendToServer(new FMLProxyPacket(byteBufOutputStream, MODID));
	}
}
