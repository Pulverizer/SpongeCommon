package org.spongepowered.common.data.provider.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BlockDirectionalSetProvider extends BlockStateDataProvider<Set<Direction>> {

    private final Map<Direction, BooleanProperty> sides;

    BlockDirectionalSetProvider(Key<? extends Value<Set<Direction>>> key, Class<? extends Block> blockType,
            Map<Direction, BooleanProperty> sides) {
        super(key, blockType);
        this.sides = sides;
    }

    @Override
    protected Optional<Set<Direction>> getFrom(BlockState dataHolder) {
        final Set<Direction> directions = new HashSet<>();
        for (final Map.Entry<Direction, BooleanProperty> entry : this.sides.entrySet()) {
            if (dataHolder.get(entry.getValue())) {
                directions.add(entry.getKey());
            }
        }
        return Optional.of(directions);
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Set<Direction> value) {
        for (final Map.Entry<Direction, BooleanProperty> entry : this.sides.entrySet()) {
            dataHolder = dataHolder.with(entry.getValue(), value.contains(entry.getKey()));
        }
        return Optional.of(dataHolder);
    }
}