package quicksave_quickload.handler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Predicate;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICubeProvider;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.opencubicchunks.cubicchunks.core.network.PacketCubes;
import io.github.opencubicchunks.cubicchunks.core.network.PacketDispatcher;
import io.github.opencubicchunks.cubicchunks.core.world.cube.Cube;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import quicksave_quickload.QuickSaveQuickLoadMod;
import quicksave_quickload.util.DimensionChanger;
import quicksave_quickload.world.WorldSavedDataCubesAndEntities;

public class SaveLoadHandler {
	public static void saveAll() {
		for (WorldServer world : QuickSaveQuickLoadMod.proxy.getServer().worlds) {
			save(world);
		}
	}

	public static void loadAll() {
		for (WorldServer world : QuickSaveQuickLoadMod.proxy.getServer().worlds) {
			load(world);
		}
	}

	private static void save(WorldServer world) {
		ICubicWorld cworld = (ICubicWorld) world;
		if (!cworld.isCubicWorld())
			return;
		WorldSavedDataCubesAndEntities data = getOrCreateData(world);
		data.ebsData.clear();
		data.cubeEntityData.clear();
		data.playersData.clear();
		ICubeProvider cache = cworld.getCubeCache();
		Iterator<Entity> ei = world.loadedEntityList.iterator();
		while (ei.hasNext()) {
			Entity entity = ei.next();
			if (entity instanceof EntityPlayer)
				continue;
			int cposX = entity.chunkCoordX;
			int cposY = entity.chunkCoordY;
			int cposZ = entity.chunkCoordZ;
			CubePos cpos = new CubePos(cposX, cposY, cposZ);
			if (data.cubeEntityData.containsKey(cpos))
				continue;
			Cube cube = (Cube) cache.getCube(cpos);
			data.cubeEntityData.put(cpos, new CubeSaveDataEntry(cube));
		}
		for (TileEntity te : world.loadedTileEntityList) {
			CubePos cpos = CubePos.fromBlockCoords(te.getPos());
			if (data.cubeEntityData.containsKey(cpos))
				continue;
			data.cubeEntityData.put(cpos, new CubeSaveDataEntry((Cube) cache.getCube(cpos)));
		}
		for (EntityPlayer player : world.playerEntities) {
			QuickSaveQuickLoadMod.log.info("Saving player " + player.getName() + " with UID "+ player.getUniqueID());
			data.playersData.put(player.getUniqueID(), player.writeToNBT(new NBTTagCompound()));
		}
		data.markDirty();
	}

	private static void load(WorldServer world) {
		ICubicWorld cworld = (ICubicWorld) world;
		if (!cworld.isCubicWorld())
			return;
		WorldSavedDataCubesAndEntities data = getOrCreateData(world);
		ICubeProvider cache = cworld.getCubeCache();
		List<Cube> cubesToUpdate = new ArrayList<Cube>();
		for (Entry<CubePos, EBSDataEntry> entry : data.ebsData.entrySet()) {
			CubePos cpos = entry.getKey();
			Cube cube = (Cube) cache.getCube(cpos);
			EBSDataEntry dataEntry = data.ebsData.get(cpos);
			if (cube.getStorage() == null)
				cube.setStorage(new ExtendedBlockStorage(Coords.cubeToMinBlock(cpos.getY()),
						world.provider.hasSkyLight()));
			cube.getStorage().getData().setDataFromNBT(dataEntry.bsdata, dataEntry.bsa, null);
			cube.getStorage().recalculateRefCounts();
			cube.markDirty();
			cubesToUpdate.add(cube);
		}
		Set<UUID> loadedEntities = new HashSet<UUID>(); 
		for (Entry<CubePos, CubeSaveDataEntry> entry : data.cubeEntityData.entrySet()) {
			CubePos cpos = entry.getKey();
			Cube cube = (Cube) cache.getCube(cpos);
			entry.getValue().load(cube, loadedEntities);
		}
		Iterator<Entity> ei = world.loadedEntityList.iterator();
		while (ei.hasNext()) {
			Entity entity = ei.next();
			if(entity instanceof EntityPlayer)
				continue;
			if(loadedEntities.contains(entity.getUniqueID()))
				continue;
			entity.setDead();
		}
		for (Entry<UUID, NBTTagCompound> entry : data.playersData.entrySet()) {
			NBTTagCompound compound = entry.getValue();
			int dimension = compound.getInteger("Dimension");
			if (dimension != world.provider.getDimension())
				continue;
			NBTTagList nbttaglist = compound.getTagList("Pos", 6);
			NBTTagList nbttaglist3 = compound.getTagList("Rotation", 5);
			double posX = nbttaglist.getDoubleAt(0);
			double posY = nbttaglist.getDoubleAt(1);
			double posZ = nbttaglist.getDoubleAt(2);
			float rotationYaw = nbttaglist3.getFloatAt(0);
			float rotationPitch = nbttaglist3.getFloatAt(1);
			MinecraftServer server = QuickSaveQuickLoadMod.proxy.getServer();
			EntityPlayerMP player = (EntityPlayerMP) server.getPlayerList().getPlayerByUUID(entry.getKey());
			if (player != null) {
                if (player.getHealth() <= 0.0F) {
                	player.connection.player = server.getPlayerList().recreatePlayerEntity(player, dimension, false);
                	player = player.connection.player;
                }
				QuickSaveQuickLoadMod.log.info("Loading save data for player " + player.getName());
				if (player.world.provider.getDimension() != dimension)
					DimensionChanger.changeDimension(server.getPlayerList(), (EntityPlayerMP) player, dimension, server);
				player.connection.setPlayerLocation(posX, posY, posZ, rotationYaw, rotationPitch);
				player.readFromNBT(entry.getValue());
				server.getPlayerList().syncPlayerInventory(player);
			} else {
				QuickSaveQuickLoadMod.log.info("Player with UID" + entry.getKey() + " not logged on server.");
			}
		}
		PacketDispatcher.sendToDimension(new PacketCubes(cubesToUpdate), world.provider.getDimension());
	}

	private static WorldSavedDataCubesAndEntities getOrCreateData(World world) {
		WorldSavedDataCubesAndEntities data = (WorldSavedDataCubesAndEntities) world.getMapStorage().getOrLoadData(WorldSavedDataCubesAndEntities.class, WorldSavedDataCubesAndEntities.dataIdentifier(world));
		if (data == null) {
			data = new WorldSavedDataCubesAndEntities(WorldSavedDataCubesAndEntities.dataIdentifier(world));
			data.markDirty();
			world.getMapStorage().setData(WorldSavedDataCubesAndEntities.dataIdentifier(world), data);
		}
		return data;
	}
}
