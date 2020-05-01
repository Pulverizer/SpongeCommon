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
package org.spongepowered.common.service.placeholder;

import com.google.common.base.Preconditions;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.placeholder.PlaceholderParser;
import org.spongepowered.api.service.placeholder.PlaceholderText;

import java.util.function.Supplier;

import javax.annotation.Nullable;

public class SpongePlaceholderTextBuilder implements PlaceholderText.Builder {

    @Nullable private PlaceholderParser parser;
    @Nullable private Supplier<Object> associatedObjectSupplier;
    @Nullable private String argument = null;

    @Override
    public PlaceholderText.Builder setParser(PlaceholderParser parser) {
        this.parser = Preconditions.checkNotNull(parser, "parser cannot be null");
        return this;
    }

    @Override
    public PlaceholderText.Builder setAssociatedObject(@Nullable Object associatedObject) {
        if (associatedObject == null) {
            this.associatedObjectSupplier = null;
        } else if (associatedObject instanceof Player) {
            return this.setAssociatedObject((Player) associatedObject);
        } else {
            this.associatedObjectSupplier = () -> associatedObject;
        }

        return this;
    }

    @Override
    public PlaceholderText.Builder setAssociatedObject(@Nullable Supplier<Object> associatedObjectSupplier) {
        this.associatedObjectSupplier = associatedObjectSupplier;
        return this;
    }

    @Override
    public PlaceholderText.Builder setArgumentString(@Nullable String argument) {
        this.argument = argument == null || argument.isEmpty() ? null : argument;
        return this;
    }

    @Override
    public PlaceholderText build() throws IllegalStateException {
        if (this.parser == null) {
            throw new IllegalStateException("parser cannot be null");
        }
        return new SpongePlaceholderText(this.parser, this.associatedObjectSupplier, this.argument);
    }

    @Override
    public PlaceholderText.Builder from(PlaceholderText value) {
        Preconditions.checkArgument(value instanceof SpongePlaceholderText, "Must supply a SpongePlaceholderText");
        this.parser = value.getParser();
        this.associatedObjectSupplier = ((SpongePlaceholderText) value).associatedObjectSupplier;
        this.argument = value.getArgumentString().orElse(null);
        return this;
    }

    @Override
    public PlaceholderText.Builder reset() {
        this.argument = null;
        this.parser = null;
        this.associatedObjectSupplier = null;
        return this;
    }

}
