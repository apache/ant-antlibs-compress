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

import java.io.File;
import java.io.IOException;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;

/**
 * Creates streams for the supported compression formats that may take
 * advantage of writing to/reading from a file.
 *
 * @since Apache Compress Antlib 1.1
 */
public interface FileAwareCompressorStreamFactory
    extends CompressorStreamFactory {

    /**
     * @param file the file to read from
     */
    CompressorInputStream getCompressorInputStream(File file)
        throws IOException;


    /**
     * @param file the file to write to
     */
    CompressorOutputStream getCompressorOutputStream(File file)
        throws IOException;

}
