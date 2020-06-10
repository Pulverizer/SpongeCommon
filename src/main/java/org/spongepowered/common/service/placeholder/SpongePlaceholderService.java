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
import org.spongepowered.api.service.placeholder.PlaceholderService;
import org.spongepowered.api.service.placeholder.PlaceholderText;
import org.spongepowered.common.SpongeImpl;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public class SpongePlaceholderService implements PlaceholderService {

    @Override
    public Optional<PlaceholderText> parse(String token) {
        return this.parseInternal(token, null, null);
    }

    @Override
    public Optional<PlaceholderText> parse(String token, Supplier<Object> associatedObjectSupplier) {
        return this.parse(token, null, associatedObjectSupplier);
    }

    @Override
    public Optional<PlaceholderText> parse(String token, Object associatedObject) {
        return this.parseInternal(token, null, associatedObject);
    }

    @Override
    public Optional<PlaceholderText> parse(String token, String argumentString) {
        return this.parseInternal(token, argumentString, null);
    }

    @Override
    public Optional<PlaceholderText> parse(String token, String argumentString, Object associatedObject) {
        return this.parseInternal(token, argumentString, associatedObject);
    }

    @Override
    public Optional<PlaceholderText> parse(String token, String argumentString, Supplier<Object> associatedObjectSupplier) {
        return this.parseInternal(token, argumentString, associatedObjectSupplier);
    }

    @Override
    public Optional<PlaceholderParser> getParser(String token) {
        return SpongeImpl.getRegistry().getType(PlaceholderParser.class, token);
    }

    @Override
    public PlaceholderText.Builder placeholderBuilder() {
        return new SpongePlaceholderTextBuilder();
    }

    private Optional<PlaceholderText> parseInternal(String token, @Nullable String argumentString, @Nullable Object messageReceiver) {
        return getParser(token)
                .map(x -> placeholderBuilder().setAssociatedObject(messageReceiver).setArgumentString(argumentString).setParser(x).build());
    }

    private Optional<PlaceholderText> parseInternal(String token, @Nullable String argumentString, @Nullable Supplier<Object> supplier) {
        return getParser(token)
                .map(x -> placeholderBuilder().setAssociatedObject(supplier).setArgumentString(argumentString).setParser(x).build());
    }

}
