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
        for(String arg: args){

            if(arg.equals("--list-classes")){
                jcm.printClassNames();
            }
            else if(arg.equals("--list-packages")){
                jcm.printPackageNames();
            }
            else if(arg.charAt(0)=='-'&&arg.charAt(1)=='-') operationType = arg;
            else if(operationType.equals("--i")){
                jcm = new JarContentManager(arg);
                cm = new ClassesManager(jcm);
                operationType ="";
            }
            else if(operationType.equals("--list-methods")){
                cm.listClassMethods(arg);
                operationType ="";
            }
            else if(operationType.equals("--list-fields")){
                cm.listClassFields(arg);
                operationType ="";
            }
            else if(operationType.equals("--list-ctors")){
                cm.listClassFields(arg);
                operationType ="";
            }
            else if(operationType.equals("--script")){
                jcm.initForScript();
                si = new ScriptInterpreter(arg,jcm,cm);


            }

        }
    }
}
