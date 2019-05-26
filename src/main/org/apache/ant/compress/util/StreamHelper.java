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

import java.io.IOException;

import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileProvider;

/**
 * Helper methods that deal with FileAware*Factory and Resources that
 * may or may not provide files.
 */
public class StreamHelper {
    private StreamHelper() { }

    private static boolean isFileCombination(ArchiveStreamFactory factory,
                                             Resource r) {
        return factory instanceof FileAwareArchiveStreamFactory
            && r.as(FileProvider.class) != null;
    }

    private static boolean isFileCombination(CompressorStreamFactory factory,
                                             Resource r) {
        return factory instanceof FileAwareCompressorStreamFactory
            && r.as(FileProvider.class) != null;
    }

    /**
     * If the factory knows about files and the resource can provide
     * one, returns an ArchiveInputStream for it, otherwise returns
     * null.
     * @param r the resource to read from
     * @param encoding the encoding of the entry names
     */
    public static
        ArchiveInputStream getInputStream(ArchiveStreamFactory factory,
                                          Resource r, String encoding)
        throws IOException {
        if (isFileCombination(factory, r)) {
            FileProvider p = (FileProvider) r.as(FileProvider.class);
            FileAwareArchiveStreamFactory f =
                (FileAwareArchiveStreamFactory) factory;
            return f.getArchiveInputStream(p.getFile(), encoding);
        }
        return null;
    }

    /**
     * If the factory knows about files and the resource can provide
     * one, returns an ArchiveOutputStream for it, otherwise returns
     * null.
     * @param r the resource to write to
     * @param encoding the encoding of the entry names
     */
    public static
        ArchiveOutputStream getOutputStream(ArchiveStreamFactory factory,
                                            Resource r, String encoding)
        throws IOException {
        if (isFileCombination(factory, r)) {
            FileProvider p = (FileProvider) r.as(FileProvider.class);
            FileAwareArchiveStreamFactory f =
                (FileAwareArchiveStreamFactory) factory;
            return f.getArchiveOutputStream(p.getFile(), encoding);
        }
        return null;
    }

    /**
     * If the factory knows about files and the resource can provide
     * one, returns an CompressorInputStream for it, otherwise returns
     * null.
     * @param r the resource to read from
     */
    public static
        CompressorInputStream getInputStream(CompressorStreamFactory factory,
                                             Resource r)
        throws IOException {
        if (isFileCombination(factory, r)) {
            FileProvider p = (FileProvider) r.as(FileProvider.class);
            FileAwareCompressorStreamFactory f =
                (FileAwareCompressorStreamFactory) factory;
            return f.getCompressorInputStream(p.getFile());
        }
        return null;
    }

    /**
     * If the factory knows about files and the resource can provide
     * one, returns an CompressorOutputStream for it, otherwise returns
     * null.
     * @param r the resource to write to
     */
    public static
        CompressorOutputStream getOutputStream(CompressorStreamFactory factory,
                                               Resource r)
        throws IOException {
        if (isFileCombination(factory, r)) {
            FileProvider p = (FileProvider) r.as(FileProvider.class);
            FileAwareCompressorStreamFactory f =
                (FileAwareCompressorStreamFactory) factory;
            return f.getCompressorOutputStream(p.getFile());
        }
        return null;
    }
}
