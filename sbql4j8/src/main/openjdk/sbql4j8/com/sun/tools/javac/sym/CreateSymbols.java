/*
 * Copyright (c) 2006, 2013, Oracle and/or its affiliates. All rights reserved.
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

package sbql4j8.com.sun.tools.javac.sym;

import sbql4j8.com.sun.tools.javac.api.JavacTaskImpl;
import sbql4j8.com.sun.tools.javac.code.Kinds;
import sbql4j8.com.sun.tools.javac.code.Scope;
import sbql4j8.com.sun.tools.javac.code.Symbol.*;
import sbql4j8.com.sun.tools.javac.code.Symbol;
import sbql4j8.com.sun.tools.javac.code.Attribute;
import sbql4j8.com.sun.tools.javac.code.Symtab;
import sbql4j8.com.sun.tools.javac.code.Type;
import sbql4j8.com.sun.tools.javac.code.Types;
import sbql4j8.com.sun.tools.javac.jvm.ClassWriter;
import sbql4j8.com.sun.tools.javac.jvm.Pool;
import sbql4j8.com.sun.tools.javac.processing.JavacProcessingEnvironment;
import sbql4j8.com.sun.tools.javac.util.List;
import sbql4j8.com.sun.tools.javac.util.Names;
import sbql4j8.com.sun.tools.javac.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import static javax.tools.JavaFileObject.Kind.CLASS;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 * Used to generate a "symbol file" representing rt.jar that only
 * includes supported or legacy proprietary API.  Valid annotation
 * processor options:
 *
 * <dl>
 * <dt>sbql4j8.com.sun.tools.javac.sym.Jar</dt>
 * <dd>Specifies the location of rt.jar.</dd>
 * <dt>sbql4j8.com.sun.tools.javac.sym.Dest</dt>
 * <dd>Specifies the destination directory.</dd>
 * </dl>
 *
 * <p><b>This is NOT part of any supported API.
 * If you write code that depends on this, you do so at your own
 * risk.  This code and its internal interfaces are subject to change
 * or deletion without notice.</b></p>
 *
 * @author Peter von der Ah\u00e9
 */
@SupportedOptions({
    "sbql4j8.com.sun.tools.javac.sym.Jar",
    "sbql4j8.com.sun.tools.javac.sym.Dest",
    "sbql4j8.com.sun.tools.javac.sym.Profiles"})
@SupportedAnnotationTypes("*")
public class CreateSymbols extends AbstractProcessor {

    static Set<String> getLegacyPackages() {
        ResourceBundle legacyBundle
            = ResourceBundle.getBundle("sbql4j8.com.sun.tools.javac.resources.legacy");
        Set<String> keys = new HashSet<String>();
        for (Enumeration<String> e = legacyBundle.getKeys(); e.hasMoreElements(); )
            keys.add(e.nextElement());
        return keys;
    }

    public boolean process(Set<? extends TypeElement> tes, RoundEnvironment renv) {
        try {
            if (renv.processingOver())
                createSymbols();
        } catch (IOException e) {
            CharSequence msg = e.getLocalizedMessage();
            if (msg == null)
                msg = e.toString();
            processingEnv.getMessager()
                .printMessage(Diagnostic.Kind.ERROR, msg);
        } catch (Throwable t) {
            t.printStackTrace();
            Throwable cause = t.getCause();
            if (cause == null)
                cause = t;
            CharSequence msg = cause.getLocalizedMessage();
            if (msg == null)
                msg = cause.toString();
            processingEnv.getMessager()
                .printMessage(Diagnostic.Kind.ERROR, msg);
        }
        return true;
    }

    void createSymbols() throws IOException {
        Set<String> legacy = getLegacyPackages();
        Set<String> legacyProprietary = getLegacyPackages();
        Set<String> documented = new HashSet<String>();
        Set<PackageSymbol> packages =
            ((JavacProcessingEnvironment)processingEnv).getSpecifiedPackages();
        Map<String,String> pOptions = processingEnv.getOptions();
        String jarName = pOptions.get("sbql4j8.com.sun.tools.javac.sym.Jar");
        if (jarName == null)
            throw new RuntimeException("Must use -Acom.sun.tools.javac.sym.Jar=LOCATION_OF_JAR");
        String destName = pOptions.get("sbql4j8.com.sun.tools.javac.sym.Dest");
        if (destName == null)
            throw new RuntimeException("Must use -Acom.sun.tools.javac.sym.Dest=LOCATION_OF_JAR");
        String profileSpec=pOptions.get("sbql4j8.com.sun.tools.javac.sym.Profiles");
        if (profileSpec == null)
            throw new RuntimeException("Must use -Acom.sun.tools.javac.sym.Profiles=PROFILES_SPEC");
        Profiles profiles = Profiles.read(new File(profileSpec));

        for (PackageSymbol psym : packages) {
            String name = psym.getQualifiedName().toString();
            legacyProprietary.remove(name);
            documented.add(name);
        }

        JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fm = tool.getStandardFileManager(null, null, null);
        Location jarLocation = StandardLocation.locationFor(jarName);
        File jarFile = new File(jarName);
        fm.setLocation(jarLocation, List.of(jarFile));
        fm.setLocation(StandardLocation.CLASS_PATH, List.<File>nil());
        fm.setLocation(StandardLocation.SOURCE_PATH, List.<File>nil());
        {
            ArrayList<File> bootClassPath = new ArrayList<File>();
            bootClassPath.add(jarFile);
            for (File path : fm.getLocation(StandardLocation.PLATFORM_CLASS_PATH)) {
                if (!new File(path.getName()).equals(new File("rt.jar")))
                    bootClassPath.add(path);
            }
            System.err.println("Using boot class path = " + bootClassPath);
            fm.setLocation(StandardLocation.PLATFORM_CLASS_PATH, bootClassPath);
        }
        // System.out.println(fm.getLocation(StandardLocation.PLATFORM_CLASS_PATH));
        File destDir = new File(destName);
        if (!destDir.exists())
            if (!destDir.mkdirs())
                throw new RuntimeException("Could not create " + destDir);
        fm.setLocation(StandardLocation.CLASS_OUTPUT, List.of(destDir));
        Set<String> hiddenPackages = new HashSet<String>();
        Set<String> crisp = new HashSet<String>();
        List<String> options = List.of("-XDdev");
        // options = options.prepend("-doe");
        // options = options.prepend("-verbose");
        JavacTaskImpl task = (JavacTaskImpl)
            tool.getTask(null, fm, null, options, null, null);
        sbql4j8.com.sun.tools.javac.main.JavaCompiler compiler =
            sbql4j8.com.sun.tools.javac.main.JavaCompiler.instance(task.getContext());
        ClassWriter writer = ClassWriter.instance(task.getContext());
        Symtab syms = Symtab.instance(task.getContext());
        Names names = Names.instance(task.getContext());
        Attribute.Compound proprietaryAnno =
            new Attribute.Compound(syms.proprietaryType,
                                   List.<Pair<Symbol.MethodSymbol,Attribute>>nil());
        Attribute.Compound[] profileAnnos = new Attribute.Compound[profiles.getProfileCount() + 1];
        Symbol.MethodSymbol profileValue = (MethodSymbol) syms.profileType.tsym.members().lookup(names.value).sym;
        for (int i = 1; i < profileAnnos.length; i++) {
            profileAnnos[i] = new Attribute.Compound(syms.profileType,
                    List.<Pair<Symbol.MethodSymbol, Attribute>>of(
                    new Pair<Symbol.MethodSymbol, Attribute>(profileValue, new Attribute.Constant(syms.intType, i))));
        }

        Type.moreInfo = true;
        Types types = Types.instance(task.getContext());
        Pool pool = new Pool(types);
        for (JavaFileObject file : fm.list(jarLocation, "", EnumSet.of(CLASS), true)) {
            String className = fm.inferBinaryName(jarLocation, file);
            int index = className.lastIndexOf('.');
            String pckName = index == -1 ? "" : className.substring(0, index);
            boolean addLegacyAnnotation = false;
            if (documented.contains(pckName)) {
                if (!legacy.contains(pckName))
                    crisp.add(pckName);
                // System.out.println("Documented: " + className);
            } else if (legacyProprietary.contains(pckName)) {
                addLegacyAnnotation = true;
                // System.out.println("Legacy proprietary: " + className);
            } else {
                // System.out.println("Hidden " + className);
                hiddenPackages.add(pckName);
                continue;
            }
            TypeSymbol sym = (TypeSymbol)compiler.resolveIdent(className);
            if (sym.kind != Kinds.TYP) {
                if (className.indexOf('$') < 0) {
                    System.err.println("Ignoring (other) " + className + " : " + sym);
                    System.err.println("   " + sym.getClass().getSimpleName() + " " + sym.type);
                }
                continue;
            }
            sym.complete();
            if (sym.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
                System.err.println("Ignoring (bad) " + sym.getQualifiedName());
                continue;
            }
            ClassSymbol cs = (ClassSymbol) sym;
            if (addLegacyAnnotation) {
                cs.prependAttributes(List.of(proprietaryAnno));
            }
            int p = profiles.getProfile(cs.fullname.toString().replace(".", "/"));
            if (0 < p && p < profileAnnos.length)
                cs.prependAttributes(List.of(profileAnnos[p]));
            writeClass(pool, cs, writer);
        }

        if (false) {
            for (String pckName : crisp)
                System.out.println("Crisp: " + pckName);
            for (String pckName : hiddenPackages)
                System.out.println("Hidden: " + pckName);
            for (String pckName : legacyProprietary)
                System.out.println("Legacy proprietary: " + pckName);
            for (String pckName : documented)
                System.out.println("Documented: " + pckName);
        }
    }

    void writeClass(final Pool pool, final ClassSymbol cs, final ClassWriter writer)
        throws IOException
    {
        try {
            pool.reset();
            cs.pool = pool;
            writer.writeClass(cs);
            for (Scope.Entry e = cs.members().elems; e != null; e = e.sibling) {
                if (e.sym.kind == Kinds.TYP) {
                    ClassSymbol nestedClass = (ClassSymbol)e.sym;
                    nestedClass.complete();
                    writeClass(pool, nestedClass, writer);
                }
            }
        } catch (ClassWriter.StringOverflow ex) {
            throw new RuntimeException(ex);
        } catch (ClassWriter.PoolOverflow ex) {
            throw new RuntimeException(ex);
        }
    }

    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    // used for debugging
    public static void main(String... args) {
        String rt_jar = args[0];
        String dest = args[1];
        args = new String[] {
            "-Xbootclasspath:" + rt_jar,
            "-XDprocess.packages",
            "-proc:only",
            "-processor",
            "sbql4j8.com.sun.tools.javac.sym.CreateSymbols",
            "-Acom.sun.tools.javac.sym.Jar=" + rt_jar,
            "-Acom.sun.tools.javac.sym.Dest=" + dest,
            // <editor-fold defaultstate="collapsed">
            "java.applet",
            "java.awt",
            "java.awt.color",
            "java.awt.datatransfer",
            "java.awt.dnd",
            "java.awt.event",
            "java.awt.font",
            "java.awt.geom",
            "java.awt.im",
            "java.awt.im.spi",
            "java.awt.image",
            "java.awt.image.renderable",
            "java.awt.print",
            "java.beans",
            "java.beans.beancontext",
            "java.io",
            "java.lang",
            "java.lang.annotation",
            "java.lang.instrument",
            "java.lang.management",
            "java.lang.ref",
            "java.lang.reflect",
            "java.math",
            "java.net",
            "java.nio",
            "java.nio.channels",
            "java.nio.channels.spi",
            "java.nio.charset",
            "java.nio.charset.spi",
            "java.rmi",
            "java.rmi.activation",
            "java.rmi.dgc",
            "java.rmi.registry",
            "java.rmi.server",
            "java.security",
            "java.security.acl",
            "java.security.cert",
            "java.security.interfaces",
            "java.security.spec",
            "java.sql",
            "java.text",
            "java.text.spi",
            "java.util",
            "java.util.concurrent",
            "java.util.concurrent.atomic",
            "java.util.concurrent.locks",
            "java.util.jar",
            "java.util.logging",
            "java.util.prefs",
            "java.util.regex",
            "java.util.spi",
            "java.util.zip",
            "sbql4j8.javax.accessibility",
            "sbql4j8.javax.activation",
            "sbql4j8.javax.activity",
            "sbql4j8.javax.annotation",
            "sbql4j8.javax.annotation.processing",
            "sbql4j8.javax.crypto",
            "sbql4j8.javax.crypto.interfaces",
            "sbql4j8.javax.crypto.spec",
            "sbql4j8.javax.imageio",
            "sbql4j8.javax.imageio.event",
            "sbql4j8.javax.imageio.metadata",
            "sbql4j8.javax.imageio.plugins.jpeg",
            "sbql4j8.javax.imageio.plugins.bmp",
            "sbql4j8.javax.imageio.spi",
            "sbql4j8.javax.imageio.stream",
            "sbql4j8.javax.jws",
            "sbql4j8.javax.jws.soap",
            "sbql4j8.javax.lang.model",
            "sbql4j8.javax.lang.model.element",
            "sbql4j8.javax.lang.model.type",
            "sbql4j8.javax.lang.model.util",
            "sbql4j8.javax.management",
            "sbql4j8.javax.management.loading",
            "sbql4j8.javax.management.monitor",
            "sbql4j8.javax.management.relation",
            "sbql4j8.javax.management.openmbean",
            "sbql4j8.javax.management.timer",
            "sbql4j8.javax.management.modelmbean",
            "sbql4j8.javax.management.remote",
            "sbql4j8.javax.management.remote.rmi",
            "sbql4j8.javax.naming",
            "sbql4j8.javax.naming.directory",
            "sbql4j8.javax.naming.event",
            "sbql4j8.javax.naming.ldap",
            "sbql4j8.javax.naming.spi",
            "sbql4j8.javax.net",
            "sbql4j8.javax.net.ssl",
            "sbql4j8.javax.print",
            "sbql4j8.javax.print.attribute",
            "sbql4j8.javax.print.attribute.standard",
            "sbql4j8.javax.print.event",
            "sbql4j8.javax.rmi",
            "sbql4j8.javax.rmi.CORBA",
            "sbql4j8.javax.rmi.ssl",
            "sbql4j8.javax.script",
            "sbql4j8.javax.security.auth",
            "sbql4j8.javax.security.auth.callback",
            "sbql4j8.javax.security.auth.kerberos",
            "sbql4j8.javax.security.auth.login",
            "sbql4j8.javax.security.auth.spi",
            "sbql4j8.javax.security.auth.x500",
            "sbql4j8.javax.security.cert",
            "sbql4j8.javax.security.sasl",
            "sbql4j8.javax.sound.sampled",
            "sbql4j8.javax.sound.sampled.spi",
            "sbql4j8.javax.sound.midi",
            "sbql4j8.javax.sound.midi.spi",
            "sbql4j8.javax.sql",
            "sbql4j8.javax.sql.rowset",
            "sbql4j8.javax.sql.rowset.serial",
            "sbql4j8.javax.sql.rowset.spi",
            "sbql4j8.javax.swing",
            "sbql4j8.javax.swing.border",
            "sbql4j8.javax.swing.colorchooser",
            "sbql4j8.javax.swing.filechooser",
            "sbql4j8.javax.swing.event",
            "sbql4j8.javax.swing.table",
            "sbql4j8.javax.swing.text",
            "sbql4j8.javax.swing.text.html",
            "sbql4j8.javax.swing.text.html.parser",
            "sbql4j8.javax.swing.text.rtf",
            "sbql4j8.javax.swing.tree",
            "sbql4j8.javax.swing.undo",
            "sbql4j8.javax.swing.plaf",
            "sbql4j8.javax.swing.plaf.basic",
            "sbql4j8.javax.swing.plaf.metal",
            "sbql4j8.javax.swing.plaf.multi",
            "sbql4j8.javax.swing.plaf.synth",
            "sbql4j8.javax.tools",
            "sbql4j8.javax.transaction",
            "sbql4j8.javax.transaction.xa",
            "sbql4j8.javax.xml.parsers",
            "sbql4j8.javax.xml.bind",
            "sbql4j8.javax.xml.bind.annotation",
            "sbql4j8.javax.xml.bind.annotation.adapters",
            "sbql4j8.javax.xml.bind.attachment",
            "sbql4j8.javax.xml.bind.helpers",
            "sbql4j8.javax.xml.bind.util",
            "sbql4j8.javax.xml.soap",
            "sbql4j8.javax.xml.ws",
            "sbql4j8.javax.xml.ws.handler",
            "sbql4j8.javax.xml.ws.handler.soap",
            "sbql4j8.javax.xml.ws.http",
            "sbql4j8.javax.xml.ws.soap",
            "sbql4j8.javax.xml.ws.spi",
            "sbql4j8.javax.xml.transform",
            "sbql4j8.javax.xml.transform.sax",
            "sbql4j8.javax.xml.transform.dom",
            "sbql4j8.javax.xml.transform.stax",
            "sbql4j8.javax.xml.transform.stream",
            "sbql4j8.javax.xml",
            "sbql4j8.javax.xml.crypto",
            "sbql4j8.javax.xml.crypto.dom",
            "sbql4j8.javax.xml.crypto.dsig",
            "sbql4j8.javax.xml.crypto.dsig.dom",
            "sbql4j8.javax.xml.crypto.dsig.keyinfo",
            "sbql4j8.javax.xml.crypto.dsig.spec",
            "sbql4j8.javax.xml.datatype",
            "sbql4j8.javax.xml.validation",
            "sbql4j8.javax.xml.namespace",
            "sbql4j8.javax.xml.xpath",
            "sbql4j8.javax.xml.stream",
            "sbql4j8.javax.xml.stream.events",
            "sbql4j8.javax.xml.stream.util",
            "org.ietf.jgss",
            "org.omg.CORBA",
            "org.omg.CORBA.DynAnyPackage",
            "org.omg.CORBA.ORBPackage",
            "org.omg.CORBA.TypeCodePackage",
            "org.omg.stub.java.rmi",
            "org.omg.CORBA.portable",
            "org.omg.CORBA_2_3",
            "org.omg.CORBA_2_3.portable",
            "org.omg.CosNaming",
            "org.omg.CosNaming.NamingContextExtPackage",
            "org.omg.CosNaming.NamingContextPackage",
            "org.omg.SendingContext",
            "org.omg.PortableServer",
            "org.omg.PortableServer.CurrentPackage",
            "org.omg.PortableServer.POAPackage",
            "org.omg.PortableServer.POAManagerPackage",
            "org.omg.PortableServer.ServantLocatorPackage",
            "org.omg.PortableServer.portable",
            "org.omg.PortableInterceptor",
            "org.omg.PortableInterceptor.ORBInitInfoPackage",
            "org.omg.Messaging",
            "org.omg.IOP",
            "org.omg.IOP.CodecFactoryPackage",
            "org.omg.IOP.CodecPackage",
            "org.omg.Dynamic",
            "org.omg.DynamicAny",
            "org.omg.DynamicAny.DynAnyPackage",
            "org.omg.DynamicAny.DynAnyFactoryPackage",
            "org.w3c.dom",
            "org.w3c.dom.events",
            "org.w3c.dom.bootstrap",
            "org.w3c.dom.ls",
            "org.xml.sax",
            "org.xml.sax.ext",
            "org.xml.sax.helpers",
            "sbql4j8.com.sun.java.browser.dom",
            "org.w3c.dom",
            "org.w3c.dom.bootstrap",
            "org.w3c.dom.ls",
            "org.w3c.dom.ranges",
            "org.w3c.dom.traversal",
            "org.w3c.dom.html",
            "org.w3c.dom.stylesheets",
            "org.w3c.dom.css",
            "org.w3c.dom.events",
            "org.w3c.dom.views",
            "sbql4j8.com.sun.management",
            "sbql4j8.com.sun.security.auth",
            "sbql4j8.com.sun.security.auth.callback",
            "sbql4j8.com.sun.security.auth.login",
            "sbql4j8.com.sun.security.auth.module",
            "sbql4j8.com.sun.security.jgss",
            "sbql4j8.com.sun.net.httpserver",
            "sbql4j8.com.sun.net.httpserver.spi",
            "sbql4j8.javax.smartcardio"
            // </editor-fold>
        };
        sbql4j8.com.sun.tools.javac.Main.compile(args);
    }

}
