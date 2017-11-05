package quicksave_quickload.handler;

import net.minecraft.world.chunk.NibbleArray;

public class EBSDataEntry {
	public final byte[] bsdata;
	public final NibbleArray bsa;

	public EBSDataEntry(byte[] bsdataIn, NibbleArray bsaIn) {
		bsdata = bsdataIn;
		bsa = bsaIn;
	}
}
