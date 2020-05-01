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
package org.spongepowered.common.registry.type.service.placeholder;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.service.placeholder.PlaceholderParser;
import org.spongepowered.api.service.placeholder.PlaceholderParsers;
import org.spongepowered.api.service.placeholder.PlaceholderText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.type.AbstractPrefixAlternateCatalogTypeRegistryModule;

@RegisterCatalog(PlaceholderParsers.class)
public class PlaceholderParserRegistryModule
        extends AbstractPrefixAlternateCatalogTypeRegistryModule<PlaceholderParser>
        implements SpongeAdditionalCatalogRegistryModule<PlaceholderParser> {

    public PlaceholderParserRegistryModule() {
        super("sponge");
    }

    @Override
    public boolean allowsApiRegistration() {
        return true;
    }

    @Override
    public void registerAdditionalCatalog(PlaceholderParser extraCatalog) {
        if (this.getById(extraCatalog.getId()).isPresent()) {
            throw new IllegalStateException("The ID " + extraCatalog.getId() + " has already been registered.");
        }
        this.register(extraCatalog);
    }

    @Override
    public void registerDefaults() {
        register(new PlaceholderParser() {
            @Override
            public Text parse(PlaceholderText placeholderText) {
                return placeholderText.getAssociatedObject()
                        .filter(x -> x instanceof CommandSource)
                        .<Text>map(x -> Text.of(((CommandSource) x).getName()))
                        .orElse(Text.EMPTY);
            }

            @Override
            public String getId() {
                return "sponge:name";
            }

            @Override
            public String getName() {
                return "Name";
            }
        });
        register(new PlaceholderParser() {
            @Override
            public Text parse(PlaceholderText placeholderText) {
                return Text.of(
                        placeholderText.getAssociatedObject()
                                .filter(x -> x instanceof Locatable)
                                .map(x -> ((Locatable) x).getWorld().getName())
                                .orElseGet(() -> SpongeImpl.getServer().getEntityWorld().getWorldInfo().getWorldName()));
            }

            @Override
            public String getId() {
                return "sponge:current_world";
            }

            @Override
            public String getName() {
                return "Current (or default) world";
            }
        });
    }
}
