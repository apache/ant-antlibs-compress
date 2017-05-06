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
import org.apache.commons.compress.compressors.z.ZCompressorInputStream;

/**
 * Creates streams for the traditional Unix compress format.
 * @since Apache Compress Antlib 1.4
 */
public class ZStreamFactory implements CompressorStreamFactory {

    /**
     * @param stream the stream to read from, should be buffered
     */
    @Override
    public CompressorInputStream getCompressorStream(InputStream stream)
        throws IOException {
        return new ZCompressorInputStream(stream);
    }

    /**
     * Not implemented.
     */
    @Override
    public CompressorOutputStream getCompressorStream(OutputStream stream)
        throws IOException {
        throw new UnsupportedOperationException();
    }
}
