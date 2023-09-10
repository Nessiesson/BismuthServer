package si.bismuth.utils;

import java.util.HashMap;
import net.minecraft.entity.living.mob.MobCategory;
import net.minecraft.util.Pair;

public class SpawnReporter {
	public static final HashMap<Integer, HashMap<MobCategory, Pair<Integer, Integer>>> mobcaps = new HashMap<>();

	static {
		mobcaps.put(-1, new HashMap<>());
		mobcaps.put(0, new HashMap<>());
		mobcaps.put(1, new HashMap<>());
	}
}
