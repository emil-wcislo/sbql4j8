/*
 * Copyright (c) 1999, 2012, Oracle and/or its affiliates. All rights reserved.
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

package sbql4j8.com.sun.tools.doclets.internal.toolkit.util;

import sbql4j8.com.sun.javadoc.*;

/**
 * This class is useful for searching a method which has documentation
 * comment and documentation tags. The method is searched in all the
 * superclasses and interfaces(subsequently super-interfaces also)
 * recursively.
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public abstract class MethodFinder {

    abstract boolean isCorrectMethod(MethodDoc method);

    public MethodDoc search(ClassDoc cd, MethodDoc method) {
        MethodDoc meth = searchInterfaces(cd, method);
        if (meth != null) {
            return meth;
        }
        ClassDoc icd = cd.superclass();
        if (icd != null) {
            meth = Util.findMethod(icd, method);
            if (meth != null) {
            if (isCorrectMethod(meth)) {
                    return meth;
                }
            }
            return search(icd, method);
        }
        return null;
    }

    public MethodDoc searchInterfaces(ClassDoc cd, MethodDoc method) {
        MethodDoc[] implementedMethods = (new ImplementedMethods(method, null)).build();
        for (int i = 0; i < implementedMethods.length; i++) {
            if (isCorrectMethod(implementedMethods[i])) {
                return implementedMethods[i];
            }
        }
        return null;
    }
}
