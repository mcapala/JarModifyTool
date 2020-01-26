package pl.wat.mc.jfk.lab2;

import javassist.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ClassEditor {
    private final JarContentManager jcm;

    public ClassEditor(JarContentManager jfm) {
        this.jcm = jfm;
    }


    public void setSuperclass(String className, String superclassName) {
        try {
            CtClass clazz = getCtClass(className);
            CtClass superclass = getCtClass(superclassName);
            clazz.defrost();
            clazz.setSuperclass(superclass);
        }
        catch (CannotCompileException e){
            System.out.println("Compile error.");
            e.printStackTrace();
        }
    }

    public void addClass(String classPath) {
        CtClass newClass = jcm.getJarClassPool().makeClass(classPath);
        jcm.classNames.add(classPath);
        jcm.saveCtClass(newClass);
    }

    public void removeClass(String classPath) {
        jcm.deleteFile(classPath.replaceAll("\\.","/")+".class");
    }

    public void addInterface(String interfacePath) {
        CtClass newInterface = jcm.getJarClassPool().makeInterface(interfacePath);
        jcm.saveCtClass(newInterface);
    }

    public void removeInterface(String interfacePath) {
        CtClass newInterface = jcm.getJarClassPool().makeInterface(interfacePath);
        jcm.saveCtClass(newInterface);
    }

    public void addPackage(String packagePath){
        jcm.createFolder(packagePath.replaceAll("\\.","/"));
    }

    public void removePackage(String packagePath){
        jcm.deleteFile(packagePath.replaceAll("\\.","/"));
    }

    public void addMethod(String classPath, String src) throws CannotCompileException {
        CtClass declaring = getCtClass(classPath);
        CtMethod newMethod = CtNewMethod.make(src,declaring);
        declaring.addMethod(newMethod);
        jcm.saveCtClass(declaring);
    }

    public void removeMethod(String classPath, String methodName,CtClass [] params) throws NotFoundException {
        CtClass cc = getCtClass(classPath);
        CtMethod ctm = getCtMethod(cc, methodName,params);
        cc.removeMethod(ctm);
        jcm.saveCtClass(cc);
    }

    public void addCodeBeforeMethod(String classPath, String methodName,CtClass [] params, String methodBody) throws CannotCompileException {
        CtClass cc = getCtClass(classPath);
        CtMethod ctm = getCtMethod(cc, methodName,params);
        ctm.insertBefore(methodBody);
        jcm.saveCtClass(cc);

    }

    public void addCodeAfterMethod(String classPath, String methodName,CtClass [] params, String methodBody) throws CannotCompileException {
        CtClass cc = getCtClass(classPath);
        CtMethod ctm;
        ctm = getCtMethod(cc, methodName,params);
        ctm.insertAfter(methodBody);
        jcm.saveCtClass(cc);
    }


    public void setMethodBody(String classPath, String methodName,CtClass [] params, String methodBody) throws CannotCompileException {
        CtClass cc = getCtClass(classPath);
        CtMethod ctm = getCtMethod(cc, methodName,params);
        ctm.setBody(methodBody);
        jcm.saveCtClass(cc);
    }

    public void addField(String classPath, String src) throws CannotCompileException {
        CtClass cc = getCtClass(classPath);
        CtField newField = CtField.make(src, cc);
        cc.addField(newField);
        jcm.saveCtClass(cc);
    }

    public void removeField(String classPath, String fieldName) throws NotFoundException {
        CtClass cc = getCtClass(classPath);
        CtField cf = cc.getDeclaredField(fieldName);
        cc.removeField(cf);
        jcm.saveCtClass(cc);
    }

    public void addConstructor(String classPath, String src) throws CannotCompileException {
        CtClass cc = getCtClass(classPath);
        CtConstructor con = CtNewConstructor.make(src, cc);
        cc.addConstructor(con);
        jcm.saveCtClass(cc);
    }

    public void removeConstructor(String classPath, CtClass[] arguments) throws NotFoundException {
        CtClass cc = getCtClass(classPath);
        CtConstructor con = cc.getDeclaredConstructor(arguments);
        cc.removeConstructor(con);
        jcm.saveCtClass(cc);
    }

    public void setConstructorBody(String classPath, CtClass[] arguments, String constructorBody) throws NotFoundException, CannotCompileException {
        CtClass cc = getCtClass(classPath);
        CtConstructor con = cc.getDeclaredConstructor(arguments);
        con.setBody(constructorBody);
        jcm.saveCtClass(cc);
    }

    public void listClassMethods(String classPath) {
        Class<?> clazz = jcm.loadClass(classPath);
        Method[] methods = clazz.getDeclaredMethods();
        System.out.println("Methods in class " + classPath);
       for (Method m : methods) {
           System.out.println(m.toString());
         }
    }

    public void listClassFields(String classPath) {
        Class<?> clazz = jcm.loadClass(classPath);
        if(clazz!=null) {
            Field[] fields = clazz.getDeclaredFields();
            System.out.println("Fields in class " + classPath);
            for (Field f : fields) {
                System.out.println(f.toString());
            }

        }
    }

    public void listClassConstructors(String classPath) {
        Class<?> clazz = jcm.loadClass(classPath);
        if(clazz!=null) {
            Constructor<?>[] constructors = clazz.getConstructors();
            System.out.println("Constructors in class " + classPath);
            for (Constructor<?> c : constructors) {
                System.out.println(c.toString());
            }
        }
    }


    public CtMethod getCtMethod(CtClass cc, String methodName,CtClass [] params) {
        CtMethod cm = null;
        try {
            if(params==null) {
                cm = cc.getDeclaredMethod(methodName);
            }
            else{
                cm = cc.getDeclaredMethod(methodName,params);
            }

        } catch (NotFoundException e) {
            System.out.println("Error in getting method from class" + cc.getName());
            e.printStackTrace();
        }
        return cm;
    }

    public CtClass getCtClass(String classPath) {
        CtClass temp = null;
        try {
            temp = jcm.getJarClassPool().get(classPath);
            temp.defrost();
        } catch (Exception e) {
            System.out.println("Error in getting class from class Pool");
        }
        if(temp==null){
            System.out.println("Class "+classPath+" does not exist.");
        }
        return temp;
    }

}
