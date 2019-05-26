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

package org.apache.ant.compress.taskdefs;

import org.apache.ant.compress.resources.BZip2Resource;
import org.apache.ant.compress.resources.CommonsCompressCompressorResource;
import org.apache.ant.compress.util.BZip2StreamFactory;
import org.apache.tools.ant.types.Resource;

/**
 * Compresses using gzip.
 */
public final class BZip2 extends PackBase {

    public BZip2() {
        super(new BZip2StreamFactory(),
              new PackBase.ResourceWrapper() {
                @Override
                public CommonsCompressCompressorResource wrap(Resource dest) {
                    return new BZip2Resource(dest);
                }
            });
    }

}
