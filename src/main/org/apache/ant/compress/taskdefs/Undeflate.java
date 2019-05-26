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

import org.apache.ant.compress.util.DeflateStreamFactory;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.apache.commons.compress.compressors.deflate.DeflateParameters;

/**
 * Expands a DEFLATE archive.
 * @since Apache Compress Antlib 1.5
 */
public final class Undeflate extends UnpackBase {

    private boolean zlibHeader = true;

    public Undeflate() {
        super(".dfl");
        setFactory(new DeflateStreamFactory() {
            @Override
            public CompressorInputStream getCompressorStream(InputStream stream)
                throws IOException {
                DeflateParameters params = new DeflateParameters();
                params.setWithZlibHeader(zlibHeader);
                return new DeflateCompressorInputStream(stream, params);
            }
        });
    }

    /**
     * Whether to expect a ZLIB header.
     *
     * <p>Default is {@code true}.</p>
     * @param zlib whether to expect a ZLIB header
     */
    public void setZlibHeader(boolean zlib) {
        zlibHeader = zlib;
    }
}
