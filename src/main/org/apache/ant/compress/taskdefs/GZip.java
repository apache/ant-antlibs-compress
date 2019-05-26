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
import org.apache.ant.compress.resources.GZipResource;
import org.apache.ant.compress.util.GZipStreamFactory;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.apache.tools.ant.types.Resource;

/**
 * Compresses using gzip.
 */
public final class GZip extends PackBase {
    private int level = Deflater.DEFAULT_COMPRESSION;

    public GZip() {
        super(new PackBase.ResourceWrapper() {
                @Override
                public CommonsCompressCompressorResource wrap(Resource dest) {
                    return new GZipResource(dest);
                }
            });
        setFactory(new GZipStreamFactory() {
                @Override
                public CompressorOutputStream getCompressorStream(OutputStream stream)
                    throws IOException {
                    GzipParameters params = new GzipParameters();
                    params.setCompressionLevel(level);
                    return new GzipCompressorOutputStream(stream, params);
                }
            });
    }

    /**
     * Set the compression level to use.  Default is
     * Deflater.DEFAULT_COMPRESSION.
     * @param level compression level.
     * @since 1.5
     */
    public void setLevel(int level) {
        this.level = level;
    }

}
