package pl.wat.mc.jfk.lab2;

import javassist.CannotCompileException;
import javassist.NotFoundException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptInterpreter {

    private final String scriptPath;
    private final List<String> scriptLines;
    private JarContentManager jcm;
    private ClassEditor cm;
    final public static Pattern instructionPattern = Pattern.compile("^(add-package|remove-package|add-method|remove-method|" +
            "add-class|remove-class|add-interface|remove-interface|set-method-body|add-before-method|add-after-method|" +
            "add-field|remove-field|add-ctor|remove-ctor|set-ctor-body)");
    private Pattern bodyPattern = Pattern.compile("\\{.*}");
    private Pattern endOfCodePattern = Pattern.compile("(}|;|\\))\\s*\\)$\\s*");
    private Pattern simpleInstruction = Pattern.compile("^(add-package|remove-package|add-method|remove-method|add-class|remove-class|add-interface|remove-interface)\\s*\\(\\s*[A-Za-z\\.]*\\s*\\)");


    public ScriptInterpreter(String scriptPath, JarContentManager jcm, ClassEditor cm) throws CannotCompileException, NotFoundException {
        this.cm = cm;
        this.jcm = jcm;
        this.scriptLines = getScriptLines(scriptPath);
        this.scriptPath = null;
    }

    public void fileInterpreter() throws CannotCompileException, NotFoundException, IOException {

        StringBuilder buffer = new StringBuilder();
        String instructionName = "";

        Instruction instr = null;

        for(int i =0;i<scriptLines.size();i++){
            String scriptLine = scriptLines.get(i);
            if(scriptLine.length()==0) continue;
            if(instructionName.equals(""))
                instructionName = getResultFromMatcher(instructionPattern,scriptLine);

            buffer.append(scriptLine);

            //Jeżeli jest nazwa instrukcji i w następnej linii jest instrukcja lub bufor zawiera : i koniec Patternu
            //lub jeżeli instrukcja bez parametru
            if(instructionName!=null&&
                    ((i+1<scriptLines.size()&&foundMatch(instructionPattern,scriptLines.get(i+1)))||
                    (buffer.toString().contains(":")&&foundMatch(endOfCodePattern,buffer.toString()))||
                            foundMatch(simpleInstruction,scriptLine))){
                buffer = new StringBuilder(buffer.toString().substring(instructionName.length()+1,buffer.toString().length()-1));

                String bufferString = buffer.toString();

                String methodBody = getResultFromMatcher(bodyPattern,bufferString);

                if(!methodBody.equals("null")) {
                    bufferString = bufferString.substring(0, bufferString.indexOf('{'));
                }

                instr = new Instruction(instructionName,bufferString , methodBody,cm,jcm);
                System.out.println("Executing instruction "+instructionName);
                instr.execute();
                buffer = new StringBuilder();
                instructionName = "";
            }

        }
        jcm.saveAndExit();

    }
    public boolean foundMatch(Pattern pattern,String line){
        Matcher matcher = pattern.matcher(line);
        return matcher.find();
    }

    public static String getResultFromMatcher(Pattern pattern, String line){
        String foundGroup = "null";
        Matcher matcher = pattern.matcher(line);
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

    private List<String> getScriptLines(String path){
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
