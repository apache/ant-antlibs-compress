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
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZMethod;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

/**
 * @since Apache Compress Antlib 1.3
 */
public class SevenZStreamFactory implements FileAwareArchiveStreamFactory {

    /**
     * @param stream the stream to read from, should be buffered
     * @param encoding the encoding of the entry names
     */
    @Override
    public ArchiveInputStream getArchiveStream(InputStream stream,
                                               String encoding)
        throws IOException {
        throw new UnsupportedOperationException("only file-based archives can be read");
    }

    /**
     * @param stream the stream to write to, should be buffered
     * @param encoding the encoding of the entry names
     */
    @Override
    public ArchiveOutputStream getArchiveStream(OutputStream stream,
                                                String encoding)
        throws IOException {
        throw new UnsupportedOperationException("only file-based archives can be written");
    }

    /**
     * @param file the file to read from
     * @param encoding the encoding of the entry names
     */
    @Override
    public ArchiveInputStream getArchiveInputStream(File file,
                                                    String encoding)
        throws IOException {
        return new SevenZArchiveInputStream(file);
    }

    /**
     * @param file the file to write to
     * @param encoding the encoding of the entry names
     */
    @Override
    public ArchiveOutputStream getArchiveOutputStream(File file,
                                                      String encoding)
        throws IOException {
        return new SevenZArchiveOutputStream(file);
    }

    /**
     * Not really a stream but provides an ArchiveInputStream
     * compatible interface over SevenZFile.
     */
    private static class SevenZArchiveInputStream extends ArchiveInputStream {

        private final SevenZFile zipFile;

        public SevenZArchiveInputStream(File file) throws IOException {
            zipFile = new SevenZFile(file);
        }

        public ArchiveEntry getNextEntry() throws IOException {
            return zipFile.getNextEntry();
        }

        public int read(byte[] b, int off, int len) throws IOException {
            return zipFile.read(b, off, len);
        }

        public void close() throws IOException {
            zipFile.close();
        }
    }

    /**
     * Not really a stream but provides an ArchiveOutputStream
     * compatible interface over SevenZOutputFile.
     */
    public static class SevenZArchiveOutputStream extends ArchiveOutputStream {

        private final SevenZOutputFile zipFile;

        public SevenZArchiveOutputStream(File file) throws IOException {
            zipFile = new SevenZOutputFile(file);
        }

        public void close() throws IOException {
            zipFile.close();
        }

        public void putArchiveEntry(ArchiveEntry entry) throws IOException {
            zipFile.putArchiveEntry(entry);
        }

        public void closeArchiveEntry() throws IOException {
            zipFile.closeArchiveEntry();
        }

        public void finish() throws IOException {
            zipFile.finish();
        }

        public ArchiveEntry createArchiveEntry(File inputFile, String entryName) 
            throws IOException {
            return zipFile.createArchiveEntry(inputFile, entryName);
        }

        public void write(byte[] b,int  off, int len) throws IOException {
            zipFile.write(b, off, len);
        }
                
        public void setContentCompression(SevenZMethod method) {
            zipFile.setContentCompression(method);
        }

        public void setContentMethods(Iterable methods) {
            zipFile.setContentMethods(methods);
        }

    }

}
