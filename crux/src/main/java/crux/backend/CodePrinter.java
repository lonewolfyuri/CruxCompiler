package crux.backend;
import java.util.*;
import java.io.*;

public class CodePrinter {
    PrintStream out;
    StringBuffer sb = new StringBuffer();

    public CodePrinter(String name) {
        try {
            out = new PrintStream(name);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void printLabel(String s) {
        out.println(s);
    }

    public void printCode(String s) {
        out.println("    "+s);
    }

    public void bufferCode(String s) {
        sb.append("    " + s + "\n");
    }

    public void bufferLabel(String s) {
        sb.append(s + "\n");
    }

    public void outputBuffer() {
        out.print(sb);
        sb = new StringBuffer();
    }

    public void close() {
        out.close();
    }
}
