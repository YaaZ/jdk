/*
 * Copyright (c) 2019, 2025, Oracle and/or its affiliates. All rights reserved.
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

package javax.lang.model.util;

import jdk.internal.javac.PreviewFeature;

import javax.annotation.processing.SupportedSourceVersion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.*;
import static javax.lang.model.SourceVersion.*;

/**
 * A visitor of types based on their {@linkplain TypeKind kind} with
 * default behavior appropriate for a {@linkplain
 * ProcessingEnvironment#isPreviewEnabled preview} source version.
 *
 * For {@linkplain
 * TypeMirror types} <code><i>Xyz</i></code> that may have more than one
 * kind, the <code>visit<i>Xyz</i></code> methods in this class delegate
 * to the <code>visit<i>Xyz</i>As<i>Kind</i></code> method corresponding to the
 * first argument's kind.  The <code>visit<i>Xyz</i>As<i>Kind</i></code> methods
 * call {@link #defaultAction defaultAction}, passing their arguments
 * to {@code defaultAction}'s corresponding parameters.
 *
 * @apiNote
 * Methods in this class may be overridden subject to their general
 * contract.
 *
 * @param <R> the return type of this visitor's methods.  Use {@link
 *            Void} for visitors that do not need to return results.
 * @param <P> the type of the additional parameter to this visitor's
 *            methods.  Use {@code Void} for visitors that do not need an
 *            additional parameter.
 *
 * @see javax.lang.model.util##expectedEvolution
 * <strong>Expected visitor evolution</strong>
 * @see AbstractAnnotationValueVisitor6##note_for_subclasses
 * <strong>Compatibility note for subclasses</strong>
 * @see TypeKindVisitor6
 * @see TypeKindVisitor7
 * @see TypeKindVisitor8
 * @see TypeKindVisitor9
 * @see TypeKindVisitor14
 * @since 23
 */
@SupportedSourceVersion(RELEASE_26)
@PreviewFeature(feature=PreviewFeature.Feature.LANGUAGE_MODEL, reflective=true)
public class TypeKindVisitorPreview<R, P> extends TypeKindVisitor14<R, P> {
    /**
     * Constructor for concrete subclasses to call; uses {@code null}
     * for the default value.
     */
    protected TypeKindVisitorPreview() {
        super(null);
    }

    /**
     * Constructor for concrete subclasses to call; uses the argument
     * for the default value.
     *
     * @param defaultValue the value to assign to {@link #DEFAULT_VALUE}
     */
    protected TypeKindVisitorPreview(R defaultValue) {
        super(defaultValue);
    }
}
