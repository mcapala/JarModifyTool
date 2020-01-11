package pl.wat.mc.jfk.lab2;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;

import java.util.*;
import java.util.regex.Pattern;

public class Instruction {

    private JarContentManager jcm;
    private ClassesManager cm;

    private Pattern namePattern = Pattern.compile("(?!\\s*:\\s*)[A-Za-z$_0-9]+(?=\\s*)");
    private Pattern srcPattern = Pattern.compile("(?!\\s*:\\s*).*");
    private Pattern packagePattern = Pattern.compile("(\\w[\\w\\d]*\\.)+");
    private Pattern classPattern = Pattern.compile("(\\w[\\w\\d]*\\.)+[\\w\\d]+");
    private Pattern primitives = Pattern.compile("(boolean|char|byte|short|int|long|float|double|void)");
    private Pattern argumentsPattern = Pattern.compile("(?!,\\s*\\(\\s*)("+classPattern.toString()+",?|\\s*"+primitives+"\\s*,?)*(?=\\s*\\)\\s*)");

    private String instructionParams;
    private String instructionName;
    private String packageName;
    private String className;
    private String name;
    private String src;
    private String bodyCode;

    private CtClass[] arguments;

    boolean parametrizedInstruction = false;
    public Instruction(String instructionName,String instructionParameters,String bodyCode,ClassesManager cm,JarContentManager jcm) {
        this.cm = cm;
        this.jcm = jcm;
        if(instructionParameters.contains(":")) parametrizedInstruction = true;
        this.instructionParams = instructionParameters;
        this.instructionName = instructionName;
        this.packageName = ScriptInterpreter.getResultFromMatcher(packagePattern,instructionParams);
        this.className = ScriptInterpreter.getResultFromMatcher(classPattern,instructionParams);

        this.instructionParams = instructionParams.replace(className,"");


        if(parametrizedInstruction){
            this.name = ScriptInterpreter.getResultFromMatcher(namePattern,instructionParams);
            this.bodyCode = bodyCode;
            this.src = ScriptInterpreter.getResultFromMatcher(srcPattern,instructionParams);
            if(bodyCode!="null")src+=bodyCode;
            this.arguments = getArguments(instructionParams);
        }
    }

    public void execute() throws CannotCompileException, NotFoundException {
        if(instructionName.equals("add-class")||instructionName.equals("remove-class")||
            instructionName.equals("add-interface")||instructionName.equals("remove-interface")||
                instructionName.equals("add-package")||instructionName.equals("remove-package")){
            switch(instructionName) {
                case "add-class":
                    cm.addClass(className);
                    break;
                case"remove-class":
                    cm.removeClass(className);
                    break;
                case"add-interface":
                    cm.addInterface(className);
                    break;
                case"remove-interface":
                    cm.removeInterface(className);
                    break;
                case"add-package":
                    cm.addPackage(packageName);
                    break;
                case"remove-package":
                    cm.removePackage(packageName);
                    break;
            }
        }
        else if(instructionName.equals("remove-field")){
            cm.removeField(className, name);
        }
        else if(instructionName.equals("remove-method")){
            cm.removeMethod(className,name,arguments);
        }
        else if (instructionName.equals("add-before-method")||instructionName.equals("add-after-method")||instructionName.equals("set-method-body")){
            switch(instructionName){
                case"add-before-method":
                    cm.addCodeBeforeMethod(className,name,arguments,bodyCode);
                    break;
                case"add-after-method":
                    cm.addCodeAfterMethod(className,name,arguments,bodyCode);
                    break;
                case"set-method-body":
                    cm.setMethodBody(className,name,arguments,bodyCode);
                    break;
            }
        }
        else if (instructionName.equals("add-ctor")||instructionName.equals("add-field")||instructionName.equals("add-method")){
            switch(instructionName){
                case"add-ctor":
                    cm.addConstructor(className,src);
                    break;
                case"add-field":
                    cm.addField(className,src);
                    break;
                case"add-method":
                    cm.addMethod(className,src);
                    break;
            }

        }
        else if (instructionName.equals("remove-ctor")){
            cm.removeConstructor(className,arguments);

        }
        else if (instructionName.equals("set-ctor-body")){
            cm.setConstructorBody(className,arguments,bodyCode);
        }
    }
    private CtClass[] getArguments(String instructionParameters)  {
        List<CtClass> ctList = new ArrayList<>();

        CtClass[] array = null;

        List<String> argumentsList = ScriptInterpreter.getAllResultsFromMatcher(argumentsPattern,instructionParameters);

        for(Iterator iter = argumentsList.iterator();iter.hasNext();){
            CtClass clazz = null;
            try {
                clazz = jcm.getJarClassPool().get(iter.next().toString());
            }
            catch(Exception e){}
            ctList.add(clazz);
        }
        if(ctList.size()>0) {
            array = new CtClass[ctList.size()];
            for(int i =0;i<ctList.size();i++){
                array[i] = ctList.get(i);
            }
        }

        return array;

    }

}
