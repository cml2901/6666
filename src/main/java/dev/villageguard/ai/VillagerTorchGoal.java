package dev.villageguard.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;

/** Житель сам ставит факелы в тёмных местах рядом, чтобы рядом с деревней не спавнились мобы. */
public class VillagerTorchGoal extends MoveToBlockGoal {
	private final Villager villager;

	public VillagerTorchGoal(Villager villager) {
		super(villager, 0.6, 14, 3);
		this.villager = villager;
	}

	@Override
	public boolean canUse() {
		return !this.villager.isSleeping() && super.canUse();
	}

	@Override
	protected boolean isValidTarget(LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		return level.getBlockState(pos).isAir()
			&& level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
			// только свет от блоков (небо не считаем): 0 = здесь могут спавниться монстры
			&& level.getMaxLocalRawBrightness(pos, 15) <= 0;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.isReachedTarget() && this.villager.level() instanceof ServerLevel level
			&& isValidTarget(level, this.blockPos)) {
			level.setBlock(this.blockPos, Blocks.TORCH.defaultBlockState(), 3);
		}
	}
}
