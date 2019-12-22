package tech.kiwa.engine.utility;

import tech.kiwa.engine.utility.MemoryJavaFileManager.MemoryInputJavaFileObject;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JavaStringCompiler {
    private JavaCompiler compiler;
    private StandardJavaFileManager stdManager;
    private MemoryJavaFileManager fileManager = null;
    private MemoryClassLoader classLoader = null;
    private List<MemoryInputJavaFileObject> javaList = new ArrayList<>();

    public JavaStringCompiler() {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        this.stdManager = compiler.getStandardFileManager(null, null, null);
    }

    /**
     * Compile a Java source file in memory.
     *
     * @param fileName Java file name, e.g. "Test.java"
     * @param source   The source code as String.
     * @return The compiled results as Map that contains class name as key,
     * class binary as value.
     * @throws IOException If compile error.
     */
    public Map<String, byte[]> compile(String fileName, String source) throws IOException {
        if (fileManager == null) {
            fileManager = new MemoryJavaFileManager(stdManager);
        }
        MemoryInputJavaFileObject javaFileObject = (MemoryInputJavaFileObject) fileManager.makeStringSource(fileName, source);
        boolean bFound = false;
        for (MemoryInputJavaFileObject java : javaList) {
            if (fileName.equals(java.getClassName())) {
                bFound = true;
                break;
            }
        }
        if (!bFound) {
            javaList.add(javaFileObject);
        }
        CompilationTask task = compiler.getTask(null, fileManager, null, null, null, javaList);
        Boolean result = task.call();
        if (result == null || !result.booleanValue()) {
            throw new RuntimeException("Compilation failed.");
        }
        return fileManager.getClassBytes();
    }

    /**
     * Load class from compiled classes.
     *
     * @param name       Full class name.
     * @param classBytes Compiled results as a Map.
     * @return The Class instance.
     * @throws ClassNotFoundException If class not found.
     * @throws IOException            If load error.
     */
    public Class<?> loadClass(String name, Map<String, byte[]> classBytes) throws ClassNotFoundException, IOException {
        if (classLoader == null) {
            classLoader = new MemoryClassLoader(classBytes);
        } else {
            classLoader.appendClass(classBytes);
        }
        return classLoader.loadClass(name);
    }

    public Class<?> queryLoadedClass(String name) throws ClassNotFoundException {
        if (classLoader == null) {
            return classLoader.queryLoadedClass(name);
        }
        return null;
    }
}