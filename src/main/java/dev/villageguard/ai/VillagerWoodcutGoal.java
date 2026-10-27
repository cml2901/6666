package dev.villageguard.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Житель валит настоящие деревья (бревно с листвой над ним, не доски и не стены домов)
 * и сажает саженец на месте. Брёвна выпадают на землю — их можно подобрать.
 */
public class VillagerWoodcutGoal extends MoveToBlockGoal {
	private final Villager villager;

	public VillagerWoodcutGoal(Villager villager) {
		super(villager, 0.6, 16, 4);
		this.villager = villager;
	}

	@Override
	public boolean canUse() {
		return !this.villager.isSleeping()
			&& this.villager.level().getMaxLocalRawBrightness(this.villager.blockPosition()) > 8
			&& super.canUse();
	}

	private static String path(BlockState state) {
		return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
	}

	private static boolean isLog(BlockState state) {
		String p = path(state);
		return p.endsWith("_log") && !p.startsWith("stripped_");
	}

	@Override
	protected boolean isValidTarget(LevelReader level, BlockPos pos) {
		if (!isLog(level.getBlockState(pos))) {
			return false;
		}
		BlockPos below = pos.below();
		// нижнее бревно ствола, под ним земля
		if (isLog(level.getBlockState(below)) || level.getBlockState(below).isAir()) {
			return false;
		}
		// над стволом есть листва — значит это дерево, а не постройка
		for (int dy = 1; dy <= 7; dy++) {
			for (int dx = -2; dx <= 2; dx++) {
				for (int dz = -2; dz <= 2; dz++) {
					if (path(level.getBlockState(pos.offset(dx, dy, dz))).endsWith("_leaves")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.isReachedTarget() || !(this.villager.level() instanceof ServerLevel level)
			|| !isValidTarget(level, this.blockPos)) {
			return;
		}
		String logName = path(level.getBlockState(this.blockPos));
		// Валим ствол целиком (не больше 14 брёвен подряд)
		BlockPos p = this.blockPos;
		for (int i = 0; i < 14 && isLog(level.getBlockState(p)); i++) {
			level.destroyBlock(p, true, this.villager);
			p = p.above();
		}
		// Саженец на месте нижнего бревна: oak_log -> oak_sapling
		String sapling = logName.substring(0, logName.length() - "_log".length()) + "_sapling";
		BuiltInRegistries.BLOCK.getOptional(Identifier.fromNamespaceAndPath("minecraft", sapling)).ifPresent((Block b) -> {
			BlockState s = b.defaultBlockState();
			if (s.canSurvive(level, this.blockPos)) {
				level.setBlock(this.blockPos, s, 3);
			}
		});
	}
}
