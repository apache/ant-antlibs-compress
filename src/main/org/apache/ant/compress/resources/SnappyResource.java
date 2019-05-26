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

import org.apache.ant.compress.util.SnappyStreamFactory;
import org.apache.tools.ant.types.ResourceCollection;

/**
 * An Snappy compressed resource.
 * @since Apache Compress Antlib 1.4
 */
public final class SnappyResource extends CommonsCompressCompressorResource {
    private static final String NAME = "Snappy";

    private final SnappyStreamFactory factory;

    public SnappyResource() {
        super(NAME);
        setFactory(factory = new SnappyStreamFactory());
    }

    public SnappyResource(ResourceCollection other) {
        super(NAME, other);
        setFactory(factory = new SnappyStreamFactory());
    }

    /**
     * Whether to use the "framing format".
     *
     * <p>Defaults to true.</p>
     */
    public void setFramed(boolean framed) {
        factory.setFramed(framed);
    }
}
