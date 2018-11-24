package quicksave_quickload.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.storage.WorldSavedData;
import quicksave_quickload.handler.CubeSaveDataEntry;
import quicksave_quickload.handler.EBSDataEntry;

public class WorldSavedDataCubesAndEntities extends WorldSavedData {

	private static String DATA_IDENTIFIER = "quicksave";
	public Map<CubePos, EBSDataEntry> ebsData = new HashMap<CubePos, EBSDataEntry>();
	public Map<CubePos, CubeSaveDataEntry> cubeEntityData = new HashMap<CubePos, CubeSaveDataEntry>();
	public Map<UUID, NBTTagCompound> playersData = new HashMap<UUID, NBTTagCompound>();

	public WorldSavedDataCubesAndEntities(String name) {
		super(name);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		NBTTagList ebsDataTags = compound.getTagList("ebs", 10);
		NBTTagList cubeEntityDataTags = compound.getTagList("entity", 10);
		NBTTagList playersTags = compound.getTagList("players", 10);
		for (int i = 0; i < ebsDataTags.tagCount(); i++) {
			NBTTagCompound tag = ebsDataTags.getCompoundTagAt(i);
			int[] posa = tag.getIntArray("pos");
			CubePos pos = new CubePos(posa[0], posa[1], posa[2]);
			EBSDataEntry entry = new EBSDataEntry();
			if (tag.hasKey("bsdata")) {
				byte[] bsdata = tag.getByteArray("bsdata");
				entry.setData(bsdata);
			}
			ebsData.put(pos, entry);
		}
		for (int i = 0; i < cubeEntityDataTags.tagCount(); i++) {
			NBTTagCompound tag = cubeEntityDataTags.getCompoundTagAt(i);
			int[] posa = tag.getIntArray("pos");
			CubePos pos = new CubePos(posa[0], posa[1], posa[2]);
			cubeEntityData.put(pos, new CubeSaveDataEntry(tag));
		}
		for (int i = 0; i < playersTags.tagCount(); i++) {
			NBTTagCompound tag = playersTags.getCompoundTagAt(i);
			playersData.put(tag.getUniqueId("UUID"), tag);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList ebsDataTags = new NBTTagList();
		NBTTagList cubeEntityDataTags = new NBTTagList();
		NBTTagList playersTags = new NBTTagList();
		for (Entry<CubePos, EBSDataEntry> entry : ebsData.entrySet()) {
			NBTTagCompound tag = new NBTTagCompound();
			CubePos pos = entry.getKey();
			tag.setIntArray("pos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
			if (entry.getValue().bsdata != null)
				tag.setByteArray("bsdata", entry.getValue().bsdata);
			ebsDataTags.appendTag(tag);
		}
		for (Entry<CubePos, CubeSaveDataEntry> entry : cubeEntityData.entrySet()) {
			NBTTagCompound tag = entry.getValue().toNBT();
			CubePos pos = entry.getKey();
			tag.setIntArray("pos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
			cubeEntityDataTags.appendTag(tag);
		}
		for (Entry<UUID, NBTTagCompound> entry : playersData.entrySet()) {
			playersTags.appendTag(entry.getValue());
		}
		compound.setTag("ebs", ebsDataTags);
		compound.setTag("entity", cubeEntityDataTags);
		compound.setTag("players", playersTags);
		return compound;
	}

	public static String dataIdentifier(World world) {
		return DATA_IDENTIFIER + world.provider.getDimension();
	}

}
