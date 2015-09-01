package dabble.redstonemod;

import java.io.File;

import dabble.redstonemod.util.Configurations;

public class RedstoneModConfig extends Configurations {
	public String whatToPrint;

	public RedstoneModConfig(File configurationsFile) {
		super(configurationsFile);
	}

	@Override
	protected void setDefaults() {
		this.whatToPrint = "";
	}

	@Override
	protected void onLoaded() {

	}
}
