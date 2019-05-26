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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

public class ZipStreamFactory implements FileAwareArchiveStreamFactory {

    /**
     * @param stream the stream to read from, should be buffered
     * @param encoding the encoding of the entry names
     */
    @Override
    public ArchiveInputStream getArchiveStream(InputStream stream,
                                               String encoding)
        throws IOException {
        return new ZipArchiveInputStream(stream, encoding, true);
    }

    /**
     * @param stream the stream to write to, should be buffered
     * @param encoding the encoding of the entry names
     */
    @Override
    public ArchiveOutputStream getArchiveStream(OutputStream stream,
                                                String encoding)
        throws IOException {
        ZipArchiveOutputStream o = new ZipArchiveOutputStream(stream);
        o.setEncoding(encoding);
        return o;
    }

    /**
     * @param file the file to read from
     * @param encoding the encoding of the entry names
     */
    @Override
    public ArchiveInputStream getArchiveInputStream(File file,
                                                    String encoding)
        throws IOException {
        return
            getArchiveStream(new BufferedInputStream(new FileInputStream(file)),
                             encoding);
    }

    /**
     * @param file the file to write to
     * @param encoding the encoding of the entry names
     */
    @Override
    public ArchiveOutputStream getArchiveOutputStream(File file,
                                                      String encoding)
        throws IOException {
        ZipArchiveOutputStream o = new ZipArchiveOutputStream(file);
        o.setEncoding(encoding);
        return o;
    }
}
