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
package com.cinchapi.concourse.server.cli;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.cinchapi.common.base.CheckedExceptions;
import com.cinchapi.common.reflect.Reflection;
import com.cinchapi.common.unsafe.RuntimeDynamics;
import com.cinchapi.concourse.server.cli.core.CommandLineInterfaceInformation;
import com.cinchapi.concourse.server.cli.env.EnvironmentCli;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * A management CLI to add/modify/remove Concourse Server environments.
 * 
 * @author Jeff Nelson
 */
public class ManageEnvironmentsCli {

    static {
        Reflections.log = null;
    }

    /**
     * Run the program...
     * 
     * @param args
     */
    public static void main(String... args) {
        // NOTE: We only use JCommaner here to get the usage message
        JCommander parser = new JCommander(new Object());
        parser.setProgramName("environment");

        // Reflectively get all the commands that can be used for environment
        // management.
        Map<String, Class<? extends EnvironmentCli>> commands = Maps
                .newHashMap();
        Reflections reflections = new Reflections(
                ManageEnvironmentsCli.class.getPackage().getName());
        Set<Class<? extends EnvironmentCli>> classes = Sets
                .newTreeSet((c1, c2) -> c1.getName().compareTo(c2.getName()));
        classes.addAll(reflections.getSubTypesOf(EnvironmentCli.class));
        classes.forEach((clazz) -> {
            // This is over engineering at its finest. The logic below use a ton
            // of reflection hacks to properly configure JCommander
            // auto-magically so that every CLI that extends EnvironmentCli just
            // works. Normally, I wouldn't recommend suffering the performance
            // hit that this requires for developer convenience, but CLIs are
            // short lived and run in a separate jvm so its okay in this case :)
            // - Jeff Nelson
            String command = EnvironmentCli.getCommand(clazz);
            commands.put(command, clazz);
            String description = command + " an environment";
            for (Annotation annotation : clazz.getDeclaredAnnotations()) {
                if(annotation
                        .annotationType() == CommandLineInterfaceInformation.class) {
                    description = ((CommandLineInterfaceInformation) annotation)
                            .description();
                    break;
                }
            }
            final String _description = description;
            Parameters annotation = new Parameters() {

                @Override
                public Class<? extends Annotation> annotationType() {
                    return Parameters.class;
                }

                @Override
                public String resourceBundle() {
                    return "";
                }

                @Override
                public String separators() {
                    return null;
                }

                @Override
                public String optionPrefixes() {
                    return null;
                }

                @Override
                public String commandDescription() {
                    return _description;
                }

                @Override
                public String commandDescriptionKey() {
                    return null;
                }

                @Override
                public String[] commandNames() {
                    return null;
                }

            };
            try {
                Object object = RuntimeDynamics.newAnonymousObject();
                Method method = Class.class.getDeclaredMethod("annotationData");
                method.setAccessible(true);
                Object annotationData = method.invoke(object.getClass());
                Field field = annotationData.getClass()
                        .getDeclaredField("annotations");
                field.setAccessible(true);
                Map<Class<? extends Annotation>, Annotation> annotations = Maps
                        .newHashMapWithExpectedSize(1);
                annotations.put(Parameters.class, annotation);
                field.set(annotationData, annotations);
                parser.addCommand(command, object);
            }
            catch (ReflectiveOperationException e) {
                throw CheckedExceptions.throwAsRuntimeException(e);
            }
        });
        try {
            parser.parse(args);
        }
        catch (ParameterException e) {
            // We're not using JCommander properly anyway, so just ignore any
            // exceptions it throws.
        }
        String command = args.length > 0 ? args[0].toLowerCase() : null;
        args = args.length > 1
                ? (String[]) Arrays.copyOfRange(args, 1, args.length)
                : new String[] {};
        Class<?> clazz = command != null ? commands.get(command) : null;
        if(clazz != null) {
            EnvironmentCli cli = (EnvironmentCli) Reflection.newInstance(clazz,
                    new Object[] { args });
            cli.run();
        }
        else {
            parser.usage();
        }
    }

}
