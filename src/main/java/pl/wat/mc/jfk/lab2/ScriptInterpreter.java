package pl.wat.mc.jfk.lab2;

import javassist.CannotCompileException;
import javassist.NotFoundException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptInterpreter {

    private final String scriptPath;
    private final List<String> scriptLines;
    private JarContentManager jcm;
    private ClassesManager cm;
    final public static Pattern instructionPattern = Pattern.compile("^(add-package|remove-package|add-method|remove-method|" +
            "add-class|remove-class|add-interface|remove-interface|set-method-body|add-before-method|add-after-method|" +
            "add-field|remove-field|add-ctor|remove-ctor|set-ctor-body)");
    private Pattern bodyPattern = Pattern.compile("(?=\\{).*(?!\\})");


    public ScriptInterpreter(String scriptPath,JarContentManager jcm,ClassesManager cm) throws CannotCompileException, NotFoundException {
        this.cm = cm;
        this.jcm = jcm;
        this.scriptPath=scriptPath;
        this.scriptLines = getFileLines(scriptPath);
        fileInterpreter();

    }

    public void fileInterpreter() throws CannotCompileException, NotFoundException {

        StringBuilder buffer = new StringBuilder();
        String instructionName;
        String methodBody = "";
        Instruction instr = null;
        boolean lastLineExecuted = false;

        for(int i =0;i<scriptLines.size();i++){
            String scriptLine = scriptLines.get(i);
            instructionName = getResultFromMatcher(instructionPattern,scriptLine);
            buffer.append(scriptLine);
            if((i+1>=scriptLines.size())||
                    (i+1<scriptLines.size()&&
                            getResultFromMatcher(instructionPattern, scriptLines.get(i+1))!="null")){

                String bufferString = buffer.toString();
                //System.out.println(bufferString+"asdasd");
                methodBody = getResultFromMatcher(bodyPattern,bufferString);
                //System.out.println(methodBody);
                if(!methodBody.equals("null")) {
                    bufferString = bufferString.substring(0, bufferString.indexOf('{'));
                }
                instr = new Instruction(instructionName,bufferString , methodBody,cm);
                instr.execute();

                buffer = new StringBuilder();
                instructionName = "";
                methodBody = "";
                instr = null;
            }

        }
        jcm.saveAndExit();

    }

    public static String getResultFromMatcher(Pattern pattern, String line){
        Matcher matcher = null;
        String foundGroup = "null";
        matcher = pattern.matcher(line);
        if(matcher.find())
            foundGroup = matcher.group();

        return foundGroup;
    }
    public static List<String> getAllResultsFromMatcher(Pattern pattern,String line){
        Matcher matcher = pattern.matcher(line);
        List <String> returnList = new ArrayList<>();
        while (matcher.find()) {
            String group = matcher.group();

            if(!group.equals("")) returnList.add(group);
        }
        return returnList;
    }

    private List<String> getFileLines(String path){
        List <String> lines = new ArrayList<>();
        try{
            File myFile = new File(path);
            Scanner myReader = new Scanner(myFile);
            while(myReader.hasNextLine()){
                lines.add(myReader.nextLine());
            }
        }
        catch(FileNotFoundException e)
        {
            System.out.println(path+" not found.");
        }
        return lines;
    }
}
