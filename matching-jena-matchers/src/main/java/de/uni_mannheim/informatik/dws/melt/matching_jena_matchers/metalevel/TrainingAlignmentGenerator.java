package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel;

import com.googlecode.cqengine.query.QueryFactory;
import de.uni_mannheim.informatik.dws.melt.matching_base.IMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_base.IMatcherCaller;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AlignmentAndParameters;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.GenericMatcherCaller;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.ontology.OntModel;

/**
 * This matcher assumes that the input alignment is a kind of reference alignment.
 * After applying the recallMatcher given in the constructor, a new alignment is returned which
 * contains positive (equivalence relation) and negative(incompat relation) correspondences.
 * With the help of this alignment, supervised matchers can be trained. 
 */
public class TrainingAlignmentGenerator implements IMatcherCaller, IMatcher<OntModel, Alignment, Properties>{
    
    private final Object recallMatcher;
    
    public TrainingAlignmentGenerator(Object recallMatcher){
        this.recallMatcher = recallMatcher;
    }
    
    @Override
    public AlignmentAndParameters match(Set<Object> sourceRespresentations, Set<Object> targetRespresentations, Object inputAlignment, Object parameters) throws Exception {
        AlignmentAndParameters r = GenericMatcherCaller.runMatcherMultipleRepresentations(this.recallMatcher, sourceRespresentations, targetRespresentations, null, parameters);
        
        Alignment recallAlignment = r.getAlignment(Alignment.class, TypeTransformerRegistry.getTransformedPropertiesOrNewInstance(parameters));        
        Alignment referenceAlignment = TypeTransformerRegistry.getTransformedObjectOrNewInstance(inputAlignment, Alignment.class, parameters);
        
        Alignment training = getTrainingAlignment(recallAlignment, referenceAlignment);
        
        return new AlignmentAndParameters(training, r.getParameters());
    }
    
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties parameters) throws Exception {
        AlignmentAndParameters r = GenericMatcherCaller.runMatcher(this.recallMatcher, source, target, null, parameters);
        Alignment recallAlignment = r.getAlignment(Alignment.class, TypeTransformerRegistry.getTransformedPropertiesOrNewInstance(parameters));         
        return getTrainingAlignment(recallAlignment, inputAlignment);
    }
    
    
    /**
     * This method returns a training alignment based on a recall alignment and a reference alignment.
     * The training alignment is generated by getting all correspondences in the recall alignment where at least one part is also contained in the reference alignment.
     * If the correspondences is directly in the reference alignment, then it is assumed to be a positive example.
     * If only one part of the correspondence is in the reference alignment, then is is assumed to be a negative correspondence.
     * The result contains the correspondences from the recall alignment, where positive examples have the equivalence relation and negative examples have INCOMPAT relation.
     * @param recallAlignment recall alignment
     * @param referenceAlignment reference alignment which does not need to really be the reference alignment of a track.
     * @return the correspondences from the recall alignment, where positive examples have the equivalence relation and negative examples have INCOMPAT relation
     */
    public static Alignment getTrainingAlignment(Alignment recallAlignment, Alignment referenceAlignment){
        
        //generate the training examples
        Iterable<Correspondence> alternatives = recallAlignment.retrieve(QueryFactory.or(
            QueryFactory.in(Correspondence.SOURCE, referenceAlignment.getDistinctSourcesAsSet()),
            QueryFactory.in(Correspondence.TARGET, referenceAlignment.getDistinctTargetsAsSet())
        ));
        
        Alignment trainingAlignment = new Alignment();
        for(Correspondence c : alternatives) {
            if(referenceAlignment.contains(c)) {
                trainingAlignment.add(
                        new Correspondence(c.getEntityOne(), c.getEntityTwo(), c.getConfidence(), CorrespondenceRelation.EQUIVALENCE, c.getExtensions())
                );
            } else {
                trainingAlignment.add(
                        new Correspondence(c.getEntityOne(), c.getEntityTwo(), c.getConfidence(), CorrespondenceRelation.INCOMPAT, c.getExtensions())
                );
            }
        }
        return trainingAlignment;
    }
}
