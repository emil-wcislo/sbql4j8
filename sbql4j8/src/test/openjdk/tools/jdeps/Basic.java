/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * @bug 8003562 8005428 8015912 8027481
 * @summary Basic tests for jdeps tool
 * @build Test p.Foo p.Bar javax.activity.NotCompactProfile
 * @run main Basic
 */

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;
import static java.nio.file.StandardCopyOption.*;

public class Basic {
    private static boolean symbolFileExist = initProfiles();
    private static boolean initProfiles() {
        // check if ct.sym exists; if not use the profiles.properties file
        Path home = Paths.get(System.getProperty("java.home"));
        if (home.endsWith("jre")) {
            home = home.getParent();
        }
        Path ctsym = home.resolve("lib").resolve("ct.sym");
        boolean symbolExists = ctsym.toFile().exists();
        if (!symbolExists) {
            Path testSrcProfiles =
                Paths.get(System.getProperty("test.src", "."), "profiles.properties");
            if (!testSrcProfiles.toFile().exists())
                throw new Error(testSrcProfiles + " does not exist");
            System.out.format("%s doesn't exist.%nUse %s to initialize profiles info%n",
                ctsym, testSrcProfiles);
            System.setProperty("jdeps.profiles", testSrcProfiles.toString());
        }
        return symbolExists;
    }

    public static void main(String... args) throws Exception {
        int errors = 0;
        errors += new Basic().run();
        if (errors > 0)
            throw new Exception(errors + " errors found");
    }

    int run() throws IOException {
        File testDir = new File(System.getProperty("test.classes", "."));
        // test a .class file
        test(new File(testDir, "Test.class"),
             new String[] {"java.lang", "p"},
             new String[] {"compact1", "not found"});
        // test a directory
        // also test non-SE javax.activity class dependency
        test(new File(testDir, "p"),
             new String[] {"java.lang", "java.util", "java.lang.management", "javax.activity", "javax.crypto"},
             new String[] {"compact1", "compact1", "compact3", testDir.getName(), "compact1"},
             new String[] {"-classpath", testDir.getPath()});
        // test class-level dependency output
        test(new File(testDir, "Test.class"),
             new String[] {"java.lang.Object", "java.lang.String", "p.Foo", "p.Bar"},
             new String[] {"compact1", "compact1", "not found", "not found"},
             new String[] {"-verbose:class"});
        // test -p option
        test(new File(testDir, "Test.class"),
             new String[] {"p.Foo", "p.Bar"},
             new String[] {"not found", "not found"},
             new String[] {"-verbose:class", "-p", "p"});
        // test -e option
        test(new File(testDir, "Test.class"),
             new String[] {"p.Foo", "p.Bar"},
             new String[] {"not found", "not found"},
             new String[] {"-verbose:class", "-e", "p\\..*"});
        test(new File(testDir, "Test.class"),
             new String[] {"java.lang"},
             new String[] {"compact1"},
             new String[] {"-verbose:package", "-e", "java\\.lang\\..*"});
        // test -classpath and -include options
        test(null,
             new String[] {"java.lang", "java.util",
                           "java.lang.management", "javax.crypto"},
             new String[] {"compact1", "compact1", "compact3", "compact1"},
             new String[] {"-classpath", testDir.getPath(), "-include", "p.+|Test.class"});
        test(new File(testDir, "Test.class"),
             new String[] {"java.lang.Object", "java.lang.String", "p.Foo", "p.Bar"},
             new String[] {"compact1", "compact1", testDir.getName(), testDir.getName()},
             new String[] {"-v", "-classpath", testDir.getPath(), "Test.class"});

        // split package p - move p/Foo.class to dir1 and p/Bar.class to dir2
        Path testClassPath = testDir.toPath();
        Path dirP = testClassPath.resolve("p");
        Path dir1 = testClassPath.resolve("dir1");
        Path subdir1P = dir1.resolve("p");
        Path dir2 = testClassPath.resolve("dir2");
        Path subdir2P = dir2.resolve("p");
        if (!Files.exists(subdir1P))
            Files.createDirectories(subdir1P);
        if (!Files.exists(subdir2P))
            Files.createDirectories(subdir2P);
        Files.move(dirP.resolve("Foo.class"), subdir1P.resolve("Foo.class"), REPLACE_EXISTING);
        Files.move(dirP.resolve("Bar.class"), subdir2P.resolve("Bar.class"), REPLACE_EXISTING);
        StringBuilder cpath = new StringBuilder(testDir.toString());
        cpath.append(File.pathSeparator).append(dir1.toString());
        cpath.append(File.pathSeparator).append(dir2.toString());
        test(new File(testDir, "Test.class"),
             new String[] {"java.lang.Object", "java.lang.String", "p.Foo", "p.Bar"},
             new String[] {"compact1", "compact1", dir1.toFile().getName(), dir2.toFile().getName()},
             new String[] {"-v", "-classpath", cpath.toString(), "Test.class"});
        return errors;
    }

    void test(File file, String[] expect, String[] profiles) {
        test(file, expect, profiles, new String[0]);
    }

    void test(File file, String[] expect, String[] profiles, String[] options) {
        List<String> args = new ArrayList<>(Arrays.asList(options));
        if (file != null) {
            args.add(file.getPath());
        }
        List<String> argsWithDashP = new ArrayList<>();
        argsWithDashP.add("-P");
        argsWithDashP.addAll(args);
        // test without -P
        checkResult("dependencies", expect, jdeps(args.toArray(new String[0])).keySet());
        // test with -P
        checkResult("profiles", expect, profiles, jdeps(argsWithDashP.toArray(new String[0])));
    }

    Map<String,String> jdeps(String... args) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        System.err.println("jdeps " + Arrays.toString(args));
        int rc = sbql4j8.com.sun.tools.jdeps.Main.run(args, pw);
        pw.close();
        String out = sw.toString();
        if (!out.isEmpty())
            System.err.println(out);
        if (rc != 0)
            throw new Error("jdeps failed: rc=" + rc);
        return findDeps(out);
    }

    // Pattern used to parse lines
    private static Pattern linePattern = Pattern.compile(".*\r?\n");
    private static Pattern pattern = Pattern.compile("\\s+ -> (\\S+) +(.*)");

    // Use the linePattern to break the given String into lines, applying
    // the pattern to each line to see if we have a match
    private static Map<String,String> findDeps(String out) {
        Map<String,String> result = new LinkedHashMap<>();
        Matcher lm = linePattern.matcher(out);  // Line matcher
        Matcher pm = null;                      // Pattern matcher
        int lines = 0;
        while (lm.find()) {
            lines++;
            CharSequence cs = lm.group();       // The current line
            if (pm == null)
                pm = pattern.matcher(cs);
            else
                pm.reset(cs);
            if (pm.find())
                result.put(pm.group(1), pm.group(2).trim());
            if (lm.end() == out.length())
                break;
        }
        return result;
    }

    void checkResult(String label, String[] expect, Collection<String> found) {
        List<String> list = Arrays.asList(expect);
        if (!isEqual(list, found))
            error("Unexpected " + label + " found: '" + found + "', expected: '" + list + "'");
    }

    void checkResult(String label, String[] expect, String[] profiles, Map<String,String> result) {
        if (expect.length != profiles.length)
            error("Invalid expected names and profiles");

        // check the dependencies
        checkResult(label, expect, result.keySet());
        // check profile information
        checkResult(label, profiles, result.values());
        for (int i=0; i < expect.length; i++) {
            String profile = result.get(expect[i]);
            if (!profile.equals(profiles[i]))
                error("Unexpected profile: '" + profile + "', expected: '" + profiles[i] + "'");
        }
    }

    boolean isEqual(List<String> expected, Collection<String> found) {
        if (expected.size() != found.size())
            return false;

        List<String> list = new ArrayList<>(found);
        list.removeAll(expected);
        return list.isEmpty();
    }

    void error(String msg) {
        System.err.println("Error: " + msg);
        errors++;
    }

    int errors;
}
