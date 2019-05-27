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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.compress.compressors.pack200.Pack200Utils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.apache.tools.ant.util.FileUtils;

/**
 * Task to "normalize" a JAR archive so that a signature applied to it
 * will still be valid after a pack200/unpack200 cycle.
 *
 * <p>As stated in <a
 * href="https://download.oracle.com/javase/1.5.0/docs/api/java/util/jar/Pack200.Packer.html">Pack200.Packer's</a>
 * javadocs applying a Pack200 compression to a JAR archive will in
 * general make its sigantures invalid.  In order to prepare a JAR for
 * signing it should be "normalized" by packing and unpacking it.
 * This is what this task does.</p>
 * @since Apache Compress Antlib 1.1
 */
public class Pack200Normalize extends Task {
    private File src, dest;
    private boolean force = false;
    private Map<String, String> properties = new HashMap();

    /**
     * The JAR archive to normalize.
     */
    public void setSrcFile(File s) {
        src = s;
    }

    /**
     * The destination archive.
     */
    public void setDestFile(File d) {
        dest = d;
    }

    /**
     * Whether to force normalization of the archive even if the
     * destination is up-to-date.
     *
     * <p>You must set this to true if you don't specify a destFile or
     * the archive will never get normalized.</p>
     */
    public void setForce(boolean b) {
        force = b;
    }

    /**
     * Sets a property for the Pack200 packer.
     */
    public void addConfiguredProperty(Environment.Variable prop) {
        prop.validate();
        properties.put(prop.getKey(), prop.getValue());
    }

    @Override
    public void execute() {
        validate();
        if (isOutOfDate()) {
            normalize();
        } else if (dest != null) {
            log(src + " not normalized as " + dest + " is up-to-date.",
                Project.MSG_VERBOSE);
        } else {
            log(src + " not normalized as force attribute is false.",
                Project.MSG_VERBOSE);
        }
    }

    private void validate() {
        if (src == null) {
            throw new BuildException("srcFile attribute is required");
        }
        if (!src.exists()) {
            throw new BuildException(src + " doesn't exists");
        }
        if (!src.isFile()) {
            throw new BuildException(src + " must be a file");
        }
        if (dest != null && dest.isDirectory()) {
            throw new BuildException(dest + " must be a file");
        }
    }

    private boolean isOutOfDate() {
        return force ||
            (dest != null
             && SelectorUtils.isOutOfDate(new FileResource(src),
                                          new FileResource(dest),
                                          FileUtils.getFileUtils()
                                          .getFileTimestampGranularity())
             );
    }

    private void normalize() {
        if (dest != null) {
            log("Normalizing " + src + " to " + dest + ".");
        } else {
            log("Normalizing " + src + ".");
        }
        try {
            Pack200Utils.normalize(src, dest != null ? dest : src, properties);
        } catch (IOException ex) {
            throw new BuildException("Caught an error normalizing "
                                     + src, ex);
        }
    }
}
