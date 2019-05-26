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

import org.apache.ant.compress.resources.TarResource;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.taskdefs.condition.Condition;

/**
 * Tests the user name of a tar entry.
 */
public class HasUserName extends ProjectComponent implements Condition {
    private TarResource ccResource;
    private org.apache.tools.ant.types.resources.TarResource antResource;
    private String name;

    public void add(org.apache.tools.ant.types.resources.TarResource r) {
        if (ccResource != null || antResource != null) {
            throw new BuildException("only one resource can be tested");
        }
        antResource = r;
    }

    public void add(TarResource r) {
        if (ccResource != null || antResource != null) {
            throw new BuildException("only one resource can be tested");
        }
        ccResource = r;
    }

    public void setName(String n) {
        name = n;
    }

    protected void validate() throws BuildException {
        if (ccResource == null && antResource == null) {
            throw new BuildException("you must specify a resource");
        }
        if (name == null) {
            throw new BuildException("name is required");
        }
    }

    @Override
    public boolean eval() throws BuildException {
        validate();
        String actual = ccResource != null
            ? ccResource.getUserName() : antResource.getUserName();
        log("expected: " + name + ", actual: " + actual, Project.MSG_VERBOSE);
        return name.equals(actual);
    }
}
