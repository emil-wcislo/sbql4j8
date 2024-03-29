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
 * @bug 7021614
 * @summary extend sbql4j8.com.sun.source API to support parsing javadoc comments
 * @build DocCommentTester
 * @run main DocCommentTester CodeTest.java
 */

class CodeTest {
    /** {@code if (a < b) { }} */
    void minimal() { }
/*
DocComment[DOC_COMMENT, pos:0
  firstSentence: 1
    Literal[CODE, pos:0, if_(a_<_b)_{_}]
  body: empty
  block tags: empty
]
*/

    /** [{@code if (a < b) { }}] */
    void in_brackets() { }
/*
DocComment[DOC_COMMENT, pos:0
  firstSentence: 3
    Text[TEXT, pos:0, []
    Literal[CODE, pos:1, if_(a_<_b)_{_}]
    Text[TEXT, pos:23, ]]
  body: empty
  block tags: empty
]
*/

    /** [ {@code if (a < b) { }} ] */
    void in_brackets_with_whitespace() { }
/*
DocComment[DOC_COMMENT, pos:0
  firstSentence: 3
    Text[TEXT, pos:0, [_]
    Literal[CODE, pos:2, if_(a_<_b)_{_}]
    Text[TEXT, pos:24, _]]
  body: empty
  block tags: empty
]
*/

    /**
     * {@code {@code nested} }
     */
    void nested() { }
/*
DocComment[DOC_COMMENT, pos:1
  firstSentence: 1
    Literal[CODE, pos:1, {@code_nested}_]
  body: empty
  block tags: empty
]
*/

    /**
     * {@code if (a < b) {
     *        }
     * }
     */
    void embedded_newline() { }
/*
DocComment[DOC_COMMENT, pos:1
  firstSentence: 1
    Literal[CODE, pos:1, if_(a_<_b)_{|________}|_]
  body: empty
  block tags: empty
]
*/

    /** {@code if (a < b) { } */
    void unterminated_1() { }
/*
DocComment[DOC_COMMENT, pos:0
  firstSentence: 1
    Erroneous[ERRONEOUS, pos:0
      code: compiler.err.dc.unterminated.inline.tag
      body: {@code_if_(a_<_b)_{_}
    ]
  body: empty
  block tags: empty
]
*/

    /**
     * {@code if (a < b) { }
     * @author jjg */
    void unterminated_2() { }
/*
DocComment[DOC_COMMENT, pos:1
  firstSentence: 1
    Erroneous[ERRONEOUS, pos:1
      code: compiler.err.dc.unterminated.inline.tag
      body: {@code_if_(a_<_b)_{_}
    ]
  body: empty
  block tags: 1
    Author[AUTHOR, pos:24
      name: 1
        Text[TEXT, pos:32, jjg]
    ]
]
*/

}

