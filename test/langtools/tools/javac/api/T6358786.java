/*
 * Copyright (c) 2005, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

/*
 * @test
 * @bug     6358786
 * @summary Doccomments are not returned from Tree API
 * @author  Peter von der Ahé
 * @modules jdk.compiler/com.sun.tools.javac.api
 * @run main T6358786 T6358786.java
 */

import java.io.*;
import java.util.*;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.tools.*;

import com.sun.tools.javac.api.JavacTaskImpl;

/**
 * Tests that doccomments are available from the Tree API.
 */
public class T6358786 {
    public static void main(String... args) throws IOException {
        JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        try (StandardJavaFileManager fm = tool.getStandardFileManager(null, null, null)) {
            String srcdir = System.getProperty("test.src");
            File file = new File(srcdir, args[0]);
            List<String> options = Arrays.asList(
                "--add-exports", "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED"
            );
            JavacTaskImpl task = (JavacTaskImpl)tool.getTask(null, fm, null, options, null, fm.getJavaFileObjectsFromFiles(Arrays.asList(file)));
            Elements elements = task.getElements();
            for (Element clazz : task.enter(task.parse())) {
                String doc = elements.getDocComment(clazz);
                if (doc == null)
                    throw new AssertionError(clazz.getSimpleName() + ": no doc comment");
                System.out.format("%s: %s%n", clazz.getSimpleName(), doc);
            }
        }
    }
}
