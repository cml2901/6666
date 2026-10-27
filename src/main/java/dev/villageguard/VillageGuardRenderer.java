package dev.villageguard;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/**
 * Рендерер стража: человеческая модель со своей текстурой и анимацией,
 * ванильный слой брони (броня видна) и меч в руке.
 */
public class VillageGuardRenderer
	extends HumanoidMobRenderer<VillageGuardEntity, HumanoidRenderState, VillageGuardModel> {

	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
		VillageGuardMod.MOD_ID, "textures/entity/village_guard.png");

	public VillageGuardRenderer(EntityRendererProvider.Context context) {
		super(context, new VillageGuardModel(VillageGuardModel.createLayer().bakeRoot()), 0.5F);

		// Ванильная броня (как у зомби): железные шлем, нагрудник, поножи и ботинки видны на модели
		ArmorModelSet<HumanoidModel<HumanoidRenderState>> armor = ArmorModelSet.bake(
			ModelLayers.ZOMBIE_ARMOR,
			context.getModelSet(),
			part -> new HumanoidModel<HumanoidRenderState>(part)
		);
		this.addLayer(new HumanoidArmorLayer<>(this, armor, context.getEquipmentRenderer()));
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
