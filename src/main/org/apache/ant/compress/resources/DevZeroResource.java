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
package org.apache.ant.compress.resources;

import org.apache.tools.ant.types.Resource;
import java.io.InputStream;
import java.util.Arrays;

/**
 * A resource that returns arbitrary many 0-bytes as its content.
 *
 * <p>This class should likely be part of Ant and only lives here as
 * it is needed for tests.</p>
 *
 * @since Compress Antlib 1.2
 */
public class DevZeroResource extends Resource {

    @Override
    public InputStream getInputStream() {
        final long size = getSize();
        return new InputStream() {
            private long bytesRead = 0;
            public int read() {
                return bytesRead++ < size ? 0 : -1;
            }
            public int read(byte[] b) {
                return read(b, 0, b.length);
            }
            public int read(byte[] b, int off, int len) {
                len = (int) Math.min((long) len, size - bytesRead);
                bytesRead += len;
                Arrays.fill(b, off, off + len, (byte) 0);
                return len == 0 ? -1 : len;
            }
        };
    }
}
