package ast;

import java.io.PrintWriter;

public class SemantError{
    public static PrintWriter out = null;

    public static void error(int lineNumber) {
        if (out != null) {
            out.print("ERROR(" + lineNumber + ")");
            out.flush();
        }
        
        else {
            System.out.print("ERROR(" + lineNumber + ")");
        }

        System.exit(0);
    }

}
