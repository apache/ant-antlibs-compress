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
package org.apache.ant.compress.resources;

import org.apache.ant.compress.util.LZ4StreamFactory;
import org.apache.tools.ant.types.ResourceCollection;

/**
 * An LZ4 compressed resource.
 * @since Apache Compress Antlib 1.5
 */
public final class LZ4Resource extends CommonsCompressCompressorResource {
    private static final String NAME = "LZ4";

    private final LZ4StreamFactory factory;

    public LZ4Resource() {
        super(NAME);
        setFactory(factory = new LZ4StreamFactory());
    }

    public LZ4Resource(ResourceCollection other) {
        super(NAME, other);
        setFactory(factory = new LZ4StreamFactory());
    }

    /**
     * Whether to use the "frame format".
     *
     * <p>Defaults to true.</p>
     */
    public void setFramed(boolean framed) {
        factory.setFramed(framed);
    }
}
