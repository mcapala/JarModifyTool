package pl.wat.mc.jfk.lab2;

import javassist.CannotCompileException;
import javassist.NotFoundException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Instruction {

    private String instructionName;



    private ClassesManager cm;

    private Pattern accesses = Pattern.compile("(public|private|static|protected|abstract|native|synchronized)");

    private Pattern packagePattern = Pattern.compile("(\\w[\\w\\d]*\\.)+");
    private String packageName;
    private Pattern classPattern = Pattern.compile("(\\w[\\w\\d]*\\.)+[\\w\\d]+");
    private String className;
    private Pattern accessPattern = Pattern.compile("(?!\\s*:\\s*)"+accesses.toString()+"(?=\\s*)");
    private String accessibility;
    private Pattern staticPattern = Pattern.compile("(?="+accessPattern.toString()+"\\s*)static(?!\\s*)");
    private boolean isStatic;
    //private Pattern returnPattern = Pattern.compile("(?=(public|private|static|protected|abstract|native|synchronized))[a-zA-Z0-9<>._?, ]*");
    private String returnType;
    private Pattern namePattern;
    private String name;
    private Pattern argumentsPattern = Pattern.compile("(?!,\\s*\\(\\s*)([\\w\\d\\[\\]]+\\s+[\\w\\d]+,|[\\w\\d\\[\\]]+\\s+[\\w\\d]+)*(?=\\s*\\)\\s*)");
    private Map<String,String> arguments;

    private String bodyCode;

    private String instrParams;

    private Pattern innerInstructionPattern =
            Pattern.compile("(?!"+ScriptInterpreter.instructionPattern.toString()+"\\s*\\(\\s*).*(?=\\))");

    boolean isMultipleParType = false;


    public Instruction(String instructionName,String instructionParameters,String bodyCode,ClassesManager cm) {
        this.cm = cm;
        this.instructionName = instructionName;
        //this.instructionParameters = ScriptInterpreter.getResultFromMatcher(innerInstructionPattern,instructionParameters);
        this.instrParams =  instructionParameters.substring(instructionName.length()+1,instructionParameters.length()-1);
        this.packageName = ScriptInterpreter.getResultFromMatcher(packagePattern,instructionParameters);
        this.accessibility = ScriptInterpreter.getResultFromMatcher(accessPattern,instructionParameters);
        this.className = ScriptInterpreter.getResultFromMatcher(classPattern,instructionParameters);
        this.returnType = getReturnType(instructionParameters);
        if(ScriptInterpreter.getResultFromMatcher(staticPattern,instructionParameters).equals("static")){
            isStatic = true;
        }
        setName();

        this.bodyCode = bodyCode;
        setInstructionType();
        if(isMultipleParType){
            this.arguments = getArguments(instructionParameters);
        }

    }
    private void setName(){
        if(instructionName.equals("remove-method")||instructionName.equals("remove-field")||instructionName.equals("remove-ctor")){
            name = instrParams.substring(instrParams.indexOf(':')+1,instrParams.length());
        }
        else if(!instructionName.equals("add-ctor")){
           // namePattern = Pattern.compile("(?!"+returnType+"\\s+)\\w[A-Za-z0-9$_]*\\s*(?=\\()");
            namePattern = Pattern.compile("(?!"+returnType+"\\s+)\\w[A-Za-z0-9$_]*\\s*(?=(\\(|$))");
            this.name = ScriptInterpreter.getResultFromMatcher(namePattern,instrParams);
            System.out.println("elo");
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
        else if(instructionName.equals("remove-method")||instructionName.equals("remove-field")){
            switch(instructionName) {
                case "remove-method":
                    cm.removeMethod(className, name);
                    break;
                case "remove-field":
                    cm.removeField(className, name);
                    break;
            }
        }
        else if (instructionName.equals("add-before-method")||instructionName.equals("add-after-method")||instructionName.equals("set-method-body")){
            switch(instructionName){
                case"add-before-method":
                    cm.addCodeBeforeMethod(className,name,bodyCode);
                    break;
                case"add-after-method":
                    cm.addCodeAfterMethod(className,name,bodyCode);
                    break;
                case"set-method-body":
                    cm.setMethodBody(className,name,bodyCode);
                    break;
            }
        }
        else if (instructionName.equals("add-ctor")){
            cm.addConstructor(className,accessibility,arguments,bodyCode);
        }
        else if (instructionName.equals("remove-ctor")){
            cm.removeConstructor(className,arguments);

        }
        else if(instructionName.equals("add-field")){
            cm.addField(className,accessibility,returnType,name);
        }
        else if(instructionName.equals("add-method")){
            System.out.println("Instruc");
            cm.addMethod(className,accessibility,isStatic,returnType,name,arguments,bodyCode);
        }
        else if (instructionName.equals("set-ctor-body")){
            cm.setConstructorBody(className,arguments,bodyCode);
        }
    }
    private void  setInstructionType(){
        if(instrParams.contains(":")){
            isMultipleParType = true;
        }
    }
    private Map<String,String> getArguments(String instructionParameters) {
        Map<String,String> argumentsMap = new HashMap<>();

        List<String> argumentsList = ScriptInterpreter.getAllResultsFromMatcher(argumentsPattern,instructionParameters);

        for(Iterator iter = argumentsList.iterator();iter.hasNext();){
            String []argument = iter.next().toString().replaceAll("\\s+"," ").split(" ");
            try {
                argumentsMap.put(getClassType(argument[0]), argument[1]);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        return argumentsMap;

    }
    private String getReturnType(String instructionParameters){
        String[] tokens = instructionParameters.split(" ");
        String returnType = null;
        for(int i =0;i<tokens.length;i++){
            System.out.println(tokens[i]);
            if(ScriptInterpreter.getResultFromMatcher(accessPattern,tokens[i])!=""&&
                    ((i+1<tokens.length)&&ScriptInterpreter.getResultFromMatcher(accessPattern,tokens[i+1])=="null")){
                returnType = tokens[i+1];
                break;
            }
        }
        return returnType;
    }
    private String getClassType(String name){
        String returnName ="";
        Map<String,Class> builtInMap = new HashMap<String,Class>();{
            builtInMap.put("int", Integer.TYPE );
            builtInMap.put("long", Long.TYPE );
            builtInMap.put("double", Double.TYPE );
            builtInMap.put("float", Float.TYPE );
            builtInMap.put("bool", Boolean.TYPE );
            builtInMap.put("char", Character.TYPE );
            builtInMap.put("byte", Byte.TYPE );
            builtInMap.put("void", Void.TYPE );
            builtInMap.put("short", Short.TYPE );
        }
        if(!builtInMap.containsKey(name)){
            try {
                returnName =  Class.forName(name).getPackage().getName();
            }
            catch(Exception e){
                System.out.println(e);
            }

        }else{
            returnName = builtInMap.get(name).getPackage().getName();
        }
        return returnName;

    }
}
