package de.uni_mannheim.informatik.dws.melt.matching_eval.tracks;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Track on the SEALS platform.
 */
public class SealsTrack extends Track {
    private static final Logger LOGGER = LoggerFactory.getLogger(SealsTrack.class);
    
    protected String tdrsLocation;
    protected String testDataCollectionName;
    protected String testDataVersionNumber;
    
    public SealsTrack(String tdrsLocation, String testDataCollectionName, String testDataVersionNumber){
        this(tdrsLocation, testDataCollectionName, testDataVersionNumber, getNiceRemoteLocation(tdrsLocation));
    }

    /**
     *
     * @param tdrsLocation Repository location e.g. "http://repositories.seals-project.eu/tdrs/".
     * @param testDataCollectionName Track name e.g. conference, anatomy
     * @param testDataVersionNumber Version of the track.
     * @param nicerLocation To avoid that multiple tracks are created, an own repository location is artificially created.
     */
    public SealsTrack(String tdrsLocation, String testDataCollectionName, String testDataVersionNumber, String nicerLocation){
        this(tdrsLocation, testDataCollectionName, testDataVersionNumber, nicerLocation, false);
    }
    
    public SealsTrack(String tdrsLocation, String testDataCollectionName, String testDataVersionNumber, boolean useDuplicateFreeStorageLayout){
        this(tdrsLocation, testDataCollectionName, testDataVersionNumber, getNiceRemoteLocation(tdrsLocation), useDuplicateFreeStorageLayout);
    }
    
    public SealsTrack(String tdrsLocation, String testDataCollectionName, String testDataVersionNumber, String nicerLocation, boolean useDuplicateFreeStorageLayout){
        super(nicerLocation, testDataCollectionName, testDataVersionNumber, useDuplicateFreeStorageLayout);
        this.tdrsLocation = tdrsLocation;
        this.testDataCollectionName = testDataCollectionName;
        this.testDataVersionNumber = testDataVersionNumber;
    }    

    @Override
    protected void downloadToCache() throws IOException {
        LOGGER.info("Downloading track {}", testDataCollectionName);
        SealsDownloadHelper bmd = new SealsDownloadHelper(tdrsLocation, testDataCollectionName, testDataVersionNumber);
        for(String testCaseId : bmd.getTestCases()){
            LOGGER.info("  currently downloading {}", testCaseId);
            URL source = bmd.getDataItem(testCaseId, "source");
            URL target = bmd.getDataItem(testCaseId, "target");
            URL reference = bmd.getDataItem(testCaseId, "reference");
            
            if(exists(source) == false || exists(target) == false){
                LOGGER.error("Source or Target is not defined - continue");
                continue;
            }
            
            this.saveInDefaultLayout(source, testCaseId, TestCaseType.SOURCE);
            this.saveInDefaultLayout(target, testCaseId, TestCaseType.TARGET);
            if(exists(reference)){
                this.saveInDefaultLayout(reference, testCaseId, TestCaseType.REFERENCE);
            }
        }
        LOGGER.info("Finished downloading track {}", testDataCollectionName);
    }
    
    private static boolean exists(URL url)
    {
        try
        {
                HttpURLConnection.setFollowRedirects(false);
                HttpURLConnection con = (HttpURLConnection) new URL(String.valueOf(url)).openConnection();
                con.setRequestMethod("HEAD");
                return con.getResponseCode() == HttpURLConnection.HTTP_OK;
        }
        catch(IOException e){ return false; }
    }
}
