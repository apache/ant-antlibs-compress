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

import org.apache.commons.compress.compressors.CompressorInputStream;

/**
 * Creates input streams for the compression formats that support
 * concatenated streams.
 * @since Apache Compress Antlib 1.2
 */
public interface CompressorWithConcatenatedStreamsFactory
    extends CompressorStreamFactory {

    /**
     * @param stream the stream to read from, should be buffered
     * @param       decompressConcatenated
     *                          if true, decompress until the end of the
     *                          input; if false, stop after the first
     *                          stream
     */
    CompressorInputStream getCompressorStream(InputStream stream,
                                              boolean decompressConcatenated)
        throws IOException;
}
