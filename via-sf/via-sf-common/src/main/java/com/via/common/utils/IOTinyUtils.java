package com.via.common.utils;

import javassist.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

public class IOTinyUtils {

    static public String toString(Reader reader) throws IOException {
        CharArrayWriter sw = new CharArrayWriter();
        copy(reader, sw);
        return sw.toString();
    }

    static public long copy(Reader input, Writer output) throws IOException {
        char[] buffer = new char[1 << 12];
        long count = 0;
        for (int n = 0; (n = input.read(buffer)) >= 0; ) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    static public List<String> readLines(Reader input) throws IOException {
        BufferedReader reader = toBufferedReader(input);
        List<String> list = new ArrayList<String>();
        String line;
        for (; ; ) {
            line = reader.readLine();
            if (null != line) {
                list.add(line);
            } else {
                break;
            }
        }
        return list;
    }

    static private BufferedReader toBufferedReader(Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }

    static public long copyFile(String source, String target) throws Exception {
        File sf = new File(source);
        if (!sf.exists()) {
            throw new IllegalArgumentException("source file does not exist.");
        }
        File tf = new File(target);
        tf.getParentFile().mkdirs();
        if (!tf.exists() && !tf.createNewFile()) {
            throw new RuntimeException("failed to create target file.");
        }

        FileChannel sc = null;
        FileChannel tc = null;
        try {
            tc = new FileOutputStream(tf).getChannel();
            sc = new FileInputStream(sf).getChannel();
            Method transferToDirectly = sc.getClass().getDeclaredMethod("transferToDirectly", new Class[] {long.class, int.class, WritableByteChannel.class});
            Method transferToTrustedChannel = sc.getClass().getDeclaredMethod("transferToTrustedChannel", new Class[] {long.class, long.class, WritableByteChannel.class});
            Method transferToArbitraryChannel = sc.getClass().getDeclaredMethod("transferToArbitraryChannel", new Class[] {long.class, int.class, WritableByteChannel.class});
            long var9;
            return (var9 = transferTo(transferToDirectly, sc, 0, (int) sf.length(), tc)) >= 0L ?
                var9 : ((var9 = transferTo(transferToTrustedChannel, sc, 0, sf.length(), tc)) >= 0L ? var9 : (transferTo(transferToArbitraryChannel, sc, 0, (int) sf.length(), tc)));
        } finally {
            if (null != sc) {
                sc.close();
            }
            if (null != tc) {
                tc.close();
            }
        }
    }

    private static long transferTo(Method m, Object ins, long var1, int var3,
        WritableByteChannel var5) throws InvocationTargetException, IllegalAccessException {
        m.setAccessible(true);
        return (long) m.invoke(ins, var1, var3, var5);
    }

    private static long transferTo(Method m, Object ins, long var1, long var3,
        WritableByteChannel var5) throws InvocationTargetException, IllegalAccessException {
        m.setAccessible(true);
        return (long) m.invoke(ins, var1, var3, var5);
    }

    public static void delete(File fileOrDir) throws IOException {
        if (fileOrDir == null) {
            return;
        }

        if (fileOrDir.isDirectory()) {
            cleanDirectory(fileOrDir);
        }

        fileOrDir.delete();
    }

    public static void cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        File[] files = directory.listFiles();
        if (files == null) { // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }

        IOException exception = null;
        for (File file : files) {
            try {
                delete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    public static void writeStringToFile(File file, String data, String encoding) throws IOException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(data.getBytes(encoding));
        } finally {
            if (null != os) {
                os.close();
            }
        }
    }

    private static String modifier(int mod) {
        if (Modifier.isPublic(mod))
            return "public";
        if (Modifier.isProtected(mod))
            return "protected";
        if (Modifier.isPrivate(mod))
            return "private";
        return "";
    }

    public static String addMethod(String name, int mod, Class<?> rt, Class<?>[] pts, Class<?>[] ets, String body) {
        StringBuilder sb = new StringBuilder();
        sb.append(modifier(mod)).append(' ').append(ReflectUtils.getName(rt)).append(' ').append(name);
        sb.append('(');
        for (int i = 0; i < pts.length; i++) {
            if (i > 0)
                sb.append(',');
            sb.append(ReflectUtils.getName(pts[i]));
            sb.append(" arg").append(i);
        }
        sb.append(')');
        if (ets != null && ets.length > 0) {
            sb.append(" throws ");
            for (int i = 0; i < ets.length; i++) {
                if (i > 0)
                    sb.append(',');
                sb.append(ReflectUtils.getName(ets[i]));
            }
        }
        sb.append('{').append(body).append('}');
        return sb.toString();
    }

    static class MyFileInputStream extends FileInputStream {

        private FileChannel fileChannel;
        private Method method;
        private String name;

        public MyFileInputStream(String name, Method method) throws FileNotFoundException {
            super(name);
            this.method = method;
            this.name = name;
        }

        @Override
        public FileChannel getChannel() {
            try {
                return (FileChannel) method.invoke(null, getFD(), name, true, false, this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        long size = 2147483647L * 2;
        ClassPool classPool = new ClassPool(true);
        classPool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
        CtClass ctClass = classPool.get("sun.nio.ch.FileChannelImpl");
        CtMethod ctMethod = ctClass.getDeclaredMethod("transferTo");
        ctClass.removeMethod(ctMethod);
        String code = "{\n" +
            "long var1 = arg0;\n" +
            "        long var3 = arg1;\n" +
            "        java.nio.channels.WritableByteChannel var5=arg2;\n" +
            "        this.ensureOpen();\n" +
            "        if(!var5.isOpen()) {\n" +
            "            throw new java.nio.channels.ClosedChannelException();\n" +
            "        } else if(!this.readable) {\n" +
            "            throw new java.nio.channels.NonReadableChannelException();\n" +
            "        } else if(var5 instanceof sun.nio.ch.FileChannelImpl && !((sun.nio.ch.FileChannelImpl)var5).writable) {\n" +
            "            throw new java.nio.channels.NonWritableChannelException();\n" +
            "        } else if(var1 >= 0L && var3 >= 0L) {\n" +
            "            long var6 = this.size();\n" +
            "            if(var1 > var6) {\n" +
            "                return 0L;\n" +
            "            } else {\n" +
            "                int var8 = (int)Math.min(var3, " + size + "L);\n" +
            "                if(var6 - var1 < (long)var8) {\n" +
            "                    var8 = (int)(var6 - var1);\n" +
            "                }\n" +
            "\n" +
            "                long var9;\n" +
            "                return (var9 = this.transferToDirectly(var1, var8, var5)) >= 0L?var9:((var9 = this.transferToTrustedChannel(var1, (long)var8, var5)) >= 0L?var9:this.transferToArbitraryChannel(var1, var8, var5));\n" +
            "            }\n" +
            "        } else {\n" +
            "            throw new java.lang.IllegalArgumentException();\n" +
            "        }}";
        code = addMethod("transferTo", Modifier.PUBLIC, long.class, new Class<?>[] {long.class, long.class, WritableByteChannel.class}, new Class<?>[] {IOException.class}, code);
        System.out.println(code);
        ctClass.addMethod(CtNewMethod.make(code, ctClass));
        Class cls = ctClass.toClass();

//        Method m = cls.getDeclaredMethod("open", new Class[] {FileDescriptor.class, String.class, boolean.class, boolean.class, Object.class});
//        long start = System.currentTimeMillis();
//        copyFile("D:\\开发帮助\\工具.zip", "D:\\WorkDir\\test.zip");
//        System.out.println("总耗时:" + (start - System.currentTimeMillis()));
    }
}
