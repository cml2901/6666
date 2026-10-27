package dev.villageguard.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Любой житель (не только фермер) собирает созревший урожай и сразу сажает заново. */
public class VillagerFarmGoal extends MoveToBlockGoal {
	private final Villager villager;

	public VillagerFarmGoal(Villager villager) {
		super(villager, 0.6, 12, 2);
		this.villager = villager;
	}

	@Override
	public boolean canUse() {
		// Работаем днём, пока светло
		return !this.villager.isSleeping()
			&& this.villager.level().getMaxLocalRawBrightness(this.villager.blockPosition()) > 8
			&& super.canUse();
	}

	@Override
	protected boolean isValidTarget(LevelReader level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state);
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.isReachedTarget() || !(this.villager.level() instanceof ServerLevel level)) {
			return;
		}
		BlockState state = level.getBlockState(this.blockPos);
		if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
			level.destroyBlock(this.blockPos, true, this.villager);
			level.setBlock(this.blockPos, crop.getStateForAge(0), 3);
		}
	}
}
