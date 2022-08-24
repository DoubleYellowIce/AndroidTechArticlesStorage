// 
// Decompiled by Procyon v0.5.36
// 

package sun.misc;

import java.security.PermissionCollection;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.security.ProtectionDomain;
import java.security.AccessControlContext;
import java.net.URI;
import java.nio.file.Paths;
import java.net.URISyntaxException;
import java.util.Vector;
import java.util.StringTokenizer;
import java.io.File;
import java.net.URL;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.AccessController;
import java.io.IOException;
import java.net.URLClassLoader;

static class ExtClassLoader extends URLClassLoader
{
    private static volatile ExtClassLoader instance;
    
    public static ExtClassLoader getExtClassLoader() throws IOException {
        if (ExtClassLoader.instance == null) {
            synchronized (ExtClassLoader.class) {
                if (ExtClassLoader.instance == null) {
                    ExtClassLoader.instance = createExtClassLoader();
                }
            }
        }
        return ExtClassLoader.instance;
    }
    
    private static ExtClassLoader createExtClassLoader() throws IOException {
        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<ExtClassLoader>)new ExtClassLoader.Launcher$ExtClassLoader$1());
        }
        catch (PrivilegedActionException ex) {
            throw (IOException)ex.getException();
        }
    }
    
    void addExtURL(final URL url) {
        super.addURL(url);
    }
    
    public ExtClassLoader(final File[] array) throws IOException {
        super(getExtURLs(array), null, Launcher.access$200());
        SharedSecrets.getJavaNetAccess().getURLClassPath((URLClassLoader)this).initLookupCache((ClassLoader)this);
    }
    
    private static File[] getExtDirs() {
        final String property = System.getProperty("java.ext.dirs");
        File[] array;
        if (property != null) {
            final StringTokenizer stringTokenizer = new StringTokenizer(property, File.pathSeparator);
            final int countTokens = stringTokenizer.countTokens();
            array = new File[countTokens];
            for (int i = 0; i < countTokens; ++i) {
                array[i] = new File(stringTokenizer.nextToken());
            }
        }
        else {
            array = new File[0];
        }
        return array;
    }
    
    private static URL[] getExtURLs(final File[] array) throws IOException {
        final Vector<URL> vector = new Vector<URL>();
        for (int i = 0; i < array.length; ++i) {
            final String[] list = array[i].list();
            if (list != null) {
                for (int j = 0; j < list.length; ++j) {
                    if (!list[j].equals("meta-index")) {
                        vector.add(Launcher.getFileURL(new File(array[i], list[j])));
                    }
                }
            }
        }
        final URL[] anArray = new URL[vector.size()];
        vector.copyInto(anArray);
        return anArray;
    }
    
    public String findLibrary(String mapLibraryName) {
        mapLibraryName = System.mapLibraryName(mapLibraryName);
        final URL[] urLs = super.getURLs();
        Object obj = null;
        for (int i = 0; i < urLs.length; ++i) {
            URI uri;
            try {
                uri = urLs[i].toURI();
            }
            catch (URISyntaxException ex) {
                continue;
            }
            final File parentFile = Paths.get(uri).toFile().getParentFile();
            if (parentFile != null && !parentFile.equals(obj)) {
                final String savedProperty = VM.getSavedProperty("os.arch");
                if (savedProperty != null) {
                    final File file = new File(new File(parentFile, savedProperty), mapLibraryName);
                    if (file.exists()) {
                        return file.getAbsolutePath();
                    }
                }
                final File file2 = new File(parentFile, mapLibraryName);
                if (file2.exists()) {
                    return file2.getAbsolutePath();
                }
            }
            obj = parentFile;
        }
        return null;
    }
    
    private static AccessControlContext getContext(final File[] array) throws IOException {
        final PathPermissions permissions = new PathPermissions(array);
        return new AccessControlContext(new ProtectionDomain[] { new ProtectionDomain(new CodeSource(permissions.getCodeBase(), (Certificate[])null), (PermissionCollection)permissions) });
    }
    
    static {
        ClassLoader.registerAsParallelCapable();
        ExtClassLoader.instance = null;
    }
}
