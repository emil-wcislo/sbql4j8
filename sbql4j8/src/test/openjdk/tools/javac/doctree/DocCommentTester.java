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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.Name;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import sbql4j8.com.sun.source.doctree.*;
import sbql4j8.com.sun.source.tree.ClassTree;
import sbql4j8.com.sun.source.tree.CompilationUnitTree;
import sbql4j8.com.sun.source.tree.MethodTree;
import sbql4j8.com.sun.source.tree.Tree;
import sbql4j8.com.sun.source.tree.VariableTree;
import sbql4j8.com.sun.source.util.DocTreeScanner;
import sbql4j8.com.sun.source.util.DocTrees;
import sbql4j8.com.sun.source.util.JavacTask;
import sbql4j8.com.sun.source.util.TreePath;
import sbql4j8.com.sun.source.util.TreePathScanner;
import sbql4j8.com.sun.tools.javac.api.JavacTool;
import sbql4j8.com.sun.tools.javac.tree.DCTree;
import sbql4j8.com.sun.tools.javac.tree.DCTree.DCDocComment;
import sbql4j8.com.sun.tools.javac.tree.DCTree.DCErroneous;
import sbql4j8.com.sun.tools.javac.tree.DocPretty;

public class DocCommentTester {

    public static void main(String... args) throws Exception {
        new DocCommentTester().run(args);
    }

    public void run(String... args) throws Exception {
        String testSrc = System.getProperty("test.src");

        List<File> files = new ArrayList<File>();
        for (String arg: args)
            files.add(new File(testSrc, arg));

        JavacTool javac = JavacTool.create();
        StandardJavaFileManager fm = javac.getStandardFileManager(null, null, null);

        Iterable<? extends JavaFileObject> fos = fm.getJavaFileObjectsFromFiles(files);

        JavacTask t = javac.getTask(null, fm, null, null, null, fos);
        final DocTrees trees = DocTrees.instance(t);

        final Checker[] checkers = {
            new ASTChecker(this, trees),
            new PosChecker(this, trees),
            new PrettyChecker(this, trees)
        };

        DeclScanner d = new DeclScanner() {
            @Override
            public Void visitCompilationUnit(CompilationUnitTree tree, Void ignore) {
                for (Checker c: checkers)
                    c.visitCompilationUnit(tree);
                return super.visitCompilationUnit(tree, ignore);
            }

            @Override
            void visitDecl(Tree tree, Name name) {
                TreePath path = getCurrentPath();
                String dc = trees.getDocComment(path);
                if (dc != null) {
                    for (Checker c : checkers) {
                        try {
                            System.err.println(path.getLeaf().getKind()
                                    + " " + name
                                    + " " + c.getClass().getSimpleName());

                            c.check(path, name);

                            System.err.println();
                        } catch (Exception e) {
                            error("Exception " + e);
                            e.printStackTrace(System.err);
                        }
                    }
                }
            }
        };

        Iterable<? extends CompilationUnitTree> units = t.parse();
        for (CompilationUnitTree unit: units) {
            d.scan(unit, null);
        }

        if (errors > 0)
            throw new Exception(errors + " errors occurred");
    }

    static abstract class DeclScanner extends TreePathScanner<Void, Void> {
        abstract void visitDecl(Tree tree, Name name);

        @Override
        public Void visitClass(ClassTree tree, Void ignore) {
            super.visitClass(tree, ignore);
            visitDecl(tree, tree.getSimpleName());
            return null;
        }

        @Override
        public Void visitMethod(MethodTree tree, Void ignore) {
            super.visitMethod(tree, ignore);
            visitDecl(tree, tree.getName());
            return null;
        }

        @Override
        public Void visitVariable(VariableTree tree, Void ignore) {
            super.visitVariable(tree, ignore);
            visitDecl(tree, tree.getName());
            return null;
        }
    }

    /**
     * Base class for checkers to check the doc comment on a declaration
     * (when present.)
     */
    abstract class Checker {
        final DocTrees trees;

        Checker(DocTrees trees) {
            this.trees = trees;
        }

        void visitCompilationUnit(CompilationUnitTree tree) { }

        abstract void check(TreePath tree, Name name) throws Exception;

        void error(String msg) {
            DocCommentTester.this.error(msg);
        }
    }

    void error(String msg) {
        System.err.println("Error: " + msg);
        errors++;
    }

    int errors;

    /**
     * Verify the structure of the DocTree AST by comparing it against golden text.
     */
    static class ASTChecker extends Checker {
        static final String NEWLINE = System.getProperty("line.separator");
        Printer printer = new Printer();
        String source;

        ASTChecker(DocCommentTester test, DocTrees t) {
            test.super(t);
        }

        @Override
        void visitCompilationUnit(CompilationUnitTree tree) {
            try {
                source = tree.getSourceFile().getCharContent(true).toString();
            } catch (IOException e) {
                source = "";
            }
        }

        void check(TreePath path, Name name) {
            StringWriter out = new StringWriter();
            DocCommentTree dc = trees.getDocCommentTree(path);
            printer.print(dc, out);
            out.flush();
            String found = out.toString().replace(NEWLINE, "\n");

            // Look for the first block comment after the first occurrence of name
            int start = source.indexOf("\n/*\n", findName(source, name));
            int end = source.indexOf("\n*/\n", start);
            String expect = source.substring(start + 4, end + 1);
            if (!found.equals(expect)) {
                System.err.println("Expect:\n" + expect);
                System.err.println("Found:\n" + found);
                error("AST mismatch for " + name);
            }
        }

        /**
         * This main program is to set up the golden comments used by this
         * checker.
         * Usage:
         *     java DocCommentTester$ASTChecker -o dir file...
         * The given files are written to the output directory with their
         * golden comments updated. The intent is that the files should
         * then be compared with the originals, e.g. with meld, and if the
         * changes are approved, the new files can be used to replace the old.
         */
        public static void main(String... args) throws Exception {
            List<File> files = new ArrayList<File>();
            File o = null;
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.equals("-o"))
                    o = new File(args[++i]);
                else if (arg.startsWith("-"))
                    throw new IllegalArgumentException(arg);
                else {
                    files.add(new File(arg));
                }
            }

            if (o == null)
                throw new IllegalArgumentException("no output dir specified");
            final File outDir = o;

            JavacTool javac = JavacTool.create();
            StandardJavaFileManager fm = javac.getStandardFileManager(null, null, null);
            Iterable<? extends JavaFileObject> fos = fm.getJavaFileObjectsFromFiles(files);

            JavacTask t = javac.getTask(null, fm, null, null, null, fos);
            final DocTrees trees = DocTrees.instance(t);

            DeclScanner d = new DeclScanner() {
                Printer p = new Printer();
                String source;

                @Override
                public Void visitCompilationUnit(CompilationUnitTree tree, Void ignore) {
                    System.err.println("processing " + tree.getSourceFile().getName());
                    try {
                        source = tree.getSourceFile().getCharContent(true).toString();
                    } catch (IOException e) {
                        source = "";
                    }

                    // remove existing gold by removing all block comments after the first '{'.
                    int start = source.indexOf("{");
                    while ((start = source.indexOf("\n/*\n", start)) != -1) {
                        int end = source.indexOf("\n*/\n");
                        source = source.substring(0, start + 1) + source.substring(end + 4);
                    }

                    // process decls in compilation unit
                    super.visitCompilationUnit(tree, ignore);

                    // write the modified source
                    File f = new File(tree.getSourceFile().getName());
                    File outFile = new File(outDir, f.getName());
                    try {
                        FileWriter out = new FileWriter(outFile);
                        try {
                            out.write(source);
                        } finally {
                            out.close();
                        }
                    } catch (IOException e) {
                        System.err.println("Can't write " + tree.getSourceFile().getName()
                                + " to " + outFile + ": " + e);
                    }
                    return null;
                }

                @Override
                void visitDecl(Tree tree, Name name) {
                    DocTree dc = trees.getDocCommentTree(getCurrentPath());
                    if (dc != null) {
                        StringWriter out = new StringWriter();
                        p.print(dc, out);
                        String found = out.toString();

                        // Look for the empty line after the first occurrence of name
                        int pos = source.indexOf("\n\n", findName(source, name));

                        // Insert the golden comment
                        source = source.substring(0, pos)
                                + "\n/*\n"
                                + found
                                + "*/"
                                + source.substring(pos);
                    }
                }

            };

            Iterable<? extends CompilationUnitTree> units = t.parse();
            for (CompilationUnitTree unit: units) {
                d.scan(unit, null);
            }
        }

        static int findName(String source, Name name) {
            Pattern p = Pattern.compile("\\s" + name + "[(;]");
            Matcher m = p.matcher(source);
            if (!m.find())
                throw new Error("cannot find " + name);
            return m.start();
        }

        static class Printer implements DocTreeVisitor<Void, Void> {
            PrintWriter out;

            void print(DocTree tree, Writer out) {
                this.out = (out instanceof PrintWriter)
                        ? (PrintWriter) out : new PrintWriter(out);
                tree.accept(this, null);
                this.out.flush();
            }

            public Void visitAttribute(AttributeTree node, Void p) {
                header(node);
                indent(+1);
                print("name", node.getName().toString());
                print("vkind", node.getValueKind().toString());
                print("value", node.getValue());
                indent(-1);
                indent();
                out.println("]");
                return null;
            }

            public Void visitAuthor(AuthorTree node, Void p) {
                header(node);
                indent(+1);
                print("name", node.getName());
                indent(-1);
                indent();
                out.println("]");
                return null;
            }

            public Void visitComment(CommentTree node, Void p) {
                header(node, compress(node.getBody()));
                return null;
            }

            public Void visitDeprecated(DeprecatedTree node, Void p) {
                header(node);
                indent(+1);
                print("body", node.getBody());
                indent(-1);
                indent();
                out.println("]");
                return null;
            }

            public Void visitDocComment(DocCommentTree node, Void p) {
                header(node);
                indent(+1);
                print("firstSentence", node.getFirstSentence());
                print("body", node.getBody());
                print("block tags", node.getBlockTags());
                indent(-1);
                indent();
                out.println("]");
                return null;
            }

            public Void visitDocRoot(DocRootTree node, Void p) {
                header(node, "");
                return null;
            }

            public Void visitEndElement(EndElementTree node, Void p) {
                header(node, node.getName().toString());
                return null;
            }

            public Void visitEntity(EntityTree node, Void p) {
                header(node, node.getName().toString());
                return null;
            }

            public Void visitErroneous(ErroneousTree node, Void p) {
                header(node);
                indent(+1);
                print("code", ((DCErroneous) node).diag.getCode());
                print("body", compress(node.getBody()));
                indent(-1);
                indent();
                out.println("]");
                return null;
            }

            public Void visitIdentifier(IdentifierTree node, Void p) {
                header(node, compress(node.getName().toString()));
                return null;
            }

            public Void visitInheritDoc(InheritDocTree node, Void p) {
                header(node, "");
                return null;
            }

            public Void visitLink(LinkTree node, Void p) {
                header(node);
                indent(+1);
                print("reference", node.getReference());
                print("body", node.getLabel());
                indent(-1);
                indent();
                out.println("]");
                return null;
            }

            public Void visitLiteral(LiteralTree node, Void p) {
                header(node, compress(node.getBody().getBody()));
                return null;
            }

            public Void visitParam(ParamTree node, Void p) {
                header(node);
                indent(+1);
                print("name", node.getName());
                print("description", node.getDescription());
                indent(-1);
                indent();
                out.println("]");
                return null;
            }

            public Void visitReference(ReferenceTree node, Void p) {
                header(node, compress(node.getSignature()));
                return null;
            }

            public Void visitReturn(ReturnTree node, Void p) {
                header(node);
                indent(+1);
                print("description", node.getDescription());
                indent(-1);
                indent();
                out.println("]");
                return null;
            }

            public Void visitSee(SeeTree node, Void p) {
                header(node);
                indent(+1);
                print("reference", node.getReference());
                indent(-1);
                indent();
                out.println("]");
                return null;
            }

            public Void visitSerial(SerialTree node, Void p) {
                header(node);
                indent(+1);
                print("description", node.getDescription());
                indent(-1);
                indent();
                out.println("]");
                return null;
            }

            public Void visitSerialData(SerialDataTree node, Void p) {
                header(node);
                indent(+1);
                print("description", node.getDescription());
                indent(-1);
                indent();
                out.println("]");
                return null;
            }

            public Void visitSerialField(SerialFieldTree node, Void p) {
                header(node);
                indent(+1);
                print("name", node.getName());
                print("type", node.getType());
                print("description", node.getDescription());
                indent(-1);
                indent();
                out.println("]");
                return null;
            }

            public Void visitSince(SinceTree node, Void p) {
                header(node);
                indent(+1);
                print("body", node.getBody());
                indent(-1);
                indent();
                out.println("]");
                return null;
            }

            public Void visitStartElement(StartElementTree node, Void p) {
                header(node);
                indent(+1);
                indent();
                out.println("name:" + node.getName());
                print("attributes", node.getAttributes());
                indent(-1);
                indent();
                out.println("]");
                return null;
            }

            public Void visitText(TextTree node, Void p) {
                header(node, compress(node.getBody()));
                return null;
            }

            public Void visitThrows(ThrowsTree node, Void p) {
                header(node);
                indent(+1);
                print("exceptionName", node.getExceptionName());
                print("description", node.getDescription());
                indent(-1);
                indent();
                out.println("]");
                return null;
            }

            public Void visitUnknownBlockTag(UnknownBlockTagTree node, Void p) {
                header(node);
                indent(+1);
                indent();
                out.println("tag:" + node.getTagName());
                print("content", node.getContent());
                indent(-1);
                indent();
                out.println("]");
                return null;
            }

            public Void visitUnknownInlineTag(UnknownInlineTagTree node, Void p) {
                header(node);
                indent(+1);
                indent();
                out.println("tag:" + node.getTagName());
                print("content", node.getContent());
                indent(-1);
                indent();
                out.println("]");
                return null;
            }

            public Void visitValue(ValueTree node, Void p) {
                header(node);
                indent(+1);
                print("reference", node.getReference());
                indent(-1);
                indent();
                out.println("]");
                return null;
            }

            public Void visitVersion(VersionTree node, Void p) {
                header(node);
                indent(+1);
                print("body", node.getBody());
                indent(-1);
                indent();
                out.println("]");
                return null;
            }

            public Void visitOther(DocTree node, Void p) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            void header(DocTree node) {
                indent();
                out.println(simpleClassName(node) + "[" + node.getKind() + ", pos:" + ((DCTree) node).pos);
            }

            void header(DocTree node, String rest) {
                indent();
                out.println(simpleClassName(node) + "[" + node.getKind() + ", pos:" + ((DCTree) node).pos
                        + (rest.isEmpty() ? "" : ", " + rest)
                        + "]");
            }

            String simpleClassName(DocTree node) {
                return node.getClass().getSimpleName().replaceAll("DC(.*)", "$1");
            }

            void print(String name, DocTree item) {
                indent();
                if (item == null)
                    out.println(name + ": null");
                else {
                    out.println(name + ":");
                    indent(+1);
                    item.accept(this, null);
                    indent(-1);
                }
            }

            void print(String name, String s) {
                indent();
                out.println(name + ": " + s);
            }

            void print(String name, List<? extends DocTree> list) {
                indent();
                if (list == null)
                    out.println(name + ": null");
                else if (list.isEmpty())
                    out.println(name + ": empty");
                else {
                    out.println(name + ": " + list.size());
                    indent(+1);
                    for (DocTree tree: list) {
                        tree.accept(this, null);
                    }
                    indent(-1);
                }
            }

            int indent = 0;

            void indent() {
                for (int i = 0; i < indent; i++) {
                    out.print("  ");
                }
            }

            void indent(int n) {
                indent += n;
            }

            String compress(String s) {
                s = s.replace("\n", "|").replace(" ", "_");
                return (s.length() < 32)
                        ? s
                        : s.substring(0, 16) + "..." + s.substring(16);
            }

            String quote(String s) {
                if (s.contains("\""))
                    return "'" + s + "'";
                else if (s.contains("'") || s.contains(" "))
                    return '"' + s + '"';
                else
                    return s;
            }


        }
    }

    /**
     * Verify the reported tree positions by comparing the characters found
     * at and after the reported position with the beginning of the pretty-
     * printed text.
     */
    static class PosChecker extends Checker {
        PosChecker(DocCommentTester test, DocTrees t) {
            test.super(t);
        }

        @Override
        void check(TreePath path, Name name) throws Exception {
            JavaFileObject fo = path.getCompilationUnit().getSourceFile();
            final CharSequence cs = fo.getCharContent(true);

            final DCDocComment dc = (DCDocComment) trees.getDocCommentTree(path);
            DCTree t = (DCTree) trees.getDocCommentTree(path);

            DocTreeScanner scanner = new DocTreeScanner<Void,Void>() {
                @Override
                public Void scan(DocTree node, Void ignore) {
                    if (node != null) {
                        try {
                            String expect = getExpectText(node);
                            long pos = ((DCTree) node).getSourcePosition(dc);
                            String found = getFoundText(cs, (int) pos, expect.length());
                            if (!found.equals(expect)) {
                                System.err.println("expect: " + expect);
                                System.err.println("found:  " + found);
                                error("mismatch");
                            }

                        } catch (StringIndexOutOfBoundsException e) {
                            error(node.getClass() + ": " + e.toString());
                                e.printStackTrace();
                        }
                    }
                    return super.scan(node, ignore);
                }
            };

            scanner.scan(t, null);
        }

        String getExpectText(DocTree t) {
            StringWriter sw = new StringWriter();
            DocPretty p = new DocPretty(sw);
            try { p.print(t); } catch (IOException never) { }
            String s = sw.toString();
            if (s.length() <= 1)
                return s;
            int ws = s.replaceAll("\\s+", " ").indexOf(" ");
            if (ws != -1) s = s.substring(0, ws);
            return (s.length() < 5) ? s : s.substring(0, 5);
        }

        String getFoundText(CharSequence cs, int pos, int len) {
            return (pos == -1) ? "" : cs.subSequence(pos, Math.min(pos + len, cs.length())).toString();
        }
    }

    /**
     * Verify the pretty printed text against a normalized form of the
     * original doc comment.
     */
    static class PrettyChecker extends Checker {

        PrettyChecker(DocCommentTester test, DocTrees t) {
            test.super(t);
        }

        @Override
        void check(TreePath path, Name name) throws Exception {
            String raw = trees.getDocComment(path);
            String normRaw = normalize(raw);

            StringWriter out = new StringWriter();
            DocPretty dp = new DocPretty(out);
            dp.print(trees.getDocCommentTree(path));
            String pretty = out.toString();

            if (!pretty.equals(normRaw)) {
                error("mismatch");
                System.err.println("*** expected:");
                System.err.println(normRaw.replace(" ", "_"));
                System.err.println("*** found:");
                System.err.println(pretty.replace(" ", "_"));
    //            throw new Error();
            }
        }

        /**
         * Normalize white space in places where the tree does not preserve it.
         */
        String normalize(String s) {
            return s.trim()
                    .replaceFirst("\\.\\s++([^@])", ". $1")
                    .replaceFirst("\\.\\s*\\n *@", ".\n@")
                    .replaceFirst("\\s+<(/?p|pre|h[1-6])>", " <$1>")
                    .replaceAll("\\{@docRoot\\s+\\}", "{@docRoot}")
                    .replaceAll("\\{@inheritDoc\\s+\\}", "{@inheritDoc}")
                    .replaceAll("(\\{@value\\s+[^}]+)\\s+(\\})", "$1$2")
                    .replaceAll("\n[ \t]+@", "\n@");
        }

    }

}

