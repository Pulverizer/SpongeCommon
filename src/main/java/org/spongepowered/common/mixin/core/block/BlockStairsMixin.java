/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.mixin.core.block;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDirectionalData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutablePortionData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableStairShapeData;
import org.spongepowered.api.data.type.PortionType;
import org.spongepowered.api.data.type.StairShape;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDirectionalData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongePortionData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeStairShapeData;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

@Mixin(BlockStairs.class)
public abstract class BlockStairsMixin extends BlockMixin {

    @SuppressWarnings("RedundantTypeArguments") // some JDK's can fail to compile without the explicit type generics
    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> bridge$getManipulators(final IBlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>of(impl$getStairShapeFor(blockState), impl$getPortionTypeFor(blockState),
                impl$getDirectionalData(blockState));
    }

    @Override
    public boolean bridge$supports(final Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableStairShapeData.class.isAssignableFrom(immutable) || ImmutablePortionData.class.isAssignableFrom(immutable)
                || ImmutableDirectionalData.class.isAssignableFrom(immutable);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Optional<BlockState> bridge$getStateWithData(final IBlockState blockState, final ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableStairShapeData) {
            final BlockStairs.EnumShape stairShapeType = (BlockStairs.EnumShape) (Object) ((ImmutableStairShapeData) manipulator).type().get();
            return Optional.of((BlockState) blockState.withProperty(BlockStairs.SHAPE, stairShapeType));
        }
        if (manipulator instanceof ImmutablePortionData) {
            final PortionType portionType = ((ImmutablePortionData) manipulator).type().get();
            return Optional.of((BlockState) blockState.withProperty(BlockStairs.HALF, impl$convertType((BlockSlab.EnumBlockHalf) (Object) portionType)));
        }
        if (manipulator instanceof ImmutableDirectionalData) {
            final Direction dir = Constants.DirectionFunctions.checkDirectionToHorizontal(((ImmutableDirectionalData) manipulator).direction().get());
            return Optional.of((BlockState) blockState.withProperty(BlockStairs.FACING, Constants.DirectionFunctions.getFor(dir)));
        }
        return super.bridge$getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> bridge$getStateWithValue(final IBlockState blockState, final Key<? extends BaseValue<E>> key, final E value) {
        if (key.equals(Keys.STAIR_SHAPE)) {
            final BlockStairs.EnumShape stairShapeType = (BlockStairs.EnumShape) value;
            return Optional.of((BlockState) blockState.withProperty(BlockStairs.SHAPE, stairShapeType));
        }
        if (key.equals(Keys.PORTION_TYPE)) {
            return Optional.of((BlockState) blockState.withProperty(BlockStairs.HALF, impl$convertType((BlockSlab.EnumBlockHalf) value)));
        }
        if (key.equals(Keys.DIRECTION)) {
            final Direction dir = Constants.DirectionFunctions.checkDirectionToHorizontal((Direction) value);
            return Optional.of((BlockState) blockState.withProperty(BlockStairs.FACING, Constants.DirectionFunctions.getFor(dir)));
        }
        return super.bridge$getStateWithValue(blockState, key, value);
    }

    @SuppressWarnings("ConstantConditions")
    private ImmutableStairShapeData impl$getStairShapeFor(final IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeStairShapeData.class,
                (StairShape) (Object) blockState.getValue(BlockStairs.SHAPE));
    }

    private ImmutablePortionData impl$getPortionTypeFor(final IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongePortionData.class,
                impl$convertType(blockState.getValue(BlockStairs.HALF)));
    }

    private ImmutableDirectionalData impl$getDirectionalData(final IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeDirectionalData.class,
                Constants.DirectionFunctions.getFor(blockState.getValue(BlockStairs.FACING)));
    }

    @SuppressWarnings("ConstantConditions")
    private PortionType impl$convertType(final BlockStairs.EnumHalf type) {
        return (PortionType) (Object) BlockSlab.EnumBlockHalf.valueOf(type.getName().toUpperCase());
    }

    private BlockStairs.EnumHalf impl$convertType(final BlockSlab.EnumBlockHalf type) {
        return BlockStairs.EnumHalf.valueOf(type.getName().toUpperCase());
    }
}
