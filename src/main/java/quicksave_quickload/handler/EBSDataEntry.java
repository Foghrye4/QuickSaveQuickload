package quicksave_quickload.handler;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.chunk.NibbleArray;

public class EBSDataEntry {
	public byte[] bsdata;

	public void setData(byte[] bytesIn) {
		bsdata = bytesIn;
	}
}
