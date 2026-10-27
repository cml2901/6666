package dev.villageguard.ai;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

/**
 * Если на игрока нацелены минимум трое мобов, каждый обходит его по кругу
 * и заходит со своей стороны, а не бежит гуськом.
 */
public class SurroundTargetGoal extends Goal {
	private static final double RING = 4.5;
	private final PathfinderMob mob;
	private final double speed;
	private final double angle;
	private int recalc;
	private int age;

	public SurroundTargetGoal(PathfinderMob mob, double speed) {
		this.mob = mob;
		this.speed = speed;
		this.angle = ((mob.getUUID().hashCode() & 0xFFFF) / 65536.0) * Math.PI * 2.0;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		LivingEntity t = this.mob.getTarget();
		if (!(t instanceof Player) || !t.isAlive()) {
			return false;
		}
		double d = this.mob.distanceToSqr(t);
		if (d < 49.0 || d > 576.0 || this.mob.getRandom().nextInt(10) != 0) {
			return false;
		}
		return this.mob.level()
			.getEntitiesOfClass(Mob.class, this.mob.getBoundingBox().inflate(16.0), m -> m != this.mob && m.getTarget() == t)
			.size() >= 2;
	}

	@Override
	public boolean canContinueToUse() {
		LivingEntity t = this.mob.getTarget();
		return t != null && t.isAlive() && this.age < 100 && this.mob.distanceToSqr(t) > 30.0;
	}

	@Override
	public void start() {
		this.age = 0;
		this.recalc = 0;
	}

	@Override
	public void tick() {
		LivingEntity t = this.mob.getTarget();
		if (t == null) {
			return;
		}
		this.age++;
		if (--this.recalc <= 0) {
			this.recalc = 10;
			this.mob.getNavigation().moveTo(
				t.getX() + Math.cos(this.angle) * RING, t.getY(), t.getZ() + Math.sin(this.angle) * RING, this.speed);
		}
	}

	@Override
	public void stop() {
		this.mob.getNavigation().stop();
	}
}
