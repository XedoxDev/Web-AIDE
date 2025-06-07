package org.xedox.webaide.format;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;

public class JSFormatter implements IFormatter {

    @Override
    public String format(CharSequence source, int tabsize) {
        com.google.javascript.jscomp.Compiler compiler = new com.google.javascript.jscomp.Compiler();
        
        CompilerOptions options = new CompilerOptions();
        options.setPrettyPrint(true);
        options.setLineLengthThreshold(80);
        options.setSkipNonTranspilationPasses(true);
        try {
            Result result = compiler.compile(
                SourceFile.fromCode("externs.js", ""),
                SourceFile.fromCode("input.js", source.toString()),
                options
            );
            
            if (!result.success) {
                for (JSError error : compiler.getErrors()) {
                    System.err.println("JS Format Error: " + error.getDescription());
                }
                return source.toString();
            }
            
            return compiler.toSource();
            
        } catch (Exception e) {
            e.printStackTrace();
            return source.toString();
        }
    }
}