/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
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
 * @bug 6893932 6990390
 * @summary javah help screen lists -h and -? but does not accept them
 */

import java.io.*;
import java.util.*;

public class TestHelpOpts {
    public static void main(String... args) throws Exception {
        new TestHelpOpts().run();
    }

    void run() throws Exception {
        Locale prev = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ENGLISH);

            String[] opts = { "-h", "-help", "-?", "--help" };
            for (String opt: opts)
                test(opt);
        } finally {
            Locale.setDefault(prev);
        }

        if (errors > 0)
            throw new Exception(errors + " errors occurred");
    }

    void test(String opt) {
        System.err.println("test " + opt);
        String[] args = { opt };

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        int rc = sbql4j8.com.sun.tools.javah.Main.run(args, pw);
        pw.close();
        String out = sw.toString();
        if (!out.isEmpty())
            System.err.println(out);
        if (rc != 0)
            error("Unexpected exit: rc=" + rc);

        String flat = out.replaceAll("\\s+", " "); // canonicalize whitespace
        if (!flat.contains("Usage: javah [options] <classes> where [options] include:"))
            error("expected text not found");
        if (flat.contains("main.opt"))
            error("key not found in resource bundle: " + flat.replaceAll(".*(main.opt.[^ ]*).*", "$1"));
    }

    void error(String msg) {
        System.err.println(msg);
        errors++;
    }

    int errors;
}
