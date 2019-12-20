package tech.kiwa.engine.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MemoryClassLoader extends ClassLoader {
    // class name to class bytes:
    private Map<String, byte[]> classBytes = new HashMap<String, byte[]>();

    public MemoryClassLoader(Map<String, byte[]> classBytes) {
        super(MemoryClassLoader.class.getClassLoader());
        this.classBytes.putAll(classBytes);
    }

    public MemoryClassLoader() {
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] buf = classBytes.get(name);
        if (buf == null) {
            return super.findClass(name);
        }
        return defineClass(name, buf, 0, buf.length);
    }

    public Class<?> queryLoadedClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    public void appendClass(Map<String, byte[]> classSet) {
        Set<String> keySet = classSet.keySet();
        for (String key : keySet) {
            if (!classBytes.containsKey(key)) {
                classBytes.put(key, classSet.get(key));
            }
        }
    }
}
