/*
 * Copyright (c) 2013-2021 Cinchapi Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cinchapi.concourse.server.cli.plugin;

import com.cinchapi.concourse.server.cli.core.OptionalArgsCli;
import com.cinchapi.concourse.server.cli.core.Options;
import com.google.common.base.CaseFormat;

/**
 * Marker class for CLIs that should be invokable from the main
 * {@link ManagePluginsCli}.
 * 
 * @author Jeff Nelson
 */
public abstract class PluginCli extends OptionalArgsCli {

    /**
     * Return the command that can be passed to the {@link ManagePluginsCli} to
     * invoke this particular cli.
     * 
     * @return the command
     */
    public static String getCommand(Class<? extends PluginCli> clazz) {
        return CaseFormat.UPPER_CAMEL
                .to(CaseFormat.LOWER_UNDERSCORE, clazz.getSimpleName())
                .split("_")[0];
    }

    /**
     * Construct a new instance.
     * 
     * @param options
     * @param args
     */
    public PluginCli(Options options, String[] args) {
        super(options, args);
    }

    /**
     * Special options for the plugin cli.
     * 
     * @author Jeff Nelson
     */
    protected static class PluginOptions extends Options {}

}
