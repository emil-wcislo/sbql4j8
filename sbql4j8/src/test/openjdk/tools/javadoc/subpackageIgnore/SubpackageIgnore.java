/*
 * Copyright (c) 2003, 2008, Oracle and/or its affiliates. All rights reserved.
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
 * @bug 4773013
 * @summary When hunting subpackages, silently ignore any directory name that
 *          can't be part of a subpackage.
 */

import sbql4j8.com.sun.javadoc.*;

public class SubpackageIgnore extends Doclet {

    public static void main(String[] args) {
        if (sbql4j8.com.sun.tools.javadoc.Main.execute(
                "javadoc",
                "SubpackageIgnore",
                SubpackageIgnore.class.getClassLoader(),
                new String[] {"-Xwerror",
                              "-sourcepath",
                              System.getProperty("test.src", "."),
                              "-subpackages",
                              "pkg1"}) != 0)
            throw new Error("Javadoc encountered warnings or errors.");
    }

    /*
     * The world's simplest doclet.
     */
    public static boolean start(RootDoc root) {
        return true;
    }
}
