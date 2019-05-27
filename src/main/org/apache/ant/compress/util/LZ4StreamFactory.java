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

package org.apache.ant.compress.util;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStream;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorOutputStream;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStream;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorOutputStream;

/**
 * Creates streams for LZ4 format.
 * @since Apache Compress Antlib 1.5
 * @see <a href="https://lz4.github.io/lz4/">LZ4 Project</a>
 */
public class LZ4StreamFactory implements CompressorWithConcatenatedStreamsFactory {

    private boolean framed = true;

    /**
     * Whether to use the "frame format".
     *
     * <p>Defaults to true.</p>
     */
    public void setFramed(boolean framed) {
        this.framed = framed;
    }

    /**
     * Whether to use the "frame format".
     */
    protected boolean isFramed() {
        return framed;
    }

    /**
     * @param stream the stream to read from, should be buffered
     */
    @Override
    public CompressorInputStream getCompressorStream(InputStream stream)
        throws IOException {
        return framed ? new FramedLZ4CompressorInputStream(stream)
            : (CompressorInputStream) new BlockLZ4CompressorInputStream(stream);
    }

    /**
     * @param stream the stream to read from, should be buffered
     * @param       decompressConcatenated
     *                          if true, decompress until the end of the
     *                          input; if false, stop after the first
     *                          stream
     */
    @Override
    public CompressorInputStream getCompressorStream(InputStream stream,
                                                     boolean decompressConcatenated)
        throws IOException {
        if (!framed && decompressConcatenated) {
            throw new UnsupportedOperationException("only the frame format supports"
                                                    + " concatenated streams");
        }
        return framed ? new FramedLZ4CompressorInputStream(stream, decompressConcatenated)
            : (CompressorInputStream) new BlockLZ4CompressorInputStream(stream);
    }

    /**
     * @param stream the stream to write to, should be buffered
     */
    @Override
    public CompressorOutputStream getCompressorStream(OutputStream stream)
        throws IOException {
        return framed ? new FramedLZ4CompressorOutputStream(stream)
            : (CompressorOutputStream) new BlockLZ4CompressorOutputStream(stream);
    }
}
