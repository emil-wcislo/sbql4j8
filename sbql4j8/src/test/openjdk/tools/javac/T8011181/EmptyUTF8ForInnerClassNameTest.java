/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @bug 8011181
 * @summary javac, empty UTF8 entry generated for inner class
 */

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import sbql4j8.com.sun.tools.javac.util.Assert;
import sbql4j8.com.sun.tools.classfile.ClassFile;

import static sbql4j8.com.sun.tools.classfile.ConstantPool.CONSTANT_Utf8;
import static sbql4j8.com.sun.tools.classfile.ConstantPool.CONSTANT_Utf8_info;
import static sbql4j8.com.sun.tools.classfile.ConstantPool.CPInfo;

public class EmptyUTF8ForInnerClassNameTest {

    public static void main(String[] args) throws Exception {
        new EmptyUTF8ForInnerClassNameTest().run();
    }

    void run() throws Exception {
        checkClassFile(Paths.get(System.getProperty("test.classes"),
                this.getClass().getName() + "$1.class"));
        checkClassFile(Paths.get(System.getProperty("test.classes"),
                this.getClass().getName() + "$EnumPlusSwitch.class"));
    }

    void checkClassFile(final Path path) throws Exception {
        ClassFile classFile = ClassFile.read(
                new BufferedInputStream(Files.newInputStream(path)));
        for (CPInfo cpInfo : classFile.constant_pool.entries()) {
            if (cpInfo.getTag() == CONSTANT_Utf8) {
                CONSTANT_Utf8_info utf8Info = (CONSTANT_Utf8_info)cpInfo;
                Assert.check(utf8Info.value.length() > 0,
                        "UTF8 with length 0 found at class " + classFile.getName());
            }
        }
    }

    static class EnumPlusSwitch {
        enum E {E1}

        public int m (E e) {
            switch (e) {
                case E1:
                    return 0;
            }
            return -1;
        }
    }

}
