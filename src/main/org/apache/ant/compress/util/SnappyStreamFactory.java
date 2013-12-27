/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStream;
import org.apache.commons.compress.compressors.snappy.SnappyCompressorInputStream;

/**
 * Creates streams for the standalone Snappy format.
 * @since Apache Compress Antlib 1.4
 * @see <a href="http://code.google.com/p/snappy/">Snappy Project</a>
 */
public class SnappyStreamFactory implements CompressorStreamFactory {

    private boolean framed = true;

    /**
     * Whether to use the "framing format".
     *
     * <p>Defaults to true.</p>
     */
    public void setFramed(boolean framed) {
        this.framed = framed;
    }

    /**
     * @param stream the stream to read from, should be buffered
     */
    public CompressorInputStream getCompressorStream(InputStream stream)
        throws IOException {
        return framed ? new FramedSnappyCompressorInputStream(stream)
            : (CompressorInputStream) new SnappyCompressorInputStream(stream);
    }

    /**
     * Not implemented.
     */
    public CompressorOutputStream getCompressorStream(OutputStream stream)
        throws IOException {
        throw new UnsupportedOperationException();
    }
}
