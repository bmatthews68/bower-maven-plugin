package com.btmatthews.maven.plugins.bower;

import java.util.*;

public class ResolutionContext {

    private Set<Package> resolved = new HashSet<Package>();
    private Map<Package, PackageLocation> unresolved = new HashMap<Package, PackageLocation>();
    private Map<Package, Exception> errors = new HashMap<Package, Exception>();

    public void addUnresolved(final Package pkg, final PackageLocation pkgLocation) {
        if (!resolved.contains(pkg) && !unresolved.containsKey(pkg) && !errors.containsKey(pkg)) {
            unresolved.put(pkg, pkgLocation);
        }
    }

    public Map.Entry<Package, PackageLocation> nextUnresolved() {
        return unresolved.entrySet().iterator().next();
    }

    public boolean hasUnresolved() {
        return unresolved.size() > 0;
    }

    public void markResolved(final Package pkg) {
        resolved.add(pkg);
        unresolved.remove(pkg);
    }

    public void resolutionError(final Package pkg, final Exception e) {
        errors.put(pkg, e);
        unresolved.remove(pkg);
    }
}
