/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.ant.compress.taskdefs;

import org.apache.ant.compress.util.TarStreamFactory;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.apache.tools.ant.types.ArchiveFileSet;

/**
 * Creates tar archives.
 */
public class Tar extends ArchiveBase {
    public Tar() {
        super(new TarStreamFactory(),
              new ArchiveBase.EntryBuilder() {
                public ArchiveEntry buildEntry(ArchiveBase.ResourceWithFlags r) {
                    boolean isDir = r.getResource().isDirectory();
                    TarArchiveEntry ent =
                        new TarArchiveEntry(r.getName(),
                                            isDir ? TarConstants.LF_DIR
                                            : TarConstants.LF_NORMAL);
                    ent.setModTime(r.getResource().getLastModified());
                    ent.setSize(isDir ? 0 : r.getResource().getSize());

                    if (r.getResourceFlags().hasModeBeenSet()) {
                        ent.setMode(r.getResourceFlags().getMode());
                    } else if (!isDir
                               && r.getCollectionFlags().hasModeBeenSet()) {
                        ent.setMode(r.getCollectionFlags().getMode());
                    } else if (isDir
                               && r.getCollectionFlags().hasDirModeBeenSet()) {
                        ent.setMode(r.getCollectionFlags().getDirMode());
                    } else {
                        ent.setMode(isDir
                                    ? ArchiveFileSet.DEFAULT_DIR_MODE
                                    : ArchiveFileSet.DEFAULT_FILE_MODE);
                    }

                    if (r.getResourceFlags().hasUserIdBeenSet()) {
                        ent.setUserId(r.getResourceFlags().getUserId());
                    } else if (r.getCollectionFlags().hasUserIdBeenSet()) {
                        ent.setUserId(r.getCollectionFlags().getUserId());
                    }

                    if (r.getResourceFlags().hasGroupIdBeenSet()) {
                        ent.setGroupId(r.getResourceFlags().getGroupId());
                    } else if (r.getCollectionFlags().hasGroupIdBeenSet()) {
                        ent.setGroupId(r.getCollectionFlags().getGroupId());
                    }
 
                    if (r.getResourceFlags().hasUserNameBeenSet()) {
                        ent.setUserName(r.getResourceFlags().getUserName());
                    } else if (r.getCollectionFlags().hasUserNameBeenSet()) {
                        ent.setUserName(r.getCollectionFlags().getUserName());
                    }

                    if (r.getResourceFlags().hasGroupNameBeenSet()) {
                        ent.setGroupName(r.getResourceFlags().getGroupName());
                    } else if (r.getCollectionFlags().hasGroupNameBeenSet()) {
                        ent.setGroupName(r.getCollectionFlags().getGroupName());
                    }
 
                    return ent;
                }
            });
    }

}