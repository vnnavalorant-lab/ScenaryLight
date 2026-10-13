package com.exemplo.luz;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Editor visual do alcance (estilo "gizmo" de volume):
 *  - Segurando a Varinha de Luz e mirando num bloco de luz, aparece uma esfera de
 *    particulas (3 aneis) com o raio atual.
 *  - Segure o botao direito mirando no bloco e AFASTE a mira dele: o raio cresce.
 *    Aproxime: diminui. Uma haste com ponta brilhante mostra a "alca" sendo puxada.
 */
public class LuzAjustavelClient implements ClientModInitializer {
    private static final double SENSIBILIDADE = 1.5;

    private static BlockPos selecionado = null;
    private static int alcanceInicial = 0;
    private static double afastamentoInicial = 0;
    private static int ultimoEnviado = -1;
    private static int tick = 0;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(LuzAjustavelClient::aoTick);
    }

    private static void aoTick(MinecraftClient c) {
        if (c.player == null || c.world == null) {
            selecionado = null;
            return;
        }
        ItemStack mao = c.player.getMainHandStack();
        boolean comVarinha = mao.isOf(LuzAjustavel.VARINHA);
        boolean comBloco = mao.isOf(LuzAjustavel.LUZ_ITEM);
        if (!comVarinha) selecionado = null;
        if (!comVarinha && !comBloco) return;

        tick++;
        if (tick % 10 == 0) marcarBlocosProximos(c);
        if (!comVarinha) return;

        HitResult mira = c.crosshairTarget;
        BlockPos naMira = null;
        if (mira instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK
                && c.world.getBlockState(b.getBlockPos()).isOf(LuzAjustavel.LUZ_BLOCK)) {
            naMira = b.getBlockPos();
        }

        boolean segurando = c.options.useKey.isPressed();
        if (segurando) {
            if (selecionado == null && naMira != null) {
                selecionado = naMira;
                BlockState s = c.world.getBlockState(selecionado);
                alcanceInicial = s.get(LuzBlock.ALCANCE);
                afastamentoInicial = afastamento(c, Vec3d.ofCenter(selecionado));
                ultimoEnviado = alcanceInicial;
            }
        } else {
            selecionado = null;
        }

        BlockPos alvo = selecionado != null ? selecionado : naMira;
        if (alvo == null) return;
        BlockState estado = c.world.getBlockState(alvo);
        if (!estado.isOf(LuzAjustavel.LUZ_BLOCK)) {
            selecionado = null;
            return;
        }

        Vec3d centro = Vec3d.ofCenter(alvo);
        int raioExibido = estado.get(LuzBlock.ALCANCE);
        Vec3d ponta = null;

        if (selecionado != null) {
            double delta = (afastamento(c, centro) - afastamentoInicial) * SENSIBILIDADE;
            int novo = (int) Math.round(alcanceInicial + delta);
            novo = Math.max(1, Math.min(LuzBlock.MAX_ALCANCE, novo));
            raioExibido = novo; // mostra na hora, sem esperar o servidor
            if (novo != ultimoEnviado) {
                ultimoEnviado = novo;
                ClientPlayNetworking.send(new AlcancePayload(alvo, novo));
            }
            ponta = pontaDaHaste(c, centro, novo);
        }

        if (tick % 3 == 0) {
            int cor = LuzBlock.rgb(estado.get(LuzBlock.COR));
            desenharEsfera(c, centro, raioExibido, cor);
            if (ponta != null) desenharHaste(c, centro, ponta, cor);
        }
    }

    /** Distancia entre o centro e o ponto do raio de visao no plano perpendicular a mira. */
    private static double afastamento(MinecraftClient c, Vec3d centro) {
        Vec3d olho = c.player.getCameraPosVec(1.0f);
        Vec3d dir = c.player.getRotationVec(1.0f);
        double t = centro.subtract(olho).dotProduct(dir);
        Vec3d ponto = olho.add(dir.multiply(t));
        return ponto.distanceTo(centro);
    }

    private static Vec3d pontaDaHaste(MinecraftClient c, Vec3d centro, int raio) {
        Vec3d olho = c.player.getCameraPosVec(1.0f);
        Vec3d dir = c.player.getRotationVec(1.0f);
        double t = centro.subtract(olho).dotProduct(dir);
        Vec3d lateral = olho.add(dir.multiply(t)).subtract(centro);
        if (lateral.length() < 0.05) {
            lateral = dir.crossProduct(new Vec3d(0, 1, 0));
            if (lateral.length() < 0.05) lateral = new Vec3d(1, 0, 0);
        }
        return centro.add(lateral.normalize().multiply(raio));
    }

    private static void desenharEsfera(MinecraftClient c, Vec3d o, double raio, int cor) {
        int n = (int) Math.max(24, Math.min(160, raio * 5));
        DustParticleEffect fx = new DustParticleEffect(cor, 1.0f);
        for (int i = 0; i < n; i++) {
            double a = 2 * Math.PI * i / n;
            double cs = Math.cos(a) * raio;
            double sn = Math.sin(a) * raio;
            c.particleManager.addParticle(fx, o.x + cs, o.y + sn, o.z, 0, 0, 0); // anel XY
            c.particleManager.addParticle(fx, o.x + cs, o.y, o.z + sn, 0, 0, 0); // anel XZ
            c.particleManager.addParticle(fx, o.x, o.y + cs, o.z + sn, 0, 0, 0); // anel YZ
        }
    }

    private static void desenharHaste(MinecraftClient c, Vec3d de, Vec3d ate, int cor) {
        DustParticleEffect fx = new DustParticleEffect(cor, 1.2f);
        Vec3d v = ate.subtract(de);
        int passos = (int) Math.max(2, v.length() * 2);
        for (int i = 0; i <= passos; i++) {
            Vec3d p = de.add(v.multiply((double) i / passos));
            c.particleManager.addParticle(fx, p.x, p.y, p.z, 0, 0, 0);
        }
        c.particleManager.addParticle(ParticleTypes.END_ROD, ate.x, ate.y, ate.z, 0, 0, 0);
    }

    /** Como o bloco e invisivel, mostra uma faisca colorida onde ha blocos de luz por perto. */
    private static void marcarBlocosProximos(MinecraftClient c) {
        BlockPos base = c.player.getBlockPos();
        BlockPos.Mutable p = new BlockPos.Mutable();
        int r = 12;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    p.set(base.getX() + dx, base.getY() + dy, base.getZ() + dz);
                    BlockState s = c.world.getBlockState(p);
                    if (s.isOf(LuzAjustavel.LUZ_BLOCK)) {
                        int cor = LuzBlock.rgb(s.get(LuzBlock.COR));
                        c.particleManager.addParticle(new DustParticleEffect(cor, 1.5f),
                                p.getX() + 0.5, p.getY() + 0.4, p.getZ() + 0.5, 0, 0, 0);
                    }
                }
            }
        }
    }
}
