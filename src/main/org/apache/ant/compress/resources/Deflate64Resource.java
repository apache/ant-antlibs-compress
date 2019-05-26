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

import org.apache.ant.compress.util.Deflate64StreamFactory;
import org.apache.tools.ant.types.ResourceCollection;

/**
 * A DEFLATE64 compressed resource.
 * @since Apache Compress Antlib 1.6
 */
public final class Deflate64Resource extends CommonsCompressCompressorResource {
    private boolean zlibHeader = true;
    private static final String NAME = "DEFLATE64";

    public Deflate64Resource() {
        super(NAME, new Deflate64StreamFactory());
    }

    public Deflate64Resource(ResourceCollection other) {
        super(NAME, new Deflate64StreamFactory(), other);
    }

}
