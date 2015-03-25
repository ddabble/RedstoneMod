package dabble.redstonemod.util;

import java.io.File;
import java.lang.reflect.Field;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;

/**
 * An abstract class that uses java reflection to automate the configuration
 * setting and getting. Each contained field must have a default value set
 * statically.
 * 
 * @author Glenn
 *
 */
public abstract class Configurations {
	public class ItemRepresentation {
		public String name;
		public int metadata = 0;
		public int amount = 1;

		public ItemStack getItemStack() {
			return new ItemStack((Item) Item.itemRegistry.getObject(name), amount == 0 ? 1 : amount, metadata);
		}
	}

	public Configurations(File configurationsFile) {
		setDefaults();

		Configuration config = new Configuration(configurationsFile);
		config.load();

		for (Field field : getClass().getFields()) {
			String category;
			String name;

			String[] splitString = field.getName().split("_");
			if (splitString.length == 2) {
				category = splitString[0];
				name = splitString[1];
			}
			else if (splitString.length == 1) {
				category = "general";
				name = splitString[0];
			}
			else {
				continue;
			}

			try {
				Object o = field.get(this);

				Class<?> c = o.getClass();
				if (c == Integer.class) {
					int def = (Integer) o;
					field.set(this, new Integer(config.get(category, name, def).getInt(def)));
				}
				else if (c == Float.class) {
					float def = (Float) o;
					field.set(this, new Float(Float.parseFloat(config.get(category, name, def).getString())));
				}
				else if (c == Double.class) {
					double def = (Double) o;
					field.set(this, new Double(Double.parseDouble(config.get(category, name, def).getString())));
				}
				else if (c == Boolean.class) {
					boolean def = (Boolean) o;
					field.set(this, new Boolean(config.get(category, name, def).getBoolean(def)));
				}
				else if (c == String.class) {
					String def = (String) o;
					field.set(this, config.get(category, name, def).getString());
				}
				else if (c == String[].class) {
					String[] def = (String[]) o;
					field.set(this, config.get(category, name, def).getStringList());
				}
			} catch (Exception e) {
				System.out.print("Failed to set value for configuration field " + category + ":" + name);
			}
		}

		config.save();

		onLoaded();
	}

	protected abstract void setDefaults();

	protected abstract void onLoaded();
}
