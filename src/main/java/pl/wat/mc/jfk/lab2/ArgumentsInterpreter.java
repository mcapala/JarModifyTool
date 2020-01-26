package pl.wat.mc.jfk.lab2;

import javassist.CannotCompileException;
import javassist.NotFoundException;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

public class ArgumentsInterpreter {

    private final String[] args;
    private JarContentManager jcm;
    private ClassEditor cm;
    private ScriptInterpreter si;
    public ArgumentsInterpreter(String[] args) throws IOException, NotFoundException, CannotCompileException {
        if(args.length<2) new IllegalArgumentException("Invalid number of arguments.");
        this.args = args;
        mainLoop();
    }
    public void mainLoop() throws IOException, NotFoundException, CannotCompileException {
        String operationType = null;
        boolean listingFlag = false;
        boolean inputSet = false;
        boolean scriptFlag = false;
        boolean classPathFlag = false;
        boolean listClassesFlag = false;
        boolean listPackagesFlag = false;
        for(String arg: args){
            if (arg.equals("--list-classes")) {
                listingFlag = true;
                listClassesFlag=true;

            } else if (arg.equals("--list-packages")) {
                listingFlag = true;
                listPackagesFlag = true;

            }
            else if(arg.charAt(0)=='-'&&arg.charAt(1)=='-') operationType = arg;
            else {
                assert operationType != null;
                if(operationType.equals("--i")){
                    jcm = new JarContentManager(arg);
                    cm = new ClassEditor(jcm);
                    inputSet = true;
                }
                else if(operationType.equals("--o")){
                    jcm.outputFileName = arg;
                }
                else if(operationType.equals("--script")){
                    if(!listingFlag&&arg.charAt(0)!='-') {
                        scriptFlag=true;
                        si = new ScriptInterpreter(arg, jcm, cm);
                    }else throw new IllegalArgumentException();
                }
                else if(operationType.equals("--cp")){
                    jcm.addedClassPath = arg;
                    classPathFlag = true;
                }
                else if(!scriptFlag) {

                    if (operationType.equals("--list-methods")) {
                        listingFlag = true;
                        cm.listClassMethods(arg);
                        operationType ="";
                    } else if (operationType.equals("--list-fields")) {
                        cm.listClassFields(arg);
                        listingFlag = true;
                        operationType ="";
                    } else if (operationType.equals("--list-ctors")) {
                        listingFlag = true;
                        cm.listClassConstructors(arg);
                        operationType ="";
                    }
                }

            }
        }
        if(!inputSet){
            throw new IllegalArgumentException("Input file not set.");
        }

        if(classPathFlag){
            if(listPackagesFlag|listClassesFlag) throw new IllegalArgumentException("Invalid arguments.");
            jcm.addClassPath();
        }

        if(scriptFlag){
            if(listPackagesFlag|listClassesFlag|listingFlag) throw new IllegalArgumentException("Invalid arguments.");
            if(jcm.outputFileName==""){
                throw new NoSuchFileException("Output file name is not set.");
            }
            else{
                jcm.initForScript();
                si.fileInterpreter();
            }
        }
        if(listClassesFlag){
            if(scriptFlag|classPathFlag) throw new IllegalArgumentException("Invalid arguments.");
            jcm.printClassNames();
        }
        if(listPackagesFlag){
            if(scriptFlag|classPathFlag) throw  new IllegalArgumentException("Invalid arguments.");
            jcm.printPackageNames();
        }
    }
}
