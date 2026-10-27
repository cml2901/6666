package dev.villageguard;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/** Состояние рендера стража: номер внешности (кожа, волосы, туника). */
public class GuardRenderState extends HumanoidRenderState {
	public int variant;
}
