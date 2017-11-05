package quicksave_quickload.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import cubicchunks.world.ICubeProvider;
import cubicchunks.world.ICubicWorld;
import cubicchunks.world.cube.Cube;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import quicksave_quickload.QuickSaveQuickLoadMod;

public class CubeSaveDataEntry {

	private final List<NBTTagCompound> tileEntityData = new ArrayList<NBTTagCompound>();
	private final NBTTagCompound entityData;

	public CubeSaveDataEntry(Cube cube) {
		for (TileEntity te : cube.getTileEntityMap().values()) {
			tileEntityData.add(te.writeToNBT(new NBTTagCompound()));
		}
		entityData = new NBTTagCompound();
		cube.getEntityContainer().writeToNbt(entityData, "entityData", e -> {QuickSaveQuickLoadMod.log.info("saving "+e);});
	}

	public void load(Cube cube, Set<UUID> loadedEntities) {
		WorldServer world = (WorldServer) cube.getCubicWorld();
		for (NBTTagCompound tag : tileEntityData) {
			int x = tag.getInteger("x");
			int y = tag.getInteger("y");
			int z = tag.getInteger("z");
			BlockPos pos = new BlockPos(x, y, z);
			TileEntity te = cube.getTileEntity(pos, EnumCreateEntityType.IMMEDIATE);
			te.readFromNBT(tag);
		}
		NBTTagList nbtEntities = entityData.getTagList("entityData", 10);
		for (int i = 0; i < nbtEntities.tagCount(); ++i) {
			NBTTagCompound nbtEntity = nbtEntities.getCompoundTagAt(i);
			UUID uid = nbtEntity.getUniqueId("UUID");
			loadedEntities.add(uid);
			Entity entity = world.getEntityFromUuid(uid);
			if (entity != null) {
				entity.readFromNBT(nbtEntity);
				QuickSaveQuickLoadMod.log.info("reading "+entity);
			} else {
				entity = EntityList.createEntityFromNBT(nbtEntity, (World) world);
				QuickSaveQuickLoadMod.log.info("Spawning "+entity);
				if (entity != null)
					world.spawnEntity(entity);
			}
		}
		cube.markDirty();
	}

	public CubeSaveDataEntry(NBTTagCompound tag) {
		NBTTagList tileEntityTags = tag.getTagList("tile", 10);
		for (int i = 0; i < tileEntityTags.tagCount(); i++) {
			tileEntityData.add(tileEntityTags.getCompoundTagAt(i));
		}
		entityData = tag.getCompoundTag("entity");
	}

	public NBTTagCompound toNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		NBTTagList tileEntityTags = new NBTTagList();
		for (NBTTagCompound tileTag : tileEntityData) {
			tileEntityTags.appendTag(tileTag);
		}
		tag.setTag("tile", tileEntityTags);
		tag.setTag("entity", entityData);
		return tag;
	}
}
