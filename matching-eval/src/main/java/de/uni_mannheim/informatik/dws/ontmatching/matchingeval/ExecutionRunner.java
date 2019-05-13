package de.uni_mannheim.informatik.dws.ontmatching.matchingeval;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import eu.sealsproject.platform.res.tool.api.ToolBridgeException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Individual execution object for parallel execution.
 * Development Remark: Not a public class because this should not be called by user of framework.
 * @author Sven Hertling
 * @author Jan Portisch
 */
class ExecutionRunner implements Callable<ExecutionResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionRunner.class);
    
    private TestCase testCase;
    private IOntologyMatchingToolBridge matcher;        
    private String matcherName;

    public ExecutionRunner(TestCase testCase, IOntologyMatchingToolBridge matcher, String matcherName){
        this.testCase = testCase;
        this.matcher = matcher;
        this.matcherName = matcherName;
    }

    @Override
    public ExecutionResult call() throws Exception {
        return runMatcher(testCase, matcher, matcherName);
    }

    /**
     * Run an individual matcher on an individual test case.
     * @param testCase Test case to be used for run.
     * @param matcher Matcher to be run.
     * @param matcherName Name of the matcher.
     * @return ExecutionResult Object
     */
    public static ExecutionResult runMatcher(TestCase testCase, IOntologyMatchingToolBridge matcher, String matcherName){
        LOGGER.info("Running matcher " + matcherName + ".");
        long runTime = 0;
        URL resultingAlignment = null;
        long startTime = System.nanoTime();
        try {
            resultingAlignment = matcher.align(testCase.getSource().toURL(), testCase.getTarget().toURL());
        } catch (ToolBridgeException | MalformedURLException ex) {
            LOGGER.error("Exception during matching", ex);
        }
        finally
        {
            runTime = System.nanoTime() - startTime;
            LOGGER.info("Running matcher " + matcherName + " completed.");
        }
        if(resultingAlignment == null) {
            LOGGER.error("Matching task unsuccessful: output alignment equals null.");
        } else {
            try {
                new File(resultingAlignment.toURI()).deleteOnExit();
            }catch (URISyntaxException | IllegalArgumentException ex) {
                LOGGER.error("Original system alignment does not point to a file and thus cannot be deleted on eval exit. Use Executor.deleteOriginalSystemResults", ex);
            }
            return new ExecutionResult(testCase, matcherName, resultingAlignment, runTime, matcher);
        }
        return null;
    }
}
