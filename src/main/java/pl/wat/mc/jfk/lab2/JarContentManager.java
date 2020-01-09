package pl.wat.mc.jfk.lab2;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.*;


public class JarContentManager {

    public final String filePath;
    private final String fileName;
    private final String fullPath;
    private final String tempFolderName = "/temp/";
    public ClassPool classPool;
    private Manifest manifest;

    public final List<String> classNames;
    public final List<String> packageNames;

    public JarContentManager(String jarPath) throws IOException, NotFoundException {

        this.fullPath = jarPath;
        this.filePath = jarPath.substring(0,jarPath.lastIndexOf('/'));
        this.fileName = jarPath.substring(jarPath.lastIndexOf('/'));
        this.classNames = new ArrayList<>();
        this.packageNames = new ArrayList<>();
        this.classPool = getJarClassPool();
        getPackageAndClassNames();

        initForScript();
        //this.classUrls = new URL[]{new URL("jar:file:" + jarPath + "!/")};

    }
    public void initForScript() throws NotFoundException, IOException {
        getJarClassPool();
        getJarManifest();
        unzipJarToTemp();
    }
    public void saveAndExit() {
        createJarFromTemp();
        deleteTempFolder();

    }
    public void saveCtClass(CtClass clazz){
        try {
            clazz.writeFile(filePath+tempFolderName);
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Error in writing class.");
        }
    }

    public ClassPool getJarClassPool() {
        if(classPool==null){
            try {
                classPool = ClassPool.getDefault();
                classPool.insertClassPath(filePath + tempFolderName);
                classPool.doPruning = false;
            }catch(NotFoundException e){
                e.printStackTrace();
                System.out.println("Error in getting class pool in path"+filePath+tempFolderName);
            }
        }
        return classPool;

    }
    public Manifest getJarManifest() throws IOException {
        if(manifest==null){
            File file = new File(fullPath);
            FileInputStream fis = new FileInputStream(file);
            JarInputStream jarStream = new JarInputStream(fis);
            manifest = jarStream.getManifest();
        }

        return manifest;
    }
    public void createFolder(String folderPath) {
        File file = new File(filePath+tempFolderName+folderPath);
        file.mkdirs();
    }
    public void getPackageAndClassNames() throws IOException {
        File file = new File(fullPath);
        JarFile jar = new JarFile(file);
        for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();){
            JarEntry entry = enums.nextElement();
            String name = entry.getName();
            if(name.endsWith(".class")) classNames.add(entry.getName().replaceAll("/","\\."));
            else if (name.endsWith("/")&&!name.equals("META-INF/")) packageNames.add(name.replaceAll("/","."));
        }
    }
    public void printClassNames(){
        for(String className:classNames){
            System.out.println(className);
        }
    }
    public void printPackageNames(){
        for(String packageName:packageNames){
            System.out.println(packageName);
        }
    }
    public void unzipJarToTemp() throws IOException {
        File file = new File(fullPath);
        JarFile jar = new JarFile(file);

        String tempFilePath = filePath+tempFolderName;
        File tempFolder = new File(tempFilePath);
        tempFolder.mkdirs();

        for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
            JarEntry entry = enums.nextElement();

            String fileName = tempFilePath + File.separator + entry.getName();
            File f = new File(fileName);

            if (fileName.endsWith("/")) {
                f.mkdirs();
            }

        }
        for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
            JarEntry entry = enums.nextElement();

            String fileName = tempFilePath + File.separator + entry.getName();
            File f = new File(fileName);

            if (!fileName.endsWith("/")) {
                InputStream is = jar.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(f);
                while (is.available() > 0) {
                    fos.write(is.read());
                }
                fos.close();
                is.close();
            }
        }
    }
    public void deleteFile(String path){
        File file = new File(filePath+tempFolderName+path);
        file.delete();
    }
    public void deleteTempFolder(){
        String pathToTemp = filePath+tempFolderName;
        File tempDirectory = new File(pathToTemp);
        deleteDirectory(tempDirectory);
    }
    public void deleteDirectory(File folder){
        if(folder.exists()){
            File[] files = folder.listFiles();
            if(null!=files){
                for (File file : files) {
                    if (file.isDirectory()) {

                        deleteDirectory(file);
                    } else {
                        System.out.println(file.getAbsolutePath());
                        file.delete();
                    }
                }
            }
        }
    }
    public void createJarFromTemp() {
        JarOutputStream target = null;
        try {
            target = new JarOutputStream(new FileOutputStream("C:/temp/outpsut.jar"), manifest);
            File inputDirectory = new File(filePath+tempFolderName);
            for (File nestedFile : inputDirectory.listFiles())
                createJarFile("", nestedFile, target);
            target.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }
    public void createJarFile(String parent,File source, JarOutputStream target) throws IOException {

        BufferedInputStream in = null;
        try
        {
            String name = (parent + source.getName()).replace("\\", "/");

            if (source.isDirectory())
            {
                if (!name.isEmpty())
                {
                    if (!name.endsWith("/"))
                        name += "/";
                    JarEntry entry = new JarEntry(name);
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                }
                for (File nestedFile : source.listFiles())
                    createJarFile(name, nestedFile, target);
                return;
            }

            if(source.getName().equals("MANIFEST.MF")) return;
            System.out.println(name);

            JarEntry entry = new JarEntry(name);
            entry.setTime(source.lastModified());
            target.putNextEntry(entry);
            in = new BufferedInputStream(new FileInputStream(source));

            byte[] buffer = new byte[1024];
            while (true)
            {
                int count = in.read(buffer);
                if (count == -1)
                    break;
                target.write(buffer, 0, count);
            }
            target.closeEntry();
        }
        finally
        {
            if (in != null)
                in.close();
        }
    }
}
