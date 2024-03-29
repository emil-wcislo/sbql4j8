/*
 * Copyright (c) 2003, 2012, Oracle and/or its affiliates. All rights reserved.
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

package sbql4j8.com.sun.tools.javadoc;

import sbql4j8.com.sun.tools.javac.comp.*;
import sbql4j8.com.sun.tools.javac.util.*;

/**
 *  Javadoc's own todo queue doesn't queue its inputs, as javadoc
 *  doesn't perform attribution of method bodies or semantic checking.
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 *
 *  @author Neal Gafter
 */
public class JavadocTodo extends Todo {
    public static void preRegister(Context context) {
        context.put(todoKey, new Context.Factory<Todo>() {
               public Todo make(Context c) {
                   return new JavadocTodo(c);
               }
        });
    }

    protected JavadocTodo(Context context) {
        super(context);
    }

    @Override
    public void append(Env<AttrContext> e) {
        // do nothing; Javadoc doesn't perform attribution.
    }

    @Override
    public boolean offer(Env<AttrContext> e) {
        return false;
    }
}
