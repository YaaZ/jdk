/*
 * Copyright (c) 1997, 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.security;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * This class extends {@code ClassLoader} with additional support for defining
 * classes with an associated code source and permissions.
 *
 * @apiNote
 * Permissions cannot be used for controlling access to resources
 * as the Security Manager is no longer supported.
 *
 * @author  Li Gong
 * @author  Roland Schemers
 * @since 1.2
 */
public class SecureClassLoader extends ClassLoader {

    /*
     * Map that maps the CodeSource to a ProtectionDomain. The key is a
     * CodeSourceKey class that uses a {@code String} instead of a URL to avoid
     * potential expensive name service lookups. This does mean that URLs that
     * are equivalent after nameservice lookup will be placed in separate
     * ProtectionDomains; however during policy enforcement these URLs will be
     * canonicalized and resolved resulting in a consistent set of granted
     * permissions.
     */
    private final Map<CodeSourceKey, ProtectionDomain> pdcache
            = new ConcurrentHashMap<>(11);

    static {
        ClassLoader.registerAsParallelCapable();
    }

    /**
     * Creates a new {@code SecureClassLoader} using the specified parent
     * class loader for delegation.
     *
     * @apiNote If {@code parent} is specified as {@code null} (for the
     * bootstrap class loader) then there is no guarantee that all platform
     * classes are visible.
     * See {@linkplain ClassLoader##builtinLoaders Run-time Built-in Class Loaders}
     * for information on the bootstrap class loader and other built-in class loaders.
     *
     * @param parent the parent ClassLoader, can be {@code null} for the bootstrap
     *               class loader
     */
    protected SecureClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * Creates a new {@code SecureClassLoader} using the
     * {@linkplain ClassLoader#getSystemClassLoader() system class loader as the parent}.
     */
    protected SecureClassLoader() {
        super();
    }

    /**
     * Creates a new {@code SecureClassLoader} of the specified name and
     * using the specified parent class loader for delegation.
     *
     * @apiNote If {@code parent} is specified as {@code null} (for the
     * bootstrap class loader) then there is no guarantee that all platform
     * classes are visible.
     * See {@linkplain ClassLoader##builtinLoaders Run-time Built-in Class Loaders}
     * for information on the bootstrap class loader and other built-in class loaders.
     *
     * @param name class loader name; or {@code null} if not named
     * @param parent the parent class loader, can be {@code null} for the bootstrap
     *               class loader
     *
     * @throws IllegalArgumentException if the given name is empty.
     *
     * @since 9
     */
    protected SecureClassLoader(String name, ClassLoader parent) {
        super(name, parent);
    }

    /**
     * Converts an array of bytes into an instance of class {@code Class},
     * with an optional CodeSource. Before the
     * class can be used it must be resolved.
     * <p>
     * If a non-null CodeSource is supplied a ProtectionDomain is
     * constructed and associated with the class being defined.
     *
     * @param      name the expected name of the class, or {@code null}
     *                  if not known, using '.' and not '/' as the separator
     *                  and without a trailing ".class" suffix.
     * @param      b    the bytes that make up the class data. The bytes in
     *             positions {@code off} through {@code off+len-1}
     *             should have the format of a valid class file as defined by
     *             <cite>The Java Virtual Machine Specification</cite>.
     * @param      off  the start offset in {@code b} of the class data
     * @param      len  the length of the class data
     * @param      cs   the associated CodeSource, or {@code null} if none
     * @return the {@code Class} object created from the data,
     *         and optional CodeSource.
     * @throws     ClassFormatError if the data did not contain a valid class
     * @throws     IndexOutOfBoundsException if either {@code off} or
     *             {@code len} is negative, or if
     *             {@code off+len} is greater than {@code b.length}.
     *
     * @throws     SecurityException if an attempt is made to add this class
     *             to a package that contains classes that were signed by
     *             a different set of certificates than this class, or if
     *             the class name begins with "java.".
     */
    protected final Class<?> defineClass(String name,
                                         byte[] b, int off, int len,
                                         CodeSource cs)
    {
        return defineClass(name, b, off, len, getProtectionDomain(cs));
    }

    /**
     * Converts a {@link java.nio.ByteBuffer ByteBuffer}
     * into an instance of class {@code Class}, with an optional CodeSource.
     * Before the class can be used it must be resolved.
     * <p>
     * If a non-null CodeSource is supplied a ProtectionDomain is
     * constructed and associated with the class being defined.
     *
     * @param      name the expected name of the class, or {@code null}
     *                  if not known, using '.' and not '/' as the separator
     *                  and without a trailing ".class" suffix.
     * @param      b    the bytes that make up the class data.  The bytes from positions
     *                  {@code b.position()} through {@code b.position() + b.limit() -1}
     *                  should have the format of a valid class file as defined by
     *                  <cite>The Java Virtual Machine Specification</cite>.
     * @param      cs   the associated CodeSource, or {@code null} if none
     * @return the {@code Class} object created from the data,
     *         and optional CodeSource.
     * @throws     ClassFormatError if the data did not contain a valid class
     * @throws     SecurityException if an attempt is made to add this class
     *             to a package that contains classes that were signed by
     *             a different set of certificates than this class, or if
     *             the class name begins with "java.".
     *
     * @since  1.5
     */
    protected final Class<?> defineClass(String name, java.nio.ByteBuffer b,
                                         CodeSource cs)
    {
        return defineClass(name, b, getProtectionDomain(cs));
    }

    /**
     * Returns the permissions for the given CodeSource object.
     * <p>
     * This method is invoked by the defineClass method which takes
     * a CodeSource as an argument when it is constructing the
     * ProtectionDomain for the class being defined.
     *
     * @param codesource the codesource.
     *
     * @return the permissions for the codesource.
     *
     */
    protected PermissionCollection getPermissions(CodeSource codesource)
    {
        return new Permissions(); // ProtectionDomain defers the binding
    }

    /*
     * Returned cached ProtectionDomain for the specified CodeSource.
     */
    private ProtectionDomain getProtectionDomain(CodeSource cs) {
        if (cs == null) {
            return null;
        }

        // Use a CodeSourceKey object key. It should behave in the
        // same manner as the CodeSource when compared for equality except
        // that no nameservice lookup is done on the hostname (String comparison
        // only), and the fragment is not considered.
        CodeSourceKey key = new CodeSourceKey(cs);
        return pdcache.computeIfAbsent(key, new Function<>() {
            // Do not turn this into a lambda since it is executed during bootstrap
            @Override
            public ProtectionDomain apply(CodeSourceKey key) {
                PermissionCollection perms
                        = SecureClassLoader.this.getPermissions(key.cs);
                ProtectionDomain pd = new ProtectionDomain(
                        key.cs, perms, SecureClassLoader.this, null);
                return pd;
            }
        });
    }

    private record CodeSourceKey(CodeSource cs) {

        @Override
        public int hashCode() {
            return Objects.hashCode(cs.getLocationNoFragString());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            return obj instanceof CodeSourceKey other
                    && Objects.equals(cs.getLocationNoFragString(),
                                other.cs.getLocationNoFragString())
                    && cs.matchCerts(other.cs, true);
        }
    }

    /**
     * Called by the VM, during -Xshare:dump
     */
    private void resetArchivedStates() {
        pdcache.clear();
    }
}
