/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_mannheim.informatik.dws.ontmatching.demomatcher;

import de.uni_mannheim.informatik.dws.ontmatching.matchingexternal.MatcherExternal;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Sven Hertling
 */
public class DemoPythonMatcher extends MatcherExternal {

    @Override
    protected List<String> getCommand(URL source, URL target, URL inputAlignment) throws Exception {
        List<String> command = new ArrayList<>();
        command.add("python");
        command.add("oaei-resources" + File.separator + "pythonMatcher.py");
        command.add(source.toString());
        command.add(target.toString());
        if(inputAlignment != null)
            command.add(inputAlignment.toString());
        return command;
        //return new ArrayList(Arrays.asList("python", "oaei-resources" + File.separator + "pythonMatcher.py", source.toString(), target.toString()))
    }
    
}
