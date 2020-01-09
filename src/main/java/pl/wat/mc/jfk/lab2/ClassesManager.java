package pl.wat.mc.jfk.lab2;

import javassist.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class ClassesManager {
    private final JarContentManager jcm;


    public ClassesManager(JarContentManager jfm) {
        this.jcm = jfm;

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


    public void addMethod(String classPath, String accesibility, boolean isStatic, String returnType, String methodName, Map<String, String> arguments, String body) throws CannotCompileException {
        StringBuilder methodBodyBuilder = new StringBuilder();
        CtClass clazz = getCtClass(classPath);
        CtMethod newMethod;


        methodBodyBuilder.append(accesibility).append(" ");
        if (isStatic) methodBodyBuilder.append("static ");
        methodBodyBuilder.append(returnType).append(" ").append(methodName);
        methodBodyBuilder.append(getArgumentsAsString(arguments));
        methodBodyBuilder.append(body);
        newMethod =  CtNewMethod.make(methodBodyBuilder.toString(), clazz);
        clazz.addMethod(newMethod);
        jcm.saveCtClass(clazz);


    }

    public void removeMethod(String classPath, String methodName) throws NotFoundException {
        CtClass cc = getCtClass(classPath);
        CtMethod ctm = getCtMethod(cc, methodName);
        cc.removeMethod(ctm);
        jcm.saveCtClass(cc);
    }

    public void addCodeBeforeMethod(String classPath, String methodName, String methodBody) throws CannotCompileException {
        CtClass cc = getCtClass(classPath);
        CtMethod ctm = getCtMethod(cc, methodName);
        ctm.insertBefore(methodBody);
        jcm.saveCtClass(cc);

    }

    public void addCodeAfterMethod(String classPath, String methodName, String methodBody) throws CannotCompileException {
        CtClass cc = getCtClass(classPath);
        CtMethod ctm = getCtMethod(cc, methodName);
        ctm.insertAfter(methodBody);
        jcm.saveCtClass(cc);
    }

    public void setMethodBody(String classPath, String methodName, String methodBody) throws CannotCompileException {
        CtClass cc = getCtClass(classPath);
        CtMethod ctm = getCtMethod(cc, methodName);
        ctm.setBody(methodBody);
        jcm.saveCtClass(cc);
    }

    public void addField(String classPath, String accessibility, String type, String fieldName) throws CannotCompileException {
        CtClass cc = getCtClass(classPath);
        StringBuilder fieldBuilder = new StringBuilder();
        fieldBuilder.append(accessibility).append(" ").append(type).append(" ").append(fieldName).append(";");
        CtField newField = CtField.make(fieldBuilder.toString(), cc);
        cc.addField(newField);
        jcm.saveCtClass(cc);
    }

    public void removeField(String classPath, String fieldName) throws NotFoundException {
        CtClass cc = getCtClass(classPath);
        CtField cf = cc.getDeclaredField(fieldName);
        cc.removeField(cf);
        jcm.saveCtClass(cc);
    }

    public void addConstructor(String classPath, String accessibility, Map<String, String> arguments,String body) throws CannotCompileException {
       // System.out.println("dodawanie konstruktora");
        CtClass cc = getCtClass(classPath);
        StringBuilder constructorBuilder = new StringBuilder();
        constructorBuilder.append(accessibility).append(" ").append(cc.getSimpleName()).append(" ").append(getArgumentsAsString(arguments)).append(body);
        System.out.println(constructorBuilder.toString());
        CtConstructor con = CtNewConstructor.make(constructorBuilder.toString(), cc);
        cc.addConstructor(con);
        jcm.saveCtClass(cc);
    }

    public void removeConstructor(String classPath, Map<String, String> arguments) throws NotFoundException {
        CtClass cc = getCtClass(classPath);
        CtConstructor[] cons = cc.getConstructors();
        System.out.println("masno"+cons[0].getParameterTypes()[0].getName());
        CtConstructor con = cc.getConstructor(getArgumentsAsString(arguments));


        cc.removeConstructor(con);
        jcm.saveCtClass(cc);
    }

    public void setConstructorBody(String classPath, Map<String, String> arguments, String constructorBody) throws NotFoundException, CannotCompileException {
        CtClass cc = getCtClass(classPath);
        CtConstructor con = cc.getConstructor(getArgumentsAsString(arguments));
        con.setBody(constructorBody);
        jcm.saveCtClass(cc);
    }

    public void listClassMethods(String classPath) throws CannotCompileException {
        Class clazz = getCtClass(classPath).toClass();
        Method[] methods = clazz.getDeclaredMethods();
        System.out.println("Methods in class " + classPath);
        for (Method m : methods) {
            System.out.println(m.getName());
        }
    }

    public void listClassFields(String classPath) throws CannotCompileException {
        Class clazz = getCtClass(classPath).toClass();
        Field[] fields = clazz.getDeclaredFields();
        System.out.println("Fields in class " + classPath);
        for (Field f : fields) {
            System.out.println(f.getName());
        }
    }

    public void listClassConstructors(String classPath) throws CannotCompileException {
        Class clazz = getCtClass(classPath).toClass();
        Constructor[] constructors = clazz.getConstructors();
        System.out.println("Fields in class " + classPath);
        for (Constructor c : constructors) {
            System.out.println(c.getName());
        }
    }

    public String getArgumentsAsString(Map<String, String> arguments) {
        StringBuilder argumentsBuilder = new StringBuilder();
        argumentsBuilder.append("(");

        for (Map.Entry<String, String> entry : arguments.entrySet()) {
            argumentsBuilder.append(entry.getKey()).append(" ").append(entry.getValue()).append(",");
        }

        argumentsBuilder.append(")");

        //Usunięcie ostatniego
        return argumentsBuilder.toString().replaceAll(",\\s*(?=\\))","");

    }

    public CtMethod getCtMethod(CtClass cc, String methodName) {
        CtMethod cm = null;
        try {
            for(CtMethod cm2:cc.getDeclaredMethods()){
                System.out.println(cm2.getName());
            }
            cm = cc.getDeclaredMethod(methodName);
        } catch (NotFoundException e) {
            System.out.println("Error in getting method from class" + cc.getName());
            e.printStackTrace();
        }
        return cm;
    }

    public CtClass getCtClass(String classPath) {
        CtClass temp = null;
        try {
            System.out.println(classPath);
            temp = jcm.getJarClassPool().get(classPath);

        } catch (Exception e) {
            System.out.println("Error in getting class from class Pool");
        }
        if(temp==null){
            try {
                throw new Exception("Class "+classPath+" doesnt exist");
            } catch (Exception e) {
                e.printStackTrace();
                jcm.saveAndExit();
                System.exit(0);
            }
        }
        temp.defrost();
        return temp;

    }
}
