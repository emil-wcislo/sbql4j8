/*
 * Copyright (c) 2009, 2013, Oracle and/or its affiliates. All rights reserved.
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

import static sbql4j8.com.sun.tools.classfile.TypeAnnotation.TargetType.*;

/*
 * @test
 * @summary Test population of reference info for method parameters
 * @compile -g Driver.java ReferenceInfoUtil.java MethodParameters.java
 * @run main Driver MethodParameters
 */
public class MethodParameters {

    @TADescription(annotation = "TA", type = METHOD_FORMAL_PARAMETER, paramIndex = 0)
    public String methodParamAsPrimitive() {
        return "void test(@TA int a) { }";
    }

    @TADescription(annotation = "TA", type = METHOD_FORMAL_PARAMETER, paramIndex = 1)
    public String methodParamAsObject() {
        return "void test(Object b, @TA Object a) { }";
    }

    @TADescriptions({
        @TADescription(annotation = "TA", type = METHOD_FORMAL_PARAMETER, paramIndex = 0),
        @TADescription(annotation = "TB", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 3, 0 }, paramIndex = 0),
        @TADescription(annotation = "TC", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 3, 1 }, paramIndex = 0),
        @TADescription(annotation = "TD", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 3, 1, 3, 0 }, paramIndex = 0)
    })
    public String methodParamAsParametrized() {
        return "void test(@TA Map<@TB String, @TC List<@TD String>> a) { }";
    }

    @TADescriptions({
        @TADescription(annotation = "TA", type = METHOD_FORMAL_PARAMETER, paramIndex = 0),
        @TADescription(annotation = "TB", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 3, 0 }, paramIndex = 0),
        @TADescription(annotation = "TC", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 3, 0, 2, 0 }, paramIndex = 0),
        @TADescription(annotation = "TD", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 3, 1 }, paramIndex = 0),
        @TADescription(annotation = "TE", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 3, 1, 3, 0 }, paramIndex = 0),
        @TADescription(annotation = "TF", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 3, 1, 3, 0, 2, 0 }, paramIndex = 0),
        @TADescription(annotation = "TG", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 3, 1, 3, 0, 2, 0, 3, 0 }, paramIndex = 0),
        @TADescription(annotation = "TH", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 3, 1, 3, 0, 2, 0, 3, 0, 2, 0 }, paramIndex = 0),
        @TADescription(annotation = "TI", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 3, 1, 3, 0, 2, 0, 3, 1 }, paramIndex = 0),
        @TADescription(annotation = "TJ", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 3, 1, 3, 0, 2, 0, 3, 1, 2, 0 }, paramIndex = 0)
    })
    public String methodParamAsWildcard() {
        return "void test(@TA Map<@TB ? extends @TC String," +
                "                 @TD List<@TE ? extends @TF Map<@TG ? super @TH String," +
                "                                                @TI ? extends @TJ Object>>> a) { }";
    }

    @TADescriptions({
        @TADescription(annotation = "TA", type = METHOD_FORMAL_PARAMETER, paramIndex = 1),
        @TADescription(annotation = "TB", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 0, 0 }, paramIndex = 1),
        @TADescription(annotation = "TC", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 0, 0, 0, 0 }, paramIndex = 1)
    })
    public String methodParamAsArray() {
        return "void test(Object b, @TC String @TA [] @TB [] a) { }";
    }

    @TADescriptions({
        @TADescription(annotation = "TA", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 0, 0 }, paramIndex = 1),
        @TADescription(annotation = "TB", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 0, 0 }, paramIndex = 1)
    })
    public String methodParamAsArray2() {
        return "void test(Object b, @TA @TB String [] a) { }";
    }

    @TADescriptions({
        @TADescription(annotation = "TA", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 0, 0 }, paramIndex = 1),
        @TADescription(annotation = "TB", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 0, 0 }, paramIndex = 1),
        @TADescription(annotation = "TC", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 0, 0 }, paramIndex = 1)
    })
    public String methodParamAsArray3() {
        return "void test(Object b, @TA @TB @TC String [] a) { }";
    }

    @TADescriptions({
        @TADescription(annotation = "TA", type = METHOD_FORMAL_PARAMETER, paramIndex = 1),
        @TADescription(annotation = "TB", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 0, 0 }, paramIndex = 1),
        @TADescription(annotation = "TC", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 0, 0, 0, 0 }, paramIndex = 1)
    })
    public String methodParamAsVararg() {
        return "void test(Object b, @TC String @TA [] @TB ... a) { }";
    }

    @TADescriptions({
        @TADescription(annotation = "TA", type = METHOD_FORMAL_PARAMETER, paramIndex = 1),
        @TADescription(annotation = "TB", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 0, 0 }, paramIndex = 1),
        @TADescription(annotation = "TC", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 0, 0, 0, 0 }, paramIndex = 1)
    })
    public String methodParamAsFQVararg() {
        return "void test(Object b, java.lang.@TC String @TA [] @TB ... a) { }";
    }

    @TADescriptions({})
    public String methodWithDeclarationAnnotatin() {
        return "void test(@Decl String a) { }";
    }

    @TADescriptions({})
    public String methodWithNoTargetAnno() {
        return "void test(@A String a) { }";
    }

    // Smoke tests
    @TADescription(annotation = "TA", type = METHOD_FORMAL_PARAMETER, paramIndex = 0)
    public String interfacemethodParamAsObject() {
        return "interface Test { void test(@TA Object a); }";
    }

    @TADescription(annotation = "TA", type = METHOD_FORMAL_PARAMETER, paramIndex = 2)
    public String abstractmethodParamAsObject() {
        return "abstract class Test { abstract void test(Object b, Object c, @TA Object a); }";
    }

    @TADescriptions({
        @TADescription(annotation = "TA", type = METHOD_FORMAL_PARAMETER, paramIndex = 0),
        @TADescription(annotation = "TB", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 3, 0 }, paramIndex = 0),
        @TADescription(annotation = "TC", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 3, 1 }, paramIndex = 0),
        @TADescription(annotation = "TD", type = METHOD_FORMAL_PARAMETER,
                genericLocation = { 3, 1, 3, 0 }, paramIndex = 0)
    })
    public String interfacemethodParamAsParametrized() {
        return "interface Test { void test(@TA Map<@TB String, @TC List<@TD String>> a); }";
    }

}
