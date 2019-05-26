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

import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;

/**
 * Creates streams for the supported archive formats that may take
 * advantage of writing to/reading from a file.
 *
 * @since Apache Compress Antlib 1.1
 */
public interface FileAwareArchiveStreamFactory extends ArchiveStreamFactory {
    /**
     * @param file the file to read from
     * @param encoding the encoding of the entry names, ignored by all
     * formats except zip
     */
    ArchiveInputStream getArchiveInputStream(File file,
                                             String encoding)
        throws IOException;


    /**
     * @param file the file to write to
     * @param encoding the encoding of the entry names, ignored by all
     * formats except zip
     */
    ArchiveOutputStream getArchiveOutputStream(File file,
                                               String encoding)
        throws IOException;

}
