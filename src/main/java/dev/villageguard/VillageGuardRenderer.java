package dev.villageguard;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/**
 * Рендерер стража: человеческая (гуманоидная) модель с носом жителя, своя текстура,
 * ванильный слой брони (броня видна) и меч в руке.
 */
public class VillageGuardRenderer
	extends HumanoidMobRenderer<VillageGuardEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {

	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
		VillageGuardMod.MOD_ID, "textures/entity/village_guard.png");

	public VillageGuardRenderer(EntityRendererProvider.Context context) {
		super(context, new HumanoidModel<HumanoidRenderState>(createLayer().bakeRoot()), 0.5F);

		// Ванильная броня (как у зомби): железные шлем, нагрудник, поножи и ботинки видны на модели
		ArmorModelSet<HumanoidModel<HumanoidRenderState>> armor = ArmorModelSet.bake(
			ModelLayers.ZOMBIE_ARMOR,
			context.getModelSet(),
			part -> new HumanoidModel<HumanoidRenderState>(part)
		);
		this.addLayer(new HumanoidArmorLayer<>(this, armor, context.getEquipmentRenderer()));
	}

	/** Обычная модель человека + большой нос жителя (текстура нарисована под стандартную развёртку 64x64). */
	private static LayerDefinition createLayer() {
		MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		mesh.getRoot().getChild("head").addOrReplaceChild(
			"nose",
			CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -4.0F, -6.0F, 2.0F, 4.0F, 2.0F),
			PartPose.ZERO
		);
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public HumanoidRenderState createRenderState() {
		return new HumanoidRenderState();
	}

	@Override
	public Identifier getTextureLocation(HumanoidRenderState state) {
		return TEXTURE;
	}
}
