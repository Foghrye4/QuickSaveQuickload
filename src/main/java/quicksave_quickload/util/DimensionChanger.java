package quicksave_quickload.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.WorldServer;

public class DimensionChanger {
	public static void changeDimension(PlayerList plist, EntityPlayerMP player, int toDimenstion, MinecraftServer server) {
        int fromDimension = player.dimension;
		WorldServer worldserver = server.getWorld(player.dimension);
		player.dimension = toDimenstion;
		WorldServer worldserver1 = server.getWorld(player.dimension);
		player.connection.sendPacket(new SPacketRespawn(player.dimension, worldserver1.getDifficulty(), worldserver1.getWorldInfo().getTerrainType(), player.interactionManager.getGameType()));
		plist.updatePermissionLevel(player);
		worldserver.removeEntityDangerously(player);
		player.isDead = false;
		if (player.isEntityAlive()) {
			worldserver.updateEntityWithOptionalForce(player, false);
	        worldserver1.spawnEntity(player);
			worldserver1.updateEntityWithOptionalForce(player, false);
		}
		player.setWorld(worldserver1);
		plist.preparePlayer(player, worldserver);
		player.connection.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
		player.interactionManager.setWorld(worldserver1);
		player.connection.sendPacket(new SPacketPlayerAbilities(player.capabilities));
		plist.updateTimeAndWeatherForPlayer(player, worldserver1);
		plist.syncPlayerInventory(player);
		for (PotionEffect potioneffect : player.getActivePotionEffects()) {
			player.connection.sendPacket(new SPacketEntityEffect(player.getEntityId(), potioneffect));
		}
        net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, fromDimension, toDimenstion);
	}
}
