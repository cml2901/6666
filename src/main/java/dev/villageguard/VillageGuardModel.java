package dev.villageguard;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * Человеческая модель стража: своя раскладка текстуры 64x64 (как у игрока, у каждой руки и ноги
 * своя область) и собственная анимация поверх ванильной ходьбы и удара мечом.
 */
public class VillageGuardModel extends HumanoidModel<HumanoidRenderState> {
	public VillageGuardModel(ModelPart root) {
		super(root);
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		PartDefinition root = mesh.getRoot();

		PartDefinition head = root.addOrReplaceChild("head",
			CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
			PartPose.ZERO);
		// "hat" в HumanoidModel лежит внутри головы и двигается вместе с ней
		head.addOrReplaceChild("hat",
			CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
			PartPose.ZERO);
		root.addOrReplaceChild("body",
			CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
			PartPose.ZERO);
		root.addOrReplaceChild("right_arm",
			CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
			PartPose.offset(-5.0F, 2.0F, 0.0F));
		root.addOrReplaceChild("left_arm",
			CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
			PartPose.offset(5.0F, 2.0F, 0.0F));
		root.addOrReplaceChild("right_leg",
			CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
			PartPose.offset(-1.9F, 12.0F, 0.0F));
		root.addOrReplaceChild("left_leg",
			CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
			PartPose.offset(1.9F, 12.0F, 0.0F));

		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(HumanoidRenderState state) {
		super.setupAnim(state);

		float t = state.ageInTicks;
		float moving = Math.min(1.0F, state.walkAnimationSpeed * 4.0F);
		float still = 1.0F - moving;

		// Дыхание: плечи слегка поднимаются и опускаются, пока страж стоит
		float breath = (float) Math.sin(t * 0.09F) * 0.03F * still;
		this.body.xRot += breath;
		this.head.xRot += breath * 0.5F;

		// Руки мягко покачиваются
		this.leftArm.zRot -= (float) Math.cos(t * 0.09F) * 0.04F * still + 0.04F;
		this.leftArm.xRot -= (float) Math.sin(t * 0.067F) * 0.04F * still;

		// Стойка с мечом: правая рука чуть вынесена вперёд, как у бойца
		if (state.attackTime <= 0.0F) {
			this.rightArm.xRot -= 0.35F;
			this.rightArm.zRot += 0.06F + (float) Math.cos(t * 0.09F) * 0.03F * still;
		}

		// Бег: корпус наклоняется вперёд, голова остаётся прямо
		this.body.xRot += 0.12F * moving;
		this.head.xRot -= 0.12F * moving;

	}
}
