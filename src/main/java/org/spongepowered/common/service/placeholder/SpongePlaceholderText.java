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

import org.spongepowered.api.service.placeholder.PlaceholderParser;
import org.spongepowered.api.service.placeholder.PlaceholderText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public class SpongePlaceholderText implements PlaceholderText {

    private final PlaceholderParser parser;
    @Nullable final Supplier<Object> associatedObjectSupplier;
    @Nullable private final String argument;

    public SpongePlaceholderText(PlaceholderParser parser,
            @Nullable Supplier<Object> associatedReceiverSupplier,
            @Nullable String argument) {
        this.parser = parser;
        this.associatedObjectSupplier = associatedReceiverSupplier;
        this.argument = argument;
    }

    @Override
    public PlaceholderParser getParser() {
        return this.parser;
    }

    @Override
    public Optional<Object> getAssociatedObject() {
        if (this.associatedObjectSupplier == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(this.associatedObjectSupplier.get());
    }

    @Override
    public Optional<String> getArgumentString() {
        return Optional.ofNullable(this.argument);
    }

    @Override
    public Text toText() {
        return this.parser.parse(this);
    }

}
