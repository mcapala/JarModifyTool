package pl.wat.mc.jfk.lab2;

import javassist.CannotCompileException;
import javassist.NotFoundException;

import java.io.IOException;

public class ArgumentsInterpreter {

    private final String[] args;
    private JarContentManager jcm;
    private ClassesManager cm;
    private ScriptInterpreter si;
    public ArgumentsInterpreter(String[] args) throws IOException, NotFoundException, CannotCompileException {
        this.args = args;

        mainLoop();
    }
    public void mainLoop() throws IOException, NotFoundException, CannotCompileException {
        String operationType = null;
        boolean listingFlag = false;
        boolean scriptFlag = false;
        for(String arg: args){
            if (arg.equals("--list-classes")) {
                listingFlag = true;
                jcm.printClassNames();
            } else if (arg.equals("--list-packages")) {
                listingFlag = true;
                jcm.printPackageNames();
            }
            else if(arg.charAt(0)=='-'&&arg.charAt(1)=='-') operationType = arg;
            else if(operationType.equals("--i")){
                jcm = new JarContentManager(arg);
                cm = new ClassesManager(jcm);
            }
            else if(operationType.equals("--o")){
                jcm.outputFileName = arg;
                si.fileInterpreter();
            }
            else if(operationType.equals("--script")){
                if(!listingFlag) {
                    scriptFlag=true;
                    jcm.initForScript();
                    si = new ScriptInterpreter(arg, jcm, cm);
                }
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
        if(jcm.outputFileName == null){
            System.out.println("Output file parameter missing. Changes won't be saved.");
        }
    }
}
