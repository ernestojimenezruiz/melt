package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrixMetric;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class EvaluatorCSVTest {

    private static Logger LOGGER = LoggerFactory.getLogger(EvaluatorCSVTest.class);

    /**
     * This tests uses the 2018 alignment files for DOME and ALOD2Vec and evaluates them on the OAEI Anatomy data set
     * using EvaluatorCSV. This test makes sure that something is written and that setting of the base directory works.
     *
     * This test is for complete gold standards (default case).
     */
    @Test
    void testEvaluator() {
        ExecutionResultSet resultSet = Executor.loadFromFolder("./src/test/resources/externalAlignmentForEvaluation", TrackRepository.Anatomy.Default.getTestCases().get(0));
        EvaluatorCSV evaluatorCSV = new EvaluatorCSV(resultSet);
        File baseDirectory = new File("./testBaseDirectory");
        baseDirectory.mkdir();
        evaluatorCSV.writeToDirectory(baseDirectory);
        assertTrue(baseDirectory.listFiles().length > 0);

        File trackPerformanceCubeFile = new File("./testBaseDirectory/trackPerformanceCube.csv");
        File testCasePerformanceCubeFile = new File("./testBaseDirectory/testCasePerformanceCube.csv");

        assertTrue(trackPerformanceCubeFile.exists());
        assertTrue(testCasePerformanceCubeFile.exists());

        // somewhat complex test, testing the correspondence count:
        try {
            BufferedReader reader = new BufferedReader(new FileReader(trackPerformanceCubeFile));
            String readLine;
            int positionTp = -1;
            int positionFp = -1;
            int positionCorrespondencesNumber = -1;

            boolean loop_1 = true;

            while((readLine = reader.readLine()) != null){
                String[] splitLine = readLine.split(",");
                if(positionTp == -1){
                    positionTp = getPosition("# of TP", splitLine);
                }
                if(positionFp == -1){
                    positionFp = getPosition("# of FP", splitLine);
                }
                if(positionCorrespondencesNumber == -1){
                    positionCorrespondencesNumber = getPosition("# of Correspondences", splitLine);
                }

                if(!loop_1) {
                    assertEquals(Integer.parseInt(splitLine[positionTp]) + Integer.parseInt(splitLine[positionFp]), Integer.parseInt(splitLine[positionCorrespondencesNumber]));
                } else loop_1 = false;
            }

        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            fail();
        } catch (IOException ioe){
            ioe.printStackTrace();
            fail();
        }

        try {
            FileUtils.deleteDirectory(baseDirectory);
        } catch (IOException ioe) {
            LOGGER.error("Could not clean up after test. Test directory 'testBaseDirectory' still exists on disk.", ioe);
        }
    }

    /**
     * This tests uses the 2018 alignment files for DOME and ALOD2Vec and evaluates them on the OAEI Anatomy data set
     * using EvaluatorCSV. This test makes sure that something is written and that setting of the base directory works.
     *
     * This test tests incomplete alignments / partial gold standard.
     */
    @Test
    void testEvaluatorPartialGS() {
        ExecutionResultSet resultSet = Executor.loadFromFolder("./src/test/resources/externalAlignmentForEvaluation", TrackRepository.Anatomy.Default.getTestCases().get(0));

        for(ExecutionResult r : resultSet){
            // add random alignment that is not false...
            r.getSystemAlignment().add("http://www.test.eu/ABC", "http://www.test.eu/DEF");
        }

        EvaluatorCSV evaluatorCSV = new EvaluatorCSV(resultSet, new ConfusionMatrixMetric(true, true));
        File baseDirectory = new File("./testBaseDirectory");
        baseDirectory.mkdir();
        evaluatorCSV.writeToDirectory(baseDirectory);
        assertTrue(baseDirectory.listFiles().length > 0);

        File trackPerformanceCubeFile = new File("./testBaseDirectory/trackPerformanceCube.csv");
        File testCasePerformanceCubeFile = new File("./testBaseDirectory/testCasePerformanceCube.csv");

        assertTrue(trackPerformanceCubeFile.exists());
        assertTrue(testCasePerformanceCubeFile.exists());

        // somewhat complex test, testing the correspondence count:
        try {
            BufferedReader reader = new BufferedReader(new FileReader(trackPerformanceCubeFile));
            String readLine;
            int positionTp = -1;
            int positionFp = -1;
            int positionCorrespondencesNumber = -1;

            boolean loop_1 = true;

            while((readLine = reader.readLine()) != null){
                String[] splitLine = readLine.split(",");
                if(positionTp == -1){
                    positionTp = getPosition("# of TP", splitLine);
                }
                if(positionFp == -1){
                    positionFp = getPosition("# of FP", splitLine);
                }
                if(positionCorrespondencesNumber == -1){
                    positionCorrespondencesNumber = getPosition("# of Correspondences", splitLine);
                }

                if(!loop_1) {
                    // Now we add 1 to the expected result because the added correspondence is not counted as a FP because we use a partial gold standard here!
                    int tpAndFp = Integer.parseInt(splitLine[positionTp]) + Integer.parseInt(splitLine[positionFp]);
                    int cNumber = Integer.parseInt(splitLine[positionCorrespondencesNumber]);
                    // TODO comment in for failure
                    //assertTrue(tpAndFp < cNumber, "The following assertion failed: " + tpAndFp + " < " + cNumber);
                } else loop_1 = false;
            }

        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            fail();
        } catch (IOException ioe){
            ioe.printStackTrace();
            fail();
        }

        try {
            FileUtils.deleteDirectory(baseDirectory);
        } catch (IOException ioe) {
            LOGGER.error("Could not clean up after test. Test directory 'testBaseDirectory' still exists on disk.", ioe);
        }
    }

    /**
     * Helper method. Return the position in the array given a string.
     * @param title String to be looked for.
     * @param array Array to be searched in.
     * @return -1 if not found, else position (starting couting with 0).
     */
    private static int getPosition(String title, String[] array){
        int result = 0;
        for(String s : array){
            s = s.replaceAll("\"", "");
            if(s.equals(title)) return result;
            result++;
        }
        return -1;
    }

}