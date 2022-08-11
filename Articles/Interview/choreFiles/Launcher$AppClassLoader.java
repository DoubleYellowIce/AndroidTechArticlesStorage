// 
// Decompiled by Procyon v0.5.36
// 

package sun.misc;

import java.net.MalformedURLException;
import java.security.cert.Certificate;
import java.security.ProtectionDomain;
import java.security.AccessControlContext;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.CodeSource;
import java.net.URL;
import java.io.IOException;
import java.security.PrivilegedAction;
import java.security.AccessController;
import java.io.File;
import java.net.URLClassLoader;

static class AppClassLoader extends URLClassLoader
{
    final URLClassPath ucp;
    
    public static ClassLoader getAppClassLoader(final ClassLoader classLoader) throws IOException {
        final String property = System.getProperty("java.class.path");
        return AccessController.doPrivileged((PrivilegedAction<ClassLoader>)new AppClassLoader.Launcher$AppClassLoader$1(property, (property == null) ? new File[0] : Launcher.access$300(property), classLoader));
    }
    
    AppClassLoader(final URL[] urls, final ClassLoader parent) {
        super(urls, parent, Launcher.access$200());
        (this.ucp = SharedSecrets.getJavaNetAccess().getURLClassPath((URLClassLoader)this)).initLookupCache((ClassLoader)this);
    }
    
    public Class<?> loadClass(final String s, final boolean resolve) throws ClassNotFoundException {
        final int lastIndex = s.lastIndexOf(46);
        if (lastIndex != -1) {
            final SecurityManager securityManager = System.getSecurityManager();
            if (securityManager != null) {
                securityManager.checkPackageAccess(s.substring(0, lastIndex));
            }
        }
        if (!this.ucp.knownToNotExist(s)) {
            return super.loadClass(s, resolve);
        }
        final Class loadedClass = this.findLoadedClass(s);
        if (loadedClass != null) {
            if (resolve) {
                this.resolveClass(loadedClass);
            }
            return (Class<?>)loadedClass;
        }
        throw new ClassNotFoundException(s);
    }
    
    @Override
    protected PermissionCollection getPermissions(final CodeSource codesource) {
        final PermissionCollection permissions = super.getPermissions(codesource);
        permissions.add(new RuntimePermission("exitVM"));
        return permissions;
    }
    
    private void appendToClassPathForInstrumentation(final String pathname) {
        assert Thread.holdsLock(this);
        super.addURL(Launcher.getFileURL(new File(pathname)));
    }
    
    private static AccessControlContext getContext(final File[] array) throws MalformedURLException {
        final PathPermissions permissions = new PathPermissions(array);
        return new AccessControlContext(new ProtectionDomain[] { new ProtectionDomain(new CodeSource(permissions.getCodeBase(), (Certificate[])null), (PermissionCollection)permissions) });
    }
    
    static {
        AppClassLoader.$assertionsDisabled = !Launcher.class.desiredAssertionStatus();
        ClassLoader.registerAsParallelCapable();
    }
}
