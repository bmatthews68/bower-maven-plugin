/*
 *  Copyright 2013 Brian Matthews
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.btmatthews.maven.plugins.bower;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.0.0
 */
public class PackageManager {

    private static final String REPOSITORY_CACHE_NAME = ".bower";
    private static final String REPOSITORY_ID = "bower";
    private static final String REPOSITORY_URL = "https://bower.herokuapp.com";
    private static final String PACKAGES = "packages";
    private static final String PACKAGE_DESCRIPTOR = "bower.json";
    private final WagonManager wagonManager;
    private final ScmManager scmManager;
    private final Logger logger;

    public PackageManager(final WagonManager wagonManager, final ScmManager scmManager, final Logger logger) {
        this.wagonManager = wagonManager;
        this.scmManager = scmManager;
        this.logger = logger;
    }

    public void updateResolutionContext(final List<Package> packages, final ResolutionContext resolutionContext) {
        try {
            final Gson gson = new Gson();
            final JsonReader reader = new JsonReader(new FileReader(getCachedPackagesFile()));
            final Map<Package, PackageLocation> packageLocations = new HashMap<Package, PackageLocation>();
            reader.beginArray();
            while (reader.hasNext()) {
                final PackageLocation packageLocation = gson.fromJson(reader, PackageLocation.class);
                for (final Package pkg : packages) {
                    if (pkg.getName().equals(packageLocation.getName())) {
                        resolutionContext.addUnresolved(pkg, packageLocation);
                    }
                }
            }
            reader.endArray();
            reader.close();
        } catch (final IOException e) {
            logger.error(Logger.ERROR_CANNOT_PARSE_PACKAGES_FILE, e);
        }
    }

    public void updatePackageList() {
        final Repository repository = new Repository(REPOSITORY_ID, REPOSITORY_URL);
        try {
            final Wagon wagon = wagonManager.getWagon(repository);
            wagon.connect(repository);
            wagon.get(PACKAGES, getCachedPackagesFile());
            wagon.disconnect();
        } catch (final WagonException e) {
            logger.error(Logger.ERROR_CANNOT_DOWNLOAD_PACKAGES_FILE, e);
        }
    }

    public void resolve(final ResolutionContext resolutionContext) {
        final File temporaryDirectory = createTemporaryDirectory();
        while (resolutionContext.hasUnresolved()) {
            final Map.Entry<Package, PackageLocation> unresolved = resolutionContext.nextUnresolved();
            final Package pkg = unresolved.getKey();
            final PackageLocation location = unresolved.getValue();
            try {
                final ScmRepository scmRepository = scmManager.makeScmRepository("scm:git:" + location.getUrl());
                final ScmProvider scmProvider = scmManager.getProviderByRepository(scmRepository);
                final ScmVersion scmVersion = new ScmTag(pkg.getVersion());
                final File packageDirectory = new File(getBowerCacheDir(), pkg.getName());
                final File versionDirectory = new File(packageDirectory, pkg.getVersion());
                versionDirectory.mkdirs();
                final ScmFileSet bowerJsonFileSet = new ScmFileSet(temporaryDirectory);
                scmProvider.checkOut(scmRepository, bowerJsonFileSet, scmVersion, true);
                FileUtils.copyFile(new File(temporaryDirectory, PACKAGE_DESCRIPTOR), new File(versionDirectory, PACKAGE_DESCRIPTOR));
                final Gson gson = new Gson();
                final List<Package> dependencies = new ArrayList<Package>();
                final JsonReader reader = new JsonReader(new FileReader(getCachedPackagesFile()));
                reader.beginObject();
                while (reader.hasNext()) {
                    final String name = reader.nextName();
                    if ("dependencies".equals(name)) {
                        final Map<String, String> dependencyMap = gson.fromJson(reader, Map.class);
                        for (final Map.Entry<String, String> entry : dependencyMap.entrySet()) {
                            final Package dependencyPackage = new Package(entry.getKey(), entry.getValue());
                            dependencies.add(dependencyPackage);
                        }
                    } else {
                        reader.skipValue();
                    }
                }
                updateResolutionContext(dependencies, resolutionContext);
                resolutionContext.markResolved(pkg);
            } catch (final ScmException e) {
                logger.error(Logger.ERROR_CANNOT_DOWNLOAD_PACKAGE_CONTENTS, e);
                resolutionContext.resolutionError(pkg, e);
            } catch (final IOException e) {
                logger.error(Logger.ERROR_CANNOT_DOWNLOAD_PACKAGE_CONTENTS, e);
                resolutionContext.resolutionError(pkg, e);
            }
        }
    }

    public File getCachedPackagesFile() {
        return new File(getBowerCacheDir(), PACKAGES);
    }

    public File getBowerCacheDir() {
        final String userHome = System.getProperty("user.home");
        final File userHomeDir = new File(userHome);
        final File bowerCacheDir = new File(userHomeDir, REPOSITORY_CACHE_NAME);
        bowerCacheDir.mkdirs();
        return bowerCacheDir;
    }

    public File createTemporaryDirectory() {
        final File tempRoot = new File(System.getProperty("java.io.tmpdir"));
        final String tempDirName = UUID.randomUUID().toString();
        final File tempDir = new File(tempRoot, tempDirName);
        tempDir.mkdirs();
        tempDir.deleteOnExit();
        return tempDir;
    }
}
