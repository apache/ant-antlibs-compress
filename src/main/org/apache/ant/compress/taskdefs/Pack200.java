/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.ant.compress.taskdefs;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.ant.compress.resources.CommonsCompressCompressorResource;
import org.apache.ant.compress.resources.Pack200Resource;
import org.apache.ant.compress.util.CompressorStreamFactory;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.pack200.Pack200CompressorOutputStream;
import org.apache.commons.compress.compressors.pack200.Pack200Strategy;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Resource;

/**
 * Compresses using pack200.
 * @since Apache Compress Antlib 1.1
 */
public final class Pack200 extends PackBase {

    private Pack200StrategyEnum strategy = Pack200StrategyEnum.IN_MEMORY;
    private final Map<String, String> properties = new HashMap<>();

    public Pack200() {
        super(new PackBase.ResourceWrapper() {
                @Override
                public CommonsCompressCompressorResource wrap(Resource dest) {
                    return new Pack200Resource(dest);
                }
            });
        setFactory(new CompressorStreamFactory() {
                @Override
                public CompressorOutputStream
                    getCompressorStream(OutputStream stream)
                    throws IOException {
                    return new Pack200CompressorOutputStream(stream,
                                                             strategy
                                                             .getStrategy(),
                                                             properties);
                }
                @Override
                public CompressorInputStream
                    getCompressorStream(InputStream stream)
                    throws IOException {
                    throw new UnsupportedOperationException();
                }
            });
    }

    /**
     * Whether to cache archive data in memory (the default) or a
     * temporary file.
     */
    public void setPack200Strategy(Pack200StrategyEnum s) {
        strategy = s;
    }

    /**
     * Sets a property for the Pack200 packer.
     */
    public void addConfiguredProperty(Environment.Variable prop) {
        prop.validate();
        properties.put(prop.getKey(), prop.getValue());
    }

    /**
     * Pack200Strategy to use: cache in memory or use a temporary file.
     *
     * @since Commons Compress 1.1
     */
    public static final class Pack200StrategyEnum extends EnumeratedAttribute {
        private static final Map STRATEGIES = new HashMap();
        private static final String IN_MEMORY_KEY = "in-memory";
        private static final String TEMP_FILE_KEY = "temp-file";
        static {
            STRATEGIES.put(IN_MEMORY_KEY, Pack200Strategy.IN_MEMORY);
            STRATEGIES.put(TEMP_FILE_KEY, Pack200Strategy.TEMP_FILE);
        }

        @Override
        public String[] getValues() {
            return new String[] {IN_MEMORY_KEY, TEMP_FILE_KEY};
        }

        static final Pack200StrategyEnum IN_MEMORY =
            new Pack200StrategyEnum(IN_MEMORY_KEY);

        private Pack200StrategyEnum(String name) {
            setValue(name);
        }

        public Pack200StrategyEnum() {
        }

        public Pack200Strategy getStrategy() {
            return (Pack200Strategy) STRATEGIES.get(getValue());
        }
    }
}
