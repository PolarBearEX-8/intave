package de.jpx3.intave.block.fluid;

import de.jpx3.intave.block.physics.MaterialMagic;
import de.jpx3.intave.block.variant.BlockVariant;
import de.jpx3.intave.block.variant.BlockVariantRegister;
import de.jpx3.intave.klass.rewrite.PatchyAutoTranslation;
import net.minecraft.server.v1_13_R2.IBlockData;
import net.minecraft.server.v1_13_R2.TagsFluid;
import org.bukkit.Material;

@PatchyAutoTranslation
final class v13FluidResolver implements FluidResolver {
  @Override
  @PatchyAutoTranslation
  public Fluid liquidFrom(Material type, int variantIndex) {
    IBlockData blockData = (IBlockData) BlockVariantRegister.rawVariantOf(type, variantIndex);
    if (blockData == null) {
      return Dry.of();
    }
    net.minecraft.server.v1_13_R2.Fluid fluid = blockData.s();
    if (fluid == null) {
      return Dry.of();
    }
    BlockVariant variant = BlockVariantRegister.variantOf(type, variantIndex);
    boolean dry = fluid.e();
    boolean isWater = fluid.a(TagsFluid.WATER);
    boolean isLava = fluid.a(TagsFluid.LAVA);
    boolean source = fluid.d();
    Boolean fallingProperty = dry ? null : variant.propertyOf("falling");
    if (fallingProperty == null) {
      fallingProperty = false;
    }
    float height = fluid.getHeight();
    int level = MaterialMagic.isLavaOrWater(type) ? variant.propertyOf("level") : 8;
    return select(isWater, isLava, dry, source, fallingProperty, height, level);
  }
}
