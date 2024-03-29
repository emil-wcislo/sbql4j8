/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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
 * @bug 6493690
 * @summary javadoc should have a javax.tools.Tool service provider
 * @build APITest
 * @run main DocletPathTest
 */

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import javax.tools.DocumentationTool;
import javax.tools.DocumentationTool.DocumentationTask;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 * Tests for locating a doclet via the file manager's DOCLET_PATH.
 */
public class DocletPathTest extends APITest {
    public static void main(String... args) throws Exception {
        new DocletPathTest().run();
    }

    /**
     * Verify that an alternate doclet can be specified, and located via
     * the file manager's DOCLET_PATH.
     */
    @Test
    public void testDocletPath() throws Exception {
        JavaFileObject docletSrc =
                createSimpleJavaFileObject("DocletOnDocletPath", docletSrcText);
        File docletDir = getOutDir("classes");
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager cfm = compiler.getStandardFileManager(null, null, null);
        cfm.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(docletDir));
        Iterable<? extends JavaFileObject> cfiles = Arrays.asList(docletSrc);
        if (!compiler.getTask(null, cfm, null, null, null, cfiles).call())
            throw new Exception("cannot compile doclet");

        JavaFileObject srcFile = createSimpleJavaFileObject();
        DocumentationTool tool = ToolProvider.getSystemDocumentationTool();
        StandardJavaFileManager fm = tool.getStandardFileManager(null, null, null);
        File outDir = getOutDir("api");
        fm.setLocation(DocumentationTool.Location.DOCUMENTATION_OUTPUT, Arrays.asList(outDir));
        fm.setLocation(DocumentationTool.Location.DOCLET_PATH, Arrays.asList(docletDir));
        Iterable<? extends JavaFileObject> files = Arrays.asList(srcFile);
        Iterable<String> options = Arrays.asList("-doclet", "DocletOnDocletPath");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        DocumentationTask t = tool.getTask(pw, fm, null, null, options, files);
        boolean ok = t.call();
        String out = sw.toString();
        System.err.println(">>" + out + "<<");
        if (ok) {
            if (out.contains(TEST_STRING)) {
                System.err.println("doclet executed as expected");
            } else {
                error("test string not found in doclet output");
            }
        } else {
            error("task failed");
        }
    }

    private static final String TEST_STRING = "DocletOnDocletPath found and running";

    private static final String docletSrcText =
        "import sbql4j8.com.sun.javadoc.*;\n" +
        "public class DocletOnDocletPath {\n" +
        "    public static boolean start(RootDoc doc) {\n" +
        "        doc.printNotice(\"" + TEST_STRING + "\");\n" +
        "        return true;\n" +
        "    }\n" +
        "    public static int optionLength(String option) { return 0; }\n" +
        "    public static boolean validOptions(String options[][],\n" +
        "            DocErrorReporter reporter) { return true; }\n" +
        "    public static LanguageVersion languageVersion() {\n" +
        "        return LanguageVersion.JAVA_1_1;\n" +
        "    }\n" +
        "}\n";
}

