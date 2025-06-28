package org.xedox.javac;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class OptionsBuilder {
    
    protected final List<String> args = new ArrayList<>();
    
    public List<String> getArgs() {
        return args;
    }
    
    public OptionsBuilder arg(String... argument) {
        for(String arg : argument) {
        	this.args.add(arg);
        }
        return this;
    }
    
    public OptionsBuilder arg(List<String> arguments) {
        this.args.addAll(arguments);
        return this;
    }
    
    public String[] build() {
        return this.args.toArray(new String[0]);
    }

    public String buildCmd() {
    	return String.join(" ", args);
    }
}