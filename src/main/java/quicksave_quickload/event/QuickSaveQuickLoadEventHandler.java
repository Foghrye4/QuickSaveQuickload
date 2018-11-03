package quicksave_quickload.event;

import javax.annotation.Nullable;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.CubeWatchEvent;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.opencubicchunks.cubicchunks.core.world.cube.Cube;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import quicksave_quickload.handler.CubeSaveDataEntry;
import quicksave_quickload.handler.EBSDataEntry;
import quicksave_quickload.handler.SaveLoadHandler;
import quicksave_quickload.world.WorldSavedDataCubesAndEntities;

public class QuickSaveQuickLoadEventHandler {

	private boolean isLoadScheduled = false;
	private boolean isSaveScheduled = false;
	@Nullable
	EntityPlayerMP playerCommandSender;

	public void scheduleLoadOnNextTick(@Nullable Entity commandSenderIn) {
		isLoadScheduled = true;
		isSaveScheduled = false;
		if (commandSenderIn instanceof EntityPlayerMP) {
			playerCommandSender = (EntityPlayerMP) commandSenderIn;
			playerCommandSender.sendMessage(new TextComponentString("Loading game..."));
		}
	}

	public void save(@Nullable Entity commandSenderIn) {
		isLoadScheduled = false;
		isSaveScheduled = true;
		if (commandSenderIn instanceof EntityPlayerMP) {
			playerCommandSender = (EntityPlayerMP) commandSenderIn;
		}
	}

	@SubscribeEvent
	public void runScheduledLoadOnServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END)
			return;
		if (!isLoadScheduled && !isSaveScheduled)
			return;
		if(isLoadScheduled)
			SaveLoadHandler.loadAll();
		if(isSaveScheduled)
			SaveLoadHandler.saveAll();
		if (playerCommandSender != null) {
			if(isLoadScheduled)
				playerCommandSender.sendMessage(new TextComponentString("Game loaded"));
			if(isSaveScheduled)
				playerCommandSender.sendMessage(new TextComponentString("Game saved"));
			playerCommandSender = null;
		}
		isLoadScheduled = false;
		isSaveScheduled = false;
	}
	@SubscribeEvent
	public void onBeforeExplosion(ExplosionEvent.Start event) {
		Vec3d pos = event.getExplosion().getPosition();
		CubePos cpos = CubePos.fromEntityCoords(pos.x,pos.y,pos.z);
		cpos.forEachWithinRange(1, cubePos -> {
			this.updateEBSData(event.getWorld(), cubePos);
		});
	}

	@SubscribeEvent
	public void onBlockBreak(BreakEvent event) {
		this.updateEBSData(event.getWorld(), event.getPos());
	}

	@SubscribeEvent
	public void onBeforeBlockPlaced(RightClickBlock event) {
		this.updateEBSData(event.getWorld(), event.getPos().offset(event.getFace()));
	}

	@SubscribeEvent
	public void onCubeWatchEvent(CubeWatchEvent event) {
		this.updateEntityData(event.getWorld(), (Cube) event.getCube(), event.getCubePos());
	}

	private void updateEntityData(ICubicWorld cworld, @Nullable Cube cube, CubePos cpos) {
		World world = (World) cworld;
		if (world.isRemote)
			return;
		if (cube == null)
			return;
		WorldSavedDataCubesAndEntities data = this.getOrCreateData(world);
		CubeSaveDataEntry ced = data.cubeEntityData.get(cpos);
		if (ced == null) {
			ced = new CubeSaveDataEntry(cube);
			data.cubeEntityData.put(cpos, ced);
			return;
		}
	}

	private void updateEBSData(World world, BlockPos bpos) {
		this.updateEBSData(world, CubePos.fromBlockCoords(bpos));
	}

	private void updateEBSData(World world, CubePos cpos) {
		if (world.isRemote)
			return;
		ICubicWorld cworld = (ICubicWorld) world;
		if (!cworld.isCubicWorld())
			return;
		WorldSavedDataCubesAndEntities data = this.getOrCreateData(world);
		if (data.ebsData.containsKey(cpos))
			return;
		Cube lc = (Cube) cworld.getCubeCache().getCube(cpos);
		EBSDataEntry dataEntry = new EBSDataEntry(new byte[4096], new NibbleArray());
		if (!lc.isEmpty())
			lc.getStorage().getData().getDataForNBT(dataEntry.bsdata, dataEntry.bsa);
		data.ebsData.put(cpos, dataEntry);
		data.markDirty();
	}

	private WorldSavedDataCubesAndEntities getOrCreateData(World world) {
		WorldSavedDataCubesAndEntities data = (WorldSavedDataCubesAndEntities) world.getMapStorage().getOrLoadData(WorldSavedDataCubesAndEntities.class, WorldSavedDataCubesAndEntities.dataIdentifier(world));
		if (data == null) {
			data = new WorldSavedDataCubesAndEntities(WorldSavedDataCubesAndEntities.dataIdentifier(world));
			data.markDirty();
			world.getMapStorage().setData(WorldSavedDataCubesAndEntities.dataIdentifier(world), data);
		}
		return data;
	}
}
