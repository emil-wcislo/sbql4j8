/*
 * Copyright (c) 2002, 2008, Oracle and/or its affiliates. All rights reserved.
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

package p;

/** Test that when running javadoc on a package, we only get
 *  documentation for those classes for which source was provided.
 */
public class SourceOnly extends sbql4j8.com.sun.javadoc.Doclet
{
    public static void main(String[] args) {
        // run javadoc on package p
        int result = sbql4j8.com.sun.tools.javadoc.Main.
            execute("javadoc", "p.SourceOnly", SourceOnly.class.getClassLoader(), new String[] {"p"});
        if (result != 0)
            throw new Error();
    }

    public static boolean start(sbql4j8.com.sun.javadoc.RootDoc root) {
        if (root.classes().length != 1)
            throw new Error("wrong set of classes documented: " + java.util.Arrays.asList(root.classes()));
        return true;
    }
}
