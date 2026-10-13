package com.exemplo.luz;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/** Bloco de luz INVISIVEL. Alcance 1..48 (a luz do jogo vanilla vai ate 15; o resto vem do shader). */
public class LuzBlock extends Block {
    public static final int MAX_ALCANCE = 48;
    public static final IntProperty ALCANCE = IntProperty.of("alcance", 1, MAX_ALCANCE);
    public static final EnumProperty<DyeColor> COR = EnumProperty.of("cor", DyeColor.class);

    private static final VoxelShape FORMA =
            VoxelShapes.cuboid(5 / 16.0, 0.5 / 16.0, 5 / 16.0, 11 / 16.0, 6.5 / 16.0, 11 / 16.0);

    /** Cores de luz (RGB 0xRRGGBB). Mantenha igual ao gerar_shader.py */
    public static int rgb(DyeColor c) {
        return switch (c) {
            case WHITE -> 0xFFFFFF;
            case ORANGE -> 0xFF8C1A;
            case MAGENTA -> 0xFF33D9;
            case LIGHT_BLUE -> 0x66BFFF;
            case YELLOW -> 0xFFE64D;
            case LIME -> 0x80FF33;
            case PINK -> 0xFF8CB3;
            case GRAY -> 0x808C99;
            case LIGHT_GRAY -> 0xCCCCD9;
            case CYAN -> 0x1AE6E6;
            case PURPLE -> 0x9940FF;
            case BLUE -> 0x334DFF;
            case BROWN -> 0xCC8040;
            case GREEN -> 0x33E64D;
            case RED -> 0xFF261A;
            case BLACK -> 0x4D1A80;
        };
    }

    public LuzBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(ALCANCE, 24)
                .with(COR, DyeColor.YELLOW));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ALCANCE, COR);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) {
        return FORMA;
    }

    /** Corante na mao: muda a cor. Varinha: o ajuste e feito pelo arrasto (cliente). */
    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
                                         PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isOf(LuzAjustavel.VARINHA)) {
            return ActionResult.SUCCESS;
        }
        if (stack.getItem() instanceof DyeItem dye) {
            if (!world.isClient()) {
                world.setBlockState(pos, state.with(COR, dye.getColor()));
                if (!player.isCreative()) stack.decrement(1);
                player.sendMessage(Text.literal("Cor: " + dye.getColor().getId()), true);
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    /** Mao vazia: +1 de alcance. Shift: -1. */
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()) {
            int atual = state.get(ALCANCE);
            int novo = player.isSneaking()
                    ? (atual <= 1 ? MAX_ALCANCE : atual - 1)
                    : (atual >= MAX_ALCANCE ? 1 : atual + 1);
            world.setBlockState(pos, state.with(ALCANCE, novo));
            player.sendMessage(Text.literal("Alcance da luz: " + novo), true);
        }
        return ActionResult.SUCCESS;
    }
}
