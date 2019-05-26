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

package org.apache.ant.compress.conditions;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.types.resources.ArchiveResource;
import org.apache.tools.zip.UnixStat;

/**
 * Tests the mode of an archive entry.
 */
public class HasMode extends ProjectComponent implements Condition {
    private ArchiveResource resource;
    private int mode = -1;

    public void add(ArchiveResource r) {
        if (resource != null) {
            throw new BuildException("only one resource can be tested");
        }
        resource = r;
    }

    public void setMode(String octalString) {
        mode = Integer.parseInt(octalString, 8);
    }

    protected void validate() throws BuildException {
        if (resource == null) {
            throw new BuildException("you must specify a resource");
        }
        if (mode < 0) {
            throw new BuildException("mode is required");
        }
    }

    @Override
    public boolean eval() throws BuildException {
        validate();
        int actual = resource.getMode();
        if (mode <= UnixStat.PERM_MASK) {
            actual &= UnixStat.PERM_MASK;
        }
        log("expected: " + mode + " (octal " + Integer.toString(mode, 8)
            + "), actual: " + actual + " (octal " + Integer.toString(actual, 8)
            + ")", Project.MSG_VERBOSE);
        return mode == actual;
    }
}
