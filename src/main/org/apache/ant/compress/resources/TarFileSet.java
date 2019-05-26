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

import org.apache.ant.compress.util.TarStreamFactory;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.ArchiveScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.Resource;

/**
 * A TarFileSet is a FileSet with extra attributes useful in the context of
 * Tar tasks.
 *
 * A TarFileSet extends FileSets with the ability to extract a subset of the
 * entries of a Tar file for inclusion in another Tar file.  It also includes
 * a prefix attribute which is prepended to each entry in the output Tar file.
 *
 */
public class TarFileSet extends CommonsCompressFileSet {

    private boolean userNameSet;
    private boolean groupNameSet;

    private String userName = "";
    private String groupName = "";

    /** Constructor for TarFileSet */
    public TarFileSet() {
        super();
    }

    /**
     * Constructor using a fileset argument.
     * @param fileset the fileset to use
     */
    protected TarFileSet(FileSet fileset) {
        super(fileset);
    }

    /**
     * Constructor using a tarfileset argument.
     * @param fileset the tarfileset to use
     */
    protected TarFileSet(TarFileSet fileset) {
        super(fileset);
    }

    /**
     * Constructor using a CommonsCompressFileSet argument.
     * @param fileset the fileset to use
     */
    protected TarFileSet(CommonsCompressFileSet fileset) {
        super(fileset);
    }

    /**
     * The username for the tar entry
     * This is not the same as the UID.
     * @param userName the user name for the tar entry.
     */
    public void setUserName(String userName) {
        checkCommonsCompressFileSetAttributesAllowed();
        userNameSet = true;
        this.userName = userName;
    }

    /**
     * @return the user name for the tar entry
     */
    public String getUserName() {
        if (isReference()) {
            return ((TarFileSet) getCheckedRef()).getUserName();
        }
        return userName;
    }

    /**
     * @return whether the user name has been explicitly set.
     */
    public boolean hasUserNameBeenSet() {
        return userNameSet;
    }

    /**
     * The groupname for the tar entry; optional, default=""
     * This is not the same as the GID.
     * @param groupName the group name string.
     */
    public void setGroup(String groupName) {
        checkCommonsCompressFileSetAttributesAllowed();
        groupNameSet = true;
        this.groupName = groupName;
    }

    /**
     * @return the group name string.
     */
    public String getGroup() {
        if (isReference()) {
            return ((TarFileSet) getCheckedRef()).getGroup();
        }
        return groupName;
    }

    /**
     * @return whether the group name has been explicitly set.
     */
    public boolean hasGroupBeenSet() {
        return groupNameSet;
    }

    /**
     * Create a new scanner.
     * @return the created scanner.
     */
    @Override
    protected ArchiveScanner newArchiveScanner() {
        CommonsCompressArchiveScanner cs =
            new CommonsCompressArchiveScanner(new TarStreamFactory(),
                                                 new CommonsCompressArchiveScanner.ResourceBuilder() {
                @Override
                public Resource buildResource(Resource archive, String encoding,
                                              ArchiveEntry entry) {
                    return new TarResource(archive, encoding,
                                           (TarArchiveEntry) entry);
                }
            }, getSkipUnreadableEntries(), getProject());
        cs.setEncoding(getEncoding());
        return cs;
    }

    /**
     * Makes this instance in effect a reference to another instance.
     *
     * <p>You must not set another attribute or nest elements inside
     * this element if you make it a reference.</p>
     * @param r the <code>Reference</code> to use.
     * @throws BuildException on error
     */
    @Override
    public void setRefid(Reference r) throws BuildException {
        if (userNameSet || groupNameSet) {
            throw tooManyAttributes();
        }
        super.setRefid(r);
    }

    /**
     * Configure a fileset based on this fileset.
     * If the fileset is a TarFileSet copy in the tarfileset
     * specific attributes.
     * @param zfs the archive fileset to configure.
     */
    @Override
    protected void configureFileSet(ArchiveFileSet zfs) {
        super.configureFileSet(zfs);
        if (zfs instanceof TarFileSet) {
            TarFileSet tfs = (TarFileSet) zfs;
            tfs.setUserName(userName);
            tfs.setGroup(groupName);
        }
    }

    @Override
    protected CommonsCompressFileSet newFileSet(FileSet fs) {
        if (fs instanceof TarFileSet) {
            return new TarFileSet((TarFileSet) fs);
        }
        if (fs instanceof CommonsCompressFileSet) {
            return new TarFileSet((CommonsCompressFileSet) fs);
        }
        return new TarFileSet(fs);
    }
}
