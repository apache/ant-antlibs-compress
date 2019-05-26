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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipException;

import org.apache.ant.compress.resources.ArFileSet;
import org.apache.ant.compress.resources.CommonsCompressArchiveResource;
import org.apache.ant.compress.resources.CpioFileSet;
import org.apache.ant.compress.resources.SevenZResource;
import org.apache.ant.compress.resources.TarFileSet;
import org.apache.ant.compress.resources.TarResource;
import org.apache.ant.compress.resources.ZipFileSet;
import org.apache.ant.compress.resources.ZipResource;
import org.apache.ant.compress.util.ArchiveStreamFactory;
import org.apache.ant.compress.util.EntryHelper;
import org.apache.ant.compress.util.StreamHelper;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ExtraFieldUtils;
import org.apache.commons.compress.archivers.zip.ZipExtraField;
import org.apache.commons.compress.archivers.zip.ZipShort;
import org.apache.commons.compress.utils.IOUtils;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.ArchiveResource;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.MappedResource;
import org.apache.tools.ant.types.resources.Resources;
import org.apache.tools.ant.types.resources.Restrict;
import org.apache.tools.ant.types.resources.selectors.Name;
import org.apache.tools.ant.types.resources.selectors.Not;
import org.apache.tools.ant.types.resources.selectors.Or;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.IdentityMapper;
import org.apache.tools.ant.util.MergingMapper;
import org.apache.tools.ant.util.ResourceUtils;
import org.apache.tools.zip.UnixStat;

/**
 * Base implementation of tasks creating archives.
 */
public abstract class ArchiveBase extends Task {
    private ArchiveStreamFactory factory;
    private FileSetBuilder fileSetBuilder;
    private EntryBuilder entryBuilder;

    private Resource dest;
    private List<ResourceCollection> sources = new ArrayList();
    private Mode mode = new Mode();
    private String encoding;
    private boolean filesOnly = true;
    private boolean preserve0permissions = false;
    private boolean roundUp = true;
    private boolean preserveLeadingSlashes = false;
    private Duplicate duplicate = new Duplicate();
    private WhenEmpty emptyBehavior = new WhenEmpty();

    private static final String NO_SOURCES_MSG = "No sources, nothing to do.";

    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();

    protected ArchiveBase() {}

    protected final void setFactory(ArchiveStreamFactory factory) {
        this.factory = factory;
    }

    protected final ArchiveStreamFactory getFactory() {
        return factory;
    }

    protected final void setEntryBuilder(EntryBuilder builder) {
        this.entryBuilder = builder;
    }

    protected final EntryBuilder getEntryBuilder() {
        return entryBuilder;
    }

    protected final void setFileSetBuilder(FileSetBuilder builder) {
        this.fileSetBuilder = builder;
    }

    protected final FileSetBuilder getFileSetBuilder() {
        return fileSetBuilder;
    }

    /**
     * The archive to create.
     */
    public void setDestfile(File f) {
        setDest(new FileResource(f));
    }

    /**
     * The archive to create.
     */
    public void addConfiguredDest(Resources r) {
        for (Iterator it = r.iterator(); it.hasNext(); ) {
            setDest((Resource) it.next());
        }
    }

    /**
     * The archive to create.
     */
    public void setDest(Resource r) {
        if (dest != null) {
            throw new BuildException("Can only have one destination resource"
                                     + " for archive.");
        }
        dest = r;
    }

    protected Resource getDest() {
        return dest;
    }

    /**
     * Sources for the archive.
     */
    public void add(ResourceCollection c) {
        sources.add(c);
    }

    /**
     * How to treat the target archive.
     */
    public void setMode(Mode m) {
        mode = m;
    }

    protected Mode getMode() {
        return mode;
    }

    /**
     * Encoding of file names.
     */
    public void setEncoding(String e) {
        encoding = e;
    }

    /**
     * Encoding of file names.
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Whether only file entries should be added to the archive.
     */
    public void setFilesOnly(boolean b) {
        filesOnly = b;
    }

    /**
     * Whether only file entries should be added to the archive.
     */
    protected boolean isFilesOnly() {
        return filesOnly;
    }

    /**
     * Whether 0 permissions read from an archive should be considered
     * real permissions (that should be preserved) or missing
     * permissions (which is the default).
     */
    public void setPreserve0permissions(boolean b) {
        preserve0permissions = b;
    }

    /**
     * Whether the file modification times will be rounded up to the
     * next timestamp (second or even second depending on the archive
     * format).
     *
     * <p>Zip archives store file modification times with a
     * granularity of two seconds, ar, tar and cpio use a granularity
     * of one second.  Times will either be rounded up or down.  If
     * you round down, the archive will always seem out-of-date when
     * you rerun the task, so the default is to round up.  Rounding up
     * may lead to a different type of problems like JSPs inside a web
     * archive that seem to be slightly more recent than precompiled
     * pages, rendering precompilation useless.</p>
     * @param r a <code>boolean</code> value
     */
    public void setRoundUp(boolean r) {
        roundUp = r;
    }

    /**
     * Flag to indicates whether leading `/'s should
     * be preserved in the file names.
     * Optional, default is <code>false</code>.
     * @param b the leading slashes flag.
     */
    public void setPreserveLeadingSlashes(boolean b) {
        this.preserveLeadingSlashes = b;
    }

    /**
     * Flag to indicates whether leading `/'s should
     * be preserved in the file names.
     * @since Apache Compress Antlib 1.1
     */
    public boolean getPreserveLeadingSlashes() {
        return preserveLeadingSlashes;
    }

    /**
     * Sets behavior for when a duplicate file is about to be added -
     * one of <code>add</code>, <code>preserve</code> or <code>fail</code>.
     * Possible values are: <code>add</code> (keep both
     * of the files); <code>preserve</code> (keep the first version
     * of the file found); <code>fail</code> halt a problem
     * Default is <code>fail</code>
     * @param df a <code>Duplicate</code> enumerated value
     */
    public void setDuplicate(Duplicate df) {
        duplicate = df;
    }

    /**
     * Sets behavior of the task when no resources are to be added.
     * Possible values are: <code>fail</code> (throw an exception
     * and halt the build); <code>skip</code> (do not create
     * any archive, but issue a warning);.
     * Default is <code>fail</code>;
     * @param we a <code>WhenEmpty</code> enumerated value
     */
    public void setWhenempty(WhenEmpty we) {
        emptyBehavior = we;
    }

    @Override
    public void execute() {
        validate();
        final Resource targetArchive = getDest();
        if (!targetArchive.isExists()) {
            // force create mode
            mode = new Mode();
            mode.setValue(Mode.FORCE_CREATE);
        }
        Collection sourceResources;
        try {
            sourceResources = findSources();
        } catch (IOException ioex) {
            throw new BuildException("Failed to read sources", ioex);
        }
        if (sourceResources.size() == 0) {
            if (WhenEmpty.SKIP.equals(emptyBehavior.getValue())) {
                log(NO_SOURCES_MSG, Project.MSG_WARN);
            } else {
                throw new BuildException(NO_SOURCES_MSG);
            }
        } else {
            File copyOfDest = maybeCopyTarget();
            Resource destOrCopy = copyOfDest == null
                ? targetArchive
                : new FileResource(copyOfDest);
            ArchiveFileSet existingEntries =
                fileSetBuilder.buildFileSet(destOrCopy);
            existingEntries.setProject(getProject());
            try {

                List/*<ResourceWithFlags>*/ toAdd
                    = new ArrayList/*<ResourceWithFlags>*/();
                toAdd.addAll(sourceResources);

                if (checkAndLogUpToDate(toAdd, targetArchive,
                                        existingEntries)) {
                    return;
                }

                addResourcesToKeep(toAdd, existingEntries, sourceResources);
                sort(toAdd);

                try {
                    writeArchive(toAdd);
                } catch (IOException ioex) {
                    throw new BuildException("Failed to write archive", ioex);
                }
            } finally {
                if (copyOfDest != null) {
                    FILE_UTILS.tryHardToDelete(copyOfDest);
                }
            }
        }
    }

    /**
     * Argument validation.
     */
    protected void validate() throws BuildException {
        if (factory == null) {
            throw new BuildException("subclass didn't provide a factory"
                                     + " instance");
        }
        if (entryBuilder == null) {
            throw new BuildException("subclass didn't provide an entryBuilder"
                                     + " instance");
        }
        if (fileSetBuilder == null) {
            throw new BuildException("subclass didn't provide a fileSetBuilder"
                                     + " instance");
        }
        if (getDest() == null) {
            throw new BuildException("must provide a destination resource");
        }
        if (sources.size() == 0) {
            throw new BuildException("must provide sources");
        }
    }

    /**
     * Find all the resources with their flags that should be added to
     * the archive.
     */
    protected Collection/*<ResourceWithFlags>*/ findSources()
        throws IOException {

        List<ResourceWithFlags> l = new ArrayList<>();
        Set<String> addedNames = new HashSet<>();
        for (Iterator rcs = sources.iterator(); rcs.hasNext(); ) {
            ResourceCollection rc = (ResourceCollection) rcs.next();
            ResourceCollectionFlags rcFlags = getFlags(rc);
            for (Iterator rs = rc.iterator(); rs.hasNext(); ) {
                Resource r = (Resource) rs.next();
                if (!isFilesOnly() || !r.isDirectory()) {
                    ResourceWithFlags rwf =
                        new ResourceWithFlags(r, rcFlags, getFlags(r));
                    String name = rwf.getName();
                    if (!"".equals(name) && !"/".equals(name)) {
                        boolean isDup = !addedNames.add(name);
                        if (!isDup || addDuplicate(name)) {
                            l.add(rwf);
                        }
                    }
                }
            }
        }
        return l;
    }

    private boolean checkAndLogUpToDate(Collection/*<ResourceWithFlags>*/ src,
                                        Resource targetArchive,
                                        ArchiveFileSet existingEntries) {
        try {
            if (!Mode.FORCE_CREATE.equals(getMode().getValue())
                && !Mode.FORCE_REPLACE.equals(getMode().getValue())
                && isUpToDate(src, existingEntries)) {
                log(targetArchive + " is up-to-date, nothing to do.");
                return true;
            }
        } catch (IOException ioex) {
            throw new BuildException("Failed to read target archive", ioex);
        }
        return false;
    }

    /**
     * Checks whether the target is more recent than the resources
     * that shall be added to it.
     *
     * <p>Will only ever be invoked if the target exists.</p>
     *
     * @param src the resources that have been found as sources, may
     * be modified in "update" mode to remove entries that are up to
     * date
     * @param existingEntries the target archive as fileset
     *
     * @return true if the target is up-to-date
     */
    protected boolean isUpToDate(Collection/*<ResourceWithFlags>*/ src,
                                 ArchiveFileSet existingEntries)
        throws IOException {

        final Resource[] srcResources = new Resource[src.size()];
        int index = 0;
        for (Iterator i = src.iterator(); i.hasNext(); ) {
            ResourceWithFlags r = (ResourceWithFlags) i.next();
            srcResources[index++] =
                new MappedResource(r.getResource(),
                                   new MergingMapper(r.getName()));
        }
        Resource[] outOfDate = ResourceUtils
            .selectOutOfDateSources(this, srcResources,
                                    new IdentityMapper(),
                                    existingEntries
                                    .getDirectoryScanner(getProject()));
        if (outOfDate.length > 0 && Mode.UPDATE.equals(getMode().getValue())) {
            HashSet<String> oodNames = new HashSet<>();
            for (int i = 0; i < outOfDate.length; i++) {
                oodNames.add(outOfDate[i].getName());
            }
            List/*<ResourceWithFlags>*/ copy =
                new LinkedList/*<ResourceWithFlags>*/(src);
            src.clear();
            for (Iterator i = copy.iterator(); i.hasNext(); ) {
                ResourceWithFlags r = (ResourceWithFlags) i.next();
                if (oodNames.contains(r.getName())) {
                    src.add(r);
                }
            }
        }
        return outOfDate.length == 0;
    }

    /**
     * Add the resources of the target archive that shall be kept when
     * creating the new one.
     */
    private void addResourcesToKeep(Collection/*<ResourceWithFlags>*/ toAdd,
                                    ArchiveFileSet target,
                                    Collection/*<ResourceWithFlags>*/ src) {
        if (!Mode.FORCE_CREATE.equals(getMode().getValue())
            && !Mode.CREATE.equals(getMode().getValue())) {
            try {
                toAdd.addAll(findUnmatchedTargets(target, src));
            } catch (IOException ioex) {
                throw new BuildException("Failed to read target archive", ioex);
            }
        }
    }

    /**
     * Find the resources from the target archive that don't have a
     * matching resource in the sources to be added.
     */
    protected Collection/*<ResourceWithFlags>*/
        findUnmatchedTargets(ArchiveFileSet target,
                             Collection/*<ResourceWithFlags>*/ src)
        throws IOException {

        List<ResourceWithFlags> l = new ArrayList<>();
        ResourceCollectionFlags rcFlags = getFlags(target);

        Restrict res = new Restrict();
        res.setProject(getProject());
        res.add(target);

        Not not = new Not();
        Or or = new Or();
        not.add(or);
        for (Iterator i = src.iterator(); i.hasNext(); ) {
            ResourceWithFlags r = (ResourceWithFlags) i.next();
            Name name = new Name();
            name.setName(r.getName());
            or.add(name);
        }
        res.add(not);

        for (Iterator rs = res.iterator(); rs.hasNext(); ) {
            Resource r = (Resource) rs.next();
            String name = r.getName();
            if ("".equals(name) || "/".equals(name)) {
                continue;
            }
            if (!isFilesOnly() || !r.isDirectory()) {
                l.add(new ResourceWithFlags(r, rcFlags, getFlags(r)));
            }
        }

        return l;
    }

    /**
     * Sorts the list of resources to add.
     */
    protected void sort(List/*<ResourceWithFlags>*/ l) {
        Collections.sort(l, new Comparator/*<ResourceWithFlags>*/() {
                @Override
                public int compare(Object o1, Object o2) {
                    ResourceWithFlags r1 = (ResourceWithFlags) o1;
                    ResourceWithFlags r2 = (ResourceWithFlags) o2;
                    return r1.getName().compareTo(r2.getName());
                }
            });
    }

    /**
     * Creates the archive archiving the given resources.
     */
    protected void writeArchive(Collection/*<ResourceWithFlags>*/ src)
        throws IOException {
        ArchiveOutputStream out = null;
        Set addedDirectories = new HashSet();
        try {
            String enc = Expand.NATIVE_ENCODING.equals(getEncoding())
                ? null : getEncoding();
            out = StreamHelper.getOutputStream(factory, getDest(), enc);
            if (out == null) {
                out =
                    factory.getArchiveStream(new BufferedOutputStream(getDest()
                                                                      .getOutputStream()),
                                             enc);
            }
            for (Iterator i = src.iterator(); i.hasNext(); ) {
                ResourceWithFlags r = (ResourceWithFlags) i.next();

                if (!isFilesOnly()) {
                    ensureParentDirs(out, r, addedDirectories);
                }

                ArchiveEntry ent = entryBuilder.buildEntry(r);
                out.putArchiveEntry(ent);
                if (!r.getResource().isDirectory()) {
                    try (InputStream in = r.getResource().getInputStream()) {
                        IOUtils.copy(in, out);
                    }
                } else {
                    addedDirectories.add(r.getName());
                }
                out.closeArchiveEntry();

            }
        } finally {
            FILE_UTILS.close(out);
        }
    }

    /**
     * Adds records for all parent directories of the given resource
     * that haven't already been added.
     *
     * <p>Flags for the "missing" directories will be taken from the
     * ResourceCollection that contains the resource to be added.</p>
     */
    protected void ensureParentDirs(ArchiveOutputStream out,
                                    ResourceWithFlags r,
                                    Set directoriesAdded)
        throws IOException {

        String[] parentStack = FileUtils.getPathStack(r.getName());
        String currentParent = "";
        int skip = r.getName().endsWith("/") ? 2 : 1;
        for (int i = 0; i < parentStack.length - skip; i++) {
            if ("".equals(parentStack[i])) continue;
            currentParent += parentStack[i] + "/";
            if (directoriesAdded.add(currentParent)) {
                Resource dir = new Resource(currentParent, true,
                                            System.currentTimeMillis(),
                                            true);
                ResourceWithFlags artifical =
                    new ResourceWithFlags(currentParent,
                                          dir, r.getCollectionFlags(),
                                          new ResourceFlags());
                ArchiveEntry ent = entryBuilder.buildEntry(artifical);
                out.putArchiveEntry(ent);
                out.closeArchiveEntry();
            }
        }
    }

    /**
     * Extracts flags from a resource.
     *
     * <p>ZipExceptions are only here for the code that translates
     * Ant's ZipExtraFields to CC ZipExtraFields and should never
     * actually be thrown.</p>
     */
    protected ResourceFlags getFlags(Resource r) throws ZipException {
        if (r instanceof ArchiveResource) {
            if (r instanceof CommonsCompressArchiveResource) {
                if (r instanceof TarResource) {
                    TarResource tr = (TarResource) r;
                    return new ResourceFlags(tr.getMode(), tr.getUid(),
                                             tr.getGid(), tr.getUserName(),
                                             tr.getGroup());
                } else if (r instanceof ZipResource) {
                    ZipResource zr = (ZipResource) r;
                    return new ResourceFlags(zr.getMode(), zr.getExtraFields(),
                                             zr.getMethod());
                } else if (r instanceof SevenZResource) {
                    SevenZResource zr = (SevenZResource) r;
                    return new ResourceFlags(zr.getContentMethods());
                } else {
                    CommonsCompressArchiveResource cr =
                        (CommonsCompressArchiveResource) r;
                    return new ResourceFlags(cr.getMode(), cr.getUid(),
                                             cr.getGid());
                }
            } else if (r instanceof
                       org.apache.tools.ant.types.resources.TarResource) {
                org.apache.tools.ant.types.resources.TarResource tr =
                    (org.apache.tools.ant.types.resources.TarResource) r;
                return new ResourceFlags(tr.getMode(), tr.getUid(),
                                         tr.getGid(), tr.getUserName(),
                                         tr.getGroup());
            } else if (r instanceof
                       org.apache.tools.ant.types.resources.ZipResource) {
                org.apache.tools.ant.types.resources.ZipResource zr =
                    (org.apache.tools.ant.types.resources.ZipResource) r;

                org.apache.tools.zip.ZipExtraField[] extra = zr.getExtraFields();
                ZipExtraField[] ex =
                    new ZipExtraField[extra == null ? 0 : extra.length];
                if (extra != null && extra.length > 0) {
                    for (int i = 0; i < extra.length; i++) {
                        try {
                            ex[i] = ExtraFieldUtils
                                .createExtraField(new ZipShort(extra[i]
                                                               .getHeaderId()
                                                               .getValue()));
                        } catch (InstantiationException e) {
                            throw new BuildException(e);
                        } catch (IllegalAccessException e) {
                            throw new BuildException(e);
                        }
                        byte[] b = extra[i].getCentralDirectoryData();
                        ex[i].parseFromCentralDirectoryData(b, 0, b.length);
                        b = extra[i].getLocalFileDataData();
                        ex[i].parseFromLocalFileData(b, 0, b.length);
                    }
                }

                return new ResourceFlags(zr.getMode(), ex, zr.getMethod());
            } else {
                ArchiveResource ar = (ArchiveResource) r;
                return new ResourceFlags(ar.getMode());
            }
        }
        return new ResourceFlags();
    }

    /**
     * Extracts flags from a resource collection.
     */
    protected ResourceCollectionFlags getFlags(ResourceCollection rc) {
        if (rc instanceof ArchiveFileSet) {
            if (rc instanceof ArFileSet) {
                ArFileSet ar = (ArFileSet) rc;
                return new ResourceCollectionFlags(ar.getPrefix(getProject()),
                                                   ar.getFullpath(getProject()),
                                                   ar.hasFileModeBeenSet()
                                                   ? ar.getFileMode(getProject())
                                                   : -1,
                                                   ar.hasDirModeBeenSet()
                                                   ? ar.getDirMode(getProject())
                                                   : -1,
                                                   ar.hasUserIdBeenSet()
                                                   ? ar.getUid()
                                                   : EntryHelper.UNKNOWN_ID,
                                                   ar.hasGroupIdBeenSet()
                                                   ? ar.getGid()
                                                   : EntryHelper.UNKNOWN_ID);
            } else if (rc instanceof CpioFileSet) {
                CpioFileSet cr = (CpioFileSet) rc;
                return new ResourceCollectionFlags(cr.getPrefix(getProject()),
                                                   cr.getFullpath(getProject()),
                                                   cr.hasFileModeBeenSet()
                                                   ? cr.getFileMode(getProject())
                                                   : -1,
                                                   cr.hasDirModeBeenSet()
                                                   ? cr.getDirMode(getProject())
                                                   : -1,
                                                   cr.hasUserIdBeenSet()
                                                   ? cr.getUid()
                                                   : EntryHelper.UNKNOWN_ID,
                                                   cr.hasGroupIdBeenSet()
                                                   ? cr.getGid()
                                                   : EntryHelper.UNKNOWN_ID);
            } else if (rc instanceof TarFileSet) {
                TarFileSet tr = (TarFileSet) rc;
                return new ResourceCollectionFlags(tr.getPrefix(getProject()),
                                                   tr.getFullpath(getProject()),
                                                   tr.hasFileModeBeenSet()
                                                   ? tr.getFileMode(getProject())
                                                   : -1,
                                                   tr.hasDirModeBeenSet()
                                                   ? tr.getDirMode(getProject())
                                                   : -1,
                                                   tr.hasUserIdBeenSet()
                                                   ? tr.getUid()
                                                   : EntryHelper.UNKNOWN_ID,
                                                   tr.hasGroupIdBeenSet()
                                                   ? tr.getGid()
                                                   : EntryHelper.UNKNOWN_ID,
                                                   tr.hasUserNameBeenSet()
                                                   ? tr.getUserName() : null,
                                                   tr.hasGroupBeenSet()
                                                   ? tr.getGroup() : null);
            } else if (rc instanceof
                       org.apache.tools.ant.types.TarFileSet) {
                org.apache.tools.ant.types.TarFileSet tr =
                    (org.apache.tools.ant.types.TarFileSet) rc;
                return new ResourceCollectionFlags(tr.getPrefix(getProject()),
                                                   tr.getFullpath(getProject()),
                                                   tr.hasFileModeBeenSet()
                                                   ? tr.getFileMode(getProject())
                                                   : -1,
                                                   tr.hasDirModeBeenSet()
                                                   ? tr.getDirMode(getProject())
                                                   : -1,
                                                   tr.hasUserIdBeenSet()
                                                   ? tr.getUid()
                                                   : EntryHelper.UNKNOWN_ID,
                                                   tr.hasGroupIdBeenSet()
                                                   ? tr.getGid()
                                                   : EntryHelper.UNKNOWN_ID,
                                                   tr.hasUserNameBeenSet()
                                                   ? tr.getUserName() : null,
                                                   tr.hasGroupBeenSet()
                                                   ? tr.getGroup() : null);
            } else {
                ArchiveFileSet ar = (ArchiveFileSet) rc;
                return new ResourceCollectionFlags(ar.getPrefix(getProject()),
                                                   ar.getFullpath(getProject()),
                                                   ar.hasFileModeBeenSet()
                                                   ? ar.getFileMode(getProject())
                                                   : -1,
                                                   ar.hasDirModeBeenSet()
                                                   ? ar.getDirMode(getProject())
                                                   : -1);
            }
        }
        return new ResourceCollectionFlags();
    }

    /**
     * Ensures a forward slash is used as file separator and strips
     * leading slashes if preserveLeadingSlashes is false.
     */
    protected String bendSlashesForward(String s) {
        if (s != null) {
            s = s.replace('\\', '/');
            if (File.separatorChar != '/' && File.separatorChar != '\\') { 
                s = s.replace(File.separatorChar, '/');
            }
            while (!preserveLeadingSlashes && s.startsWith("/")) {
                s = s.substring(1);
            }
        }
        return s;
    }

    /**
     * Modify last modified timestamp based on the roundup attribute.
     *
     * @param millis the timestamp
     * @param granularity the granularity of timestamps in the archive
     * format in millis
     */
    protected long round(long millis, long granularity) {
        return roundUp ? millis + granularity - 1 : millis;
    }

    /**
     * Is invoked if a duplicate entry is found, decides whether the
     * entry shall be added regardless.
     */
    protected boolean addDuplicate(String name) {
        if (duplicate.getValue().equals(Duplicate.PRESERVE)) {
            log(name + " already added, skipping.", Project.MSG_INFO);
            return false;
        } else if (duplicate.getValue().equals(Duplicate.FAIL)) {
            throw new BuildException("Duplicate entry " + name
                                     + " was found and the duplicate "
                                     + "attribute is 'fail'.");
        }
        // duplicate equal to add, so we continue
        log("duplicate entry " + name + " found, adding.", Project.MSG_VERBOSE);
        return true;
    }

    /**
     * Creates a copy of the target archive in update or recreate mode
     * because some entries may later be read and archived from it.
     */
    private File maybeCopyTarget() {
        File copyOfDest = null;
        try {
            if (!Mode.FORCE_CREATE.equals(getMode().getValue())
                && !Mode.CREATE.equals(getMode().getValue())) {
                copyOfDest = FILE_UTILS.createTempFile(getTaskName(), ".tmp",
                                                       null, true, false);
                ResourceUtils.copyResource(getDest(),
                                           new FileResource(copyOfDest));
            }
        } catch (IOException ioex) {
            if (copyOfDest != null && copyOfDest.exists()) {
                FILE_UTILS.tryHardToDelete(copyOfDest);
            }
            throw new BuildException("Failed to copy target archive", ioex);
        }
        return copyOfDest;
    }

    /**
     * Valid Modes for create/update/replace.
     */
    public static final class Mode extends EnumeratedAttribute {

        /**
         * Create a new archive.
         */
        public static final String CREATE = "create";
        /**
         * Create a new archive even if the target exists and seems
         * up-to-date.
         */
        public static final String FORCE_CREATE = "force-create";
        /**
         * Update an existing archive.
         */
        public static final String UPDATE = "update";
        /**
         * Update an existing archive, replacing all existing entries
         * with those from sources.
         */
        public static final String REPLACE = "replace";
        /**
         * Update an existing archive - replacing all existing entries
         * with those from sources - even if the target exists and
         * seems up-to-date.
         */
        public static final String FORCE_REPLACE = "force-replace";

        public Mode() {
            super();
            setValue(CREATE);
        }

        public String[] getValues() {
            return new String[] {CREATE, UPDATE, REPLACE,
                                 FORCE_CREATE, FORCE_REPLACE};
        }

    }

    /**
     * Possible behaviors when a duplicate file is added:
     * "add", "preserve" or "fail"
     */
    public static class Duplicate extends EnumeratedAttribute {
        private static String ADD = "add";
        private static String PRESERVE = "preserve";
        private static String FAIL = "fail";

        public Duplicate() {
            setValue(FAIL);
        }

        /**
         * @see EnumeratedAttribute#getValues()
         */
        /** {@inheritDoc} */
        public String[] getValues() {
            return new String[] {ADD, PRESERVE, FAIL};
        }
    }

    /**
     * Possible behaviors when there are no matching files for the task:
     * "fail", "skip".
     */
    public static class WhenEmpty extends EnumeratedAttribute {
        private static String FAIL = "fail";
        private static String SKIP = "skip";

        public WhenEmpty() {
            setValue(FAIL);
        }

        /**
         * The string values for the enumerated value
         * @return the values
         */
        public String[] getValues() {
            return new String[] {FAIL, SKIP};
        }
    }

    /**
     * Various flags a (archive) resource may hold in addition to
     * being a plain resource.
     */
    // FIXME this isn't really scaling if it wants to be a catch all
    // for all relevant flags of all formats.
    public class ResourceFlags {
        private final int mode;
        private final boolean modeSet;
        private final int gid;
        private final int uid;
        private final ZipExtraField[] extraFields;
        private final String userName;
        private final String groupName;
        private final int compressionMethod;
        private Iterable/*<? extends SevenZMethodConfiguration>*/ contentMethods;

        public ResourceFlags() {
            this(-1);
        }

        public ResourceFlags(int mode) {
            this(mode, new ZipExtraField[0], -1);
        }

        public ResourceFlags(int mode, ZipExtraField[] extraFields,
                             int compressionMethod) {
            this(mode, extraFields, EntryHelper.UNKNOWN_ID,
                 EntryHelper.UNKNOWN_ID, null, null,
                 compressionMethod, null);
        }

        public ResourceFlags(int mode, int uid, int gid) {
            this(mode, new ZipExtraField[0], uid, gid, null, null, -1, null);
        }

        public ResourceFlags(int mode, int uid, int gid, String userName,
                             String groupName) {
            this(mode, new ZipExtraField[0], uid, gid, userName, groupName, -1, null);
        }

        public ResourceFlags(Iterable/*<? extends SevenZMethodConfiguration>*/ contentMethods) {
            this(-1, new ZipExtraField[0], EntryHelper.UNKNOWN_ID,
                 EntryHelper.UNKNOWN_ID, null, null, -1, contentMethods);
        }

        private ResourceFlags(int mode, ZipExtraField[] extraFields,
                              int uid, int gid,
                              String userName, String groupName,
                              int compressionMethod,
                              Iterable/*<? extends SevenZMethodConfiguration>*/ contentMethods) {
            this.mode = mode;
            this.extraFields = extraFields;
            this.gid = gid;
            this.uid = uid;
            this.userName = userName;
            this.groupName = groupName;
            int m = mode & UnixStat.PERM_MASK;
            modeSet = mode >= 0 && (m > 0 || (m == 0 && preserve0permissions));
            this.compressionMethod = compressionMethod;
            this.contentMethods = contentMethods;
        }

        public boolean hasModeBeenSet() { return modeSet; }
        public int getMode() { return mode; }

        public ZipExtraField[] getZipExtraFields() { return extraFields; }

        public boolean hasUserIdBeenSet() {
            return uid != EntryHelper.UNKNOWN_ID;
        }
        public int getUserId() { return uid; }

        public boolean hasGroupIdBeenSet() {
            return gid != EntryHelper.UNKNOWN_ID;
        }
        public int getGroupId() { return gid; }

        public boolean hasUserNameBeenSet() { return userName != null; }
        public String getUserName() { return userName; }

        public boolean hasGroupNameBeenSet() { return groupName != null; }
        public String getGroupName() { return groupName; }

        public boolean hasCompressionMethod() { return compressionMethod >= 0; }
        public int getCompressionMethod() { return compressionMethod; }

        public boolean hasContentMethods() { return contentMethods != null; }
        public Iterable/*<? extends SevenZMethodConfiguration>*/ getContentMethods() {
            return contentMethods;
        }
    }

    /**
     * Various flags a (archive) resource collection may hold.
     */
    public class ResourceCollectionFlags extends ResourceFlags {
        private final String prefix, fullpath;
        private final int dirMode;

        public ResourceCollectionFlags() {
            this(null, null);
        }

        public ResourceCollectionFlags(String prefix, String fullpath) {
            this(prefix, fullpath, -1, -1);
        }

        public ResourceCollectionFlags(String prefix, String fullpath,
                                       int fileMode, int dirMode) {
            this(prefix, fullpath, fileMode, dirMode, EntryHelper.UNKNOWN_ID,
                 EntryHelper.UNKNOWN_ID);
        }

        public ResourceCollectionFlags(String prefix, String fullpath,
                                       int fileMode, int dirMode,
                                       int uid, int gid) {
            this(prefix, fullpath, fileMode, dirMode, uid, gid, null, null);
        }

        public ResourceCollectionFlags(String prefix, String fullpath,
                                       int fileMode, int dirMode,
                                       int uid, int gid,
                                       String userName, String groupName) {
            super(fileMode, uid, gid, userName, groupName);
            this.dirMode = dirMode;
            this.prefix = bendSlashesForward(prefix);
            this.fullpath = bendSlashesForward(fullpath);
        }

        public boolean hasDirModeBeenSet() { return dirMode >= 0; }
        public int getDirMode() { return dirMode; }

        public boolean hasPrefix() {
            return prefix != null && !"".equals(prefix);
        }
        public String getPrefix() { return prefix; }

        public boolean hasFullpath() {
            return fullpath != null && !"".equals(fullpath);
        }
        public String getFullpath() { return fullpath; }
    }

    /**
     * Binds a resource to additional data that may be present.
     */
    public class ResourceWithFlags {
        private final Resource r;
        private final ResourceCollectionFlags rcFlags;
        private final ResourceFlags rFlags;
        private final String name;

        public ResourceWithFlags(Resource r, ResourceCollectionFlags rcFlags,
                                 ResourceFlags rFlags) {
            this(null, r, rcFlags, rFlags);
        }

        public ResourceWithFlags(String name, Resource r,
                                 ResourceCollectionFlags rcFlags,
                                 ResourceFlags rFlags) {
            this.r = r;
            this.rcFlags = rcFlags;
            this.rFlags = rFlags;

            if (name == null) {
                name = r.getName();
                if (rcFlags.hasFullpath()) {
                    name = rcFlags.getFullpath();
                } else if (rcFlags.hasPrefix()) {
                    String prefix = rcFlags.getPrefix();
                    if (!prefix.endsWith("/")) {
                        prefix = prefix + "/";
                    }
                    name = prefix + name;
                }
                name = bendSlashesForward(name);
            }
            if (r.isDirectory() && !name.endsWith("/")) {
                name += "/";
            } else if (!r.isDirectory() && name.endsWith("/")) {
                name = name.substring(0, name.length() - 1);
            }

            this.name = name;
        }

        public Resource getResource() { return r; }
        public ResourceCollectionFlags getCollectionFlags() { return rcFlags; }
        public ResourceFlags getResourceFlags() { return rFlags; }

        /**
         * The name the target entry will have.
         *
         * <p>Already takes fullpath and prefix into account.</p>
         *
         * <p>Ensures directory names end in slashes while file names
         * never will.</p>
         */
        public String getName() {
            return name;
        }
    }

    /**
     * Creates an archive entry for the concrete format.
     */
    public static interface EntryBuilder {
        ArchiveEntry buildEntry(ResourceWithFlags resource);
    }

    /**
     * Creates an archive fileset to read the target archive.
     */
    public static interface FileSetBuilder {
        ArchiveFileSet buildFileSet(Resource dest);
    }
}
