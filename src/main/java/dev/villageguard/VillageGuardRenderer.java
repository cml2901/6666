package dev.villageguard;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.Identifier;

/**
 * Рендерер стража: человеческая модель со своей текстурой и анимацией,
 * ванильный слой брони (броня видна) и меч в руке.
 */
public class VillageGuardRenderer
	extends HumanoidMobRenderer<VillageGuardEntity, GuardRenderState, VillageGuardModel> {

	/** Четыре внешности; какая именно — зависит от UUID стража, так что стражи не одинаковые. */
	private static final Identifier[] TEXTURES = {
		texture("village_guard"), texture("village_guard_1"), texture("village_guard_2"), texture("village_guard_3")
	};

	private static Identifier texture(String name) {
		return Identifier.fromNamespaceAndPath(VillageGuardMod.MOD_ID, "textures/entity/" + name + ".png");
	}

	public VillageGuardRenderer(EntityRendererProvider.Context context) {
		super(context, new VillageGuardModel(VillageGuardModel.createLayer().bakeRoot()), 0.5F);

		// Ванильная броня (как у зомби): железные шлем, нагрудник, поножи и ботинки видны на модели
		ArmorModelSet<HumanoidModel<GuardRenderState>> armor = ArmorModelSet.bake(
			ModelLayers.ZOMBIE_ARMOR,
			context.getModelSet(),
			part -> new HumanoidModel<GuardRenderState>(part)
		);
		this.addLayer(new HumanoidArmorLayer<>(this, armor, context.getEquipmentRenderer()));
	}

	@Override
	public GuardRenderState createRenderState() {
		return new GuardRenderState();
	}

	@Override
	public void extractRenderState(VillageGuardEntity entity, GuardRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.variant = Math.floorMod(entity.getUUID().hashCode(), TEXTURES.length);
	}

	@Override
	public Identifier getTextureLocation(GuardRenderState state) {
		return TEXTURES[state.variant];
	}
}
