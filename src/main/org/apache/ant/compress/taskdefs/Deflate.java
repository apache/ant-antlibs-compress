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

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;

import org.apache.ant.compress.resources.CommonsCompressCompressorResource;
import org.apache.ant.compress.resources.DeflateResource;
import org.apache.ant.compress.util.DeflateStreamFactory;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream;
import org.apache.commons.compress.compressors.deflate.DeflateParameters;
import org.apache.tools.ant.types.Resource;

/**
 * Compresses using deflate.
 * @since Apache Compress Antlib 1.5
 */
public final class Deflate extends PackBase {
    private boolean zlibHeader = true;
    private int level = Deflater.DEFAULT_COMPRESSION;

    public Deflate() {
        super(new PackBase.ResourceWrapper() {
                @Override
                public CommonsCompressCompressorResource wrap(Resource dest) {
                    return new DeflateResource(dest);
                }
            });
        setFactory(new DeflateStreamFactory() {
                @Override
                public CompressorOutputStream getCompressorStream(OutputStream stream)
                    throws IOException {
                    DeflateParameters params = new DeflateParameters();
                    params.setCompressionLevel(level);
                    params.setWithZlibHeader(zlibHeader);
                    return new DeflateCompressorOutputStream(stream, params);
                }
            });
    }

    /**
     * Set the compression level to use.  Default is
     * Deflater.DEFAULT_COMPRESSION.
     * @param level compression level.
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Whether to write a ZLIB header.
     *
     * <p>Default is {@code true}.</p>
     * @param zlib whether to write a ZLIB header
     */
    public void setZlibHeader(boolean zlib) {
        zlibHeader = zlib;
    }
}
