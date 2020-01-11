package pl.wat.mc.jfk.lab2;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import java.io.IOException;


public class Main {
    public static void main (String[] args) throws IOException, NotFoundException, CannotCompileException {
        ArgumentsInterpreter ai = new ArgumentsInterpreter(args);
    }
}
