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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.pack200.Pack200CompressorInputStream;
import org.apache.commons.compress.compressors.pack200.Pack200CompressorOutputStream;

/**
 * Creates streams for the pack200 format.
 * @since Apache Compress Antlib 1.1
 */
public class Pack200StreamFactory implements FileAwareCompressorStreamFactory {

    /**
     * @param stream the stream to read from, should be buffered
     */
    @Override
    public CompressorInputStream getCompressorStream(InputStream stream)
        throws IOException {
        return new Pack200CompressorInputStream(stream);
    }

    /**
     * @param stream the stream to write to, should be buffered
     */
    @Override
    public CompressorOutputStream getCompressorStream(OutputStream stream)
        throws IOException {
        return new Pack200CompressorOutputStream(stream);
    }

    /**
     * @param file the file to read from
     */
    @Override
    public CompressorInputStream getCompressorInputStream(File file)
        throws IOException {
        return new Pack200CompressorInputStream(file);
    }

    /**
     * @param file the file to write to
     */
    @Override
    public CompressorOutputStream getCompressorOutputStream(File file)
        throws IOException {
        return
            getCompressorStream(new BufferedOutputStream(new FileOutputStream(file)));
    }
}
