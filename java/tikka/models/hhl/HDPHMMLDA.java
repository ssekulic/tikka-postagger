///////////////////////////////////////////////////////////////////////////////
//  Copyright (C) 2010 Taesun Moon, The University of Texas at Austin
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 3 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
///////////////////////////////////////////////////////////////////////////////
package tikka.models.hhl;

import tikka.apps.CommandLineOptions;

import tikka.opennlp.io.DataFormatEnum;
import tikka.opennlp.io.DataReader;
import tikka.opennlp.io.DirReader;
import tikka.opennlp.io.DirWriter;

import tikka.structures.DoubleStringPair;
import tikka.structures.StringDoublePair;
import tikka.structures.distributions.AffixStateDP;
import tikka.structures.distributions.DirichletBaseDistribution;
import tikka.structures.distributions.HierarchicalDirichletBaseDistribution;
import tikka.structures.distributions.StemAffixStateDP;
import tikka.structures.distributions.StemAffixStateHDP;
import tikka.structures.distributions.StemAffixTopicDP;
import tikka.structures.distributions.StemAffixTopicHDP;
import tikka.structures.lexicons.Lexicon;

import tikka.utils.ec.util.MersenneTwisterFast;
import tikka.utils.normalizer.WordNormalizer;
import tikka.utils.normalizer.WordNormalizerToLowerNoNum;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import tikka.utils.math.SampleEval;

/**
 * This is a hybrid between the HDPHMMLDA model and the HMMLDA model. It assumes
 * that only words from the topic states have morphological structure. The model
 * posits a fixed number of topic states.
 * 
 * @author tsmoon
 */
public abstract class HDPHMMLDA {

    /**
     * Machine epsilon for comparing equality in floating point numbers.
     */
    protected static final double EPSILON = 1e-12;
    /**
     * Maximum length of certain arrays (e.g. word splitting)
     */
    protected static final int MAXLEN = 100;
    /**
     * Random number generator. Preferred over Java native Rand.
     */
    static protected MersenneTwisterFast mtfRand;
    /**
     * Seed for random number generator. Default is 0.
     */
    protected int randomSeed;
    /**
     * Number of topic types.
     */
    protected int topicK;
    /**
     * Number of iterations
     */
    protected int iterations;
    /**
     * Number of documents
     */
    protected int documentD;
    /**
     * Number of documents in the test set
     */
    protected int testDocumentD;
    /**
     * Number of word types
     */
    protected int wordW;
    /**
     * Number of word tokens
     */
    protected int wordN;
    /**
     * Number of types to printTabulatedProbabilities per class (topic and/or state)
     */
    protected int outputPerClass;
    /**
     * Hyperparameter for topic-by-document prior.
     */
    protected double alpha;
//    /**
//     * Normalization term for doc by topic table. Set to
//     * <pre>alpha*topicK</pre>
//     */
//    protected double talpha;
    /**
     * Hyperparameter for word/stem-by-topic prior
     */
    protected double beta;
    /**
     * Normalization term for word-by-topic multinomial
     */
    protected double wbeta;
    /**
     * Counts of word types
     */
    protected int[] wordCounts;
    /**
     * Counts of topics.
     */
    protected int[] topicCounts;
//    /**
//     * Count of documents. Only needed in sampling stages. Nonetheless, should
//     * be populated early on. Also, it includes a normalization term for the
//     * hyperparameter and is thus not a true count but a normalized count
//     */
//    protected double[] documentCounts;
    /**
     * Posterior probabilities for topics.
     */
    protected double[] topicProbs;
    /**
     * Counts of topics by document
     */
    protected int[] DocumentByTopic;
    /**
     * Array of counts for words given topics.
     * This has no effect on the model. It is merely here as a bookkeeping
     * device to check how the segmentation model is doing compared to
     * normalization as if the words had been dumped into their topics.
     */
    protected int[] TopicByWord;
    /**
     * Hashtable from word to index.
     */
    protected HashMap<String, Integer> wordIdx;
    /**
     * Hashtable from index to word
     */
    protected HashMap<Integer, String> idxToWord;
    /**
     * Array of document indexes. Of length {@link #wordN}.
     */
    protected int[] documentVector;
    /**
     * Array of topic indexes. Of length {@link #wordN}.
     */
    protected int[] topicVector;
    /**
     * Array of word indexes. Of length {@link #wordN}.
     */
    protected int[] wordVector;
    /**
     * The temperature setting for approximating MAP estimation.
     */
    protected static final double MAPTEMP = 0.1;
    /**
     * Temperature at which to start annealing process
     */
    protected double initialTemperature;
    /**
     * Decrement at which to reduce the temperature in annealing process
     */
    protected double temperatureDecrement;
    /**
     * Stop changing temperature after the following temp has been reached.
     */
    protected double targetTemperature;
    /**
     * The actual exponent in the process. Is the reciprocal of the temperature.
     */
    protected double temperatureReciprocal;
    /**
     * Current temperature for annealing.
     */
    protected double temperature;
    /**
     * Number of iterations per temperature increment. This is only used
     * when simulated annealing is implemented. It is identical to
     * {@link #iteration}.
     */
    protected int innerIterations;
    /**
     * Number of outer iterations per annealing scheme. This is only used
     * when simulated annealing is implemented. It is set to
     * <pre>
     * (targetTemperature-initialTemperature)/temperatureDecrement+1
     * </pre>
     */
    protected int outerIterations;
    /**
     * Number of iterations for test set burnin
     */
    protected int testSetBurninIterations;
    /**
     * Reader for each document
     */
    protected DataReader dataReader;
    /**
     * Reader for walking through training directories
     */
    protected DirReader dirReader;
    /**
     * Reader for walking through test directories
     */
    protected DirReader testDirReader = null;
    /**
     * Format of the input data
     */
    protected DataFormatEnum.DataFormat dataFormat;
    /**
     * Specifies how to normalize words
     */
    protected WordNormalizer wordNormalizer;
    /**
     * Hyperparameter for state emissions
     */
    protected double gamma;
    /**
     * Normalization term for state emissions (over words)
     */
    protected double wgamma;
    /**
     * Number of states including topic states and sentence boundary state
     */
    protected int stateS;
    /**
     * Offset for 3rd order state count array
     */
    protected int S3;
    /**
     * Offset for 2nd order state count array
     */
    protected int S2;
    /**
     * End of sentence marker index
     */
    protected final int EOSi = 0;
    /**
     * End of sentence marker
     */
    protected final String EOSw = ".";
    /**
     * 3rd order state counts
     */
    protected int[] thirdOrderTransitions;
    /**
     * 2nd order state counts
     */
    protected int[] secondOrderTransitions;
    /**
     * Array of counts per state
     */
    protected int[] stateCounts;
    /**
     * Token indexes for sentence boundaries; sentences begin here
     */
    protected int[] sentenceVector;
    /**
     * Array of states one word before in previous iteration
     */
    protected int[] first;
    /**
     * Array of states two words before in previous iteration
     */
    protected int[] second;
    /**
     * Array of states three words before in previous iteration
     */
    protected int[] third;
    /**
     * Array of states over tokens
     */
    protected int[] stateVector;
    /**
     * Array of counts for words given states.
     * This has no effect on the model. It is merely here as a bookkeeping
     * device to check how the segmentation model is doing compared to
     * normalization as if the words had been dumped into their states.
     */
    protected int[] StateByWord;
    /**
     * Probability of each state
     */
    protected double[] stateProbs;
    /**
     * Table of top {@link #outputPerClass} words per state. Used in
     * normalization and printing. Calculated from the bookkeeping table
     * of {@link #StateByWord}. Is not related to counts derived from
     * segmentations.
     */
    protected StringDoublePair[][] TopWordsPerStateFromRaw;
    /**
     * Table of top {@link #outputPerClass} words per topic. Used in
     * normalization and printing. Calculated from the bookkeeping table
     * of {@link #TopicByWord}. Is not related to counts derived from
     * segmentations.
     */
    protected StringDoublePair[][] TopWordsPerTopicFromRaw;
    /**
     * Table of top {@link #outputPerClass} words per state. Used in
     * normalization and printing.
     */
    protected StringDoublePair[][] TopWordsPerState;
    /**
     * Table of top {@link #outputPerClass} words per topic. Used in
     * normalization and printing.
     */
    protected StringDoublePair[][] TopWordsPerTopic;
    /**
     * Hierarchical dirichlet process structure for stems given affixes and
     * topics.
     */
    protected StemAffixTopicHDP stemAffixTopicHDP;
    /**
     * Hierarchical dirichlet process structure for stems given affixes and
     * topics.
     */
    protected StemAffixTopicDP stemAffixTopicDP;
    /**
     * Hierarchical dirichlet process structure for stems given affixes and
     * topics.
     */
    protected StemAffixStateHDP stemAffixStateHDP;
    /**
     * Dirichlet process structure for affixes given states
     */
    protected AffixStateDP affixStateDP;
    /**
     * Hierarchical dirichlet process structure for stems given affixes and
     * topics.
     */
    protected StemAffixStateDP stemAffixStateDP;
    /**
     * Number of topic states. This is a less than {@link #stateS} and entails
     * that the topic states are a subset of the full states. A count of one
     * must be added to whatever number is passed from {@link HybridHMMLDAOptions}
     * since state 0 is always the sentence boundary.
     */
    protected int topicSubStates;
    /**
     * OS neutral newline character
     */
    protected final static String newline = System.getProperty("line.separator");
    /**
     * Hyperparameter for state by affix by stem DP
     */
    protected double muStem;
    /**
     * Hyperparameter for state by affix by stem HDP base distribution
     */
    protected double muStemBase;
    /**
     * Hyperparameter for state by affix DP
     */
    protected double muAffix;
    /**
     * Hyperparameter for state by affix HDP base distribution
     */
    protected double muAffixBase;
    /**
     * Hyperparameter for topic by affix by stem DP
     */
    protected double betaStem;
    /**
     * Hyperparameter for topic by affix by stem HDP base distribution
     */
    protected double betaStemBase;
    /**
     * Hyperparameter for state transition prior. This overrides
     * {@link HMMLDA#gamma} to reflect notation in the paper. See
     * {@link HMMLDA#thirdOrderTransitions} and {@link HMMLDA#secondOrderTransitions}.
     */
    protected double psi;
    /**
     * Set to be {@link HMMLDA#stateS} * {@link #psi}. For use in
     * normalization of draws.
     */
    protected double spsi;
    /**
     * Hyperparameter for "switch" prior. See {@link  #switchVector}
     * and {@link #fourthOrderSwitches}.
     */
    protected double xi;
    /**
     * Set at {@link #xi} * {@link #switchQ}. For use in normalization of draws
     */
    protected double qxi;
    /**
     * Array of switch indexes.
     */
    protected int[] switchVector;
    /**
     * Array for counting switches given current state and three previous
     * states.
     */
    protected int[] fourthOrderSwitches;
    /**
     * Array of switch counts given state.
     */
    protected int[] SwitchByState;
    /**
     * Array of switch probabilities given state.
     */
    protected double[] SwitchByStateProbs;
    /**
     * Marginal probability of each switch. Used in normalization and printing
     * stage.
     */
    protected double[] switchProbs;
    /**
     * Counts of each switch.
     */
    protected int[] switchCounts;
    /**
     * The total number of switch types. There are only two. One which bears
     * topical content and one which does not.
     */
    protected final int switchQ = 2;
    /**
     * Prior probability of a morpheme boundary for affixes. Equivalent to
     * <pre>P(#)</pre> in the model.
     */
    protected double affixBoundaryProb;
    /**
     * Prior probability of a morpheme boundary for stems. Equivalent to
     * <pre>P(#)</pre> in the model.
     */
    protected double stemBoundaryProb;
    /**
     * Prior probability of a morpheme non-boundary for affixes. Equivalent to
     * <pre>1-P(#)</pre> in the model.
     */
    protected double notAffixBoundaryProb;
    /**
     * Prior probability of a morpheme non-boundary for stems. Equivalent to
     * <pre>1-P(#)</pre> in the model.
     */
    protected double notStemBoundaryProb;
    /**
     * Base distribution for the stems given affixes and states. Equivalent to
     * <pre>B_0(stem)</pre> in the model.
     */
    protected HierarchicalDirichletBaseDistribution stemStateHierarchicalBaseDistribution;
    /**
     * Base distribution for the stems given affixes and topics. Equivalent to
     * <pre>G_0(stem)</pre> in the model.
     */
    protected HierarchicalDirichletBaseDistribution stemTopicHierarchicalBaseDistribution;
    /**
     * Base distribution for the affixes given states. Equivalent to
     * <pre>G(affix)</pre> in the model.
     */
    protected DirichletBaseDistribution dirichletAffixBaseDistribution;
    /**
     * Base distribution for the stems given states. Equivalent to
     * <pre>G_0(affix)</pre> in the model.
     */
    protected DirichletBaseDistribution dirichletStemBaseDistribution;
    /**
     * Array of affix indexes.
     */
    protected int[] affixVector;
    /**
     * Array of stem indexes
     */
    protected int[] stemVector;
    /**
     * Array of where each token was segmented. For use in reconstruction
     * in the serializableModel.
     */
    protected int[] splitVector;
    /**
     * Lexicon for the stems. Keeps track of the strings and their indexes.
     */
    protected Lexicon stemLexicon;
    /**
     * Lexicon for the affixes. Keeps track of the strings and their indexes.
     */
    protected Lexicon affixLexicon;
    /**
     * Hashmap from index to stems. To be shared across all stem related
     * structures.
     */
    protected HashMap<String, Integer> StemToIdx;
    /**
     * Hashmap from index to affixes. To be shared across all affix related
     * structures.
     */
    protected HashMap<String, Integer> AffixToIdx;
    /**
     * Path of training data.
     */
    protected String rootDir;
    /**
     * Path of test data.
     */
    protected String testRootDir = null;
    /**
     * Type of model that is being run.
     */
    protected String modelName;
    /**
     * Number of samples to take
     */
    protected int samples;
    /**
     * Number of iterations between samples
     */
    protected int lag;
    /**
     * Probability table of tokens per sample.
     */
    protected double[] SampleProbs;
    /**
     * String for maintaining all model parameters. Only for printing purposes.
     */
    protected StringBuilder modelParameterStringBuilder;

    /**
     * Default constructor.
     * 
     * @param options   Options from the command line.
     */
    public HDPHMMLDA(CommandLineOptions options) throws IOException {
        /**
         * Setting input data
         */
        dataFormat = options.getDataFormat();
        rootDir = options.getDataDir();
        dirReader = new DirReader(rootDir, dataFormat);
        testRootDir = options.getTestDataDir();
        if (testRootDir != null) {
            testDirReader = new DirReader(testRootDir, dataFormat);
        } else {
            testRootDir = "";
        }

        /**
         * Setting lexicons
         */
        wordIdx = new HashMap<String, Integer>();
        idxToWord = new HashMap<Integer, String>();
        wordIdx.put(EOSw, EOSi);
        idxToWord.put(EOSi, EOSw);

        StemToIdx = new HashMap<String, Integer>();
        AffixToIdx = new HashMap<String, Integer>();
        stemLexicon = new Lexicon(StemToIdx);
        affixLexicon = new Lexicon(AffixToIdx);

        /**
         * Setting dimensions
         */
        topicK = options.getTopics();
        stateS = options.getStates();
        topicSubStates = options.getTopicSubStates() + 1;
        documentD = 0;
        outputPerClass = options.getOutputPerClass();
        S3 = stateS * stateS * stateS;
        S2 = stateS * stateS;

        /**
         * Setting iterations and temperatures
         */
        iterations = options.getNumIterations();
        initialTemperature = options.getInitialTemperature();
        temperature = initialTemperature;
        temperatureReciprocal = 1 / temperature;
        temperatureDecrement = options.getTemperatureDecrement();
        targetTemperature = options.getTargetTemperature();
        innerIterations = iterations;
        outerIterations =
              (int) Math.round((initialTemperature - targetTemperature)
              / temperatureDecrement) + 1;
        samples = options.getSamples();
        lag = options.getLag();
        testSetBurninIterations = options.getTestSetBurninIterations();

        /**
         * Setting hyperparameters
         */
        alpha = options.getAlpha();
//        talpha = alpha * topicK;
        beta = options.getBeta();
        gamma = options.getGamma();
        muStem = options.getMuStem();
        muAffix = options.getMuAffix();
        betaStem = options.getBetaStem();
        muStemBase = options.getMuStemBase();
        muAffixBase = options.getMuAffixBase();
        betaStemBase = options.getBetaStemBase();
        psi = options.getPsi();
        spsi = stateS * psi;
        xi = options.getXi();
        qxi = xi * switchQ;

        /**
         * Initializing random number generator, etc.
         */
        randomSeed = options.getRandomSeed();
        if (randomSeed == -1) {
            randomSeed = 0;
        }
        mtfRand = new MersenneTwisterFast(randomSeed);
        wordNormalizer = new WordNormalizerToLowerNoNum();

        stemBoundaryProb = options.getStemBoundaryProb();
        notStemBoundaryProb = 1 - stemBoundaryProb;
        affixBoundaryProb = options.getAffixBoundaryProb();
        notAffixBoundaryProb = 1 - affixBoundaryProb;

        modelName = options.getExperimentModel();
    }

    /**
     * Constructor used when model is loaded from a previous training session.
     */
    public HDPHMMLDA() {
    }

    /**
     * Randomly assign topics to words, documents and states.
     */
    public void initialize() {

        switchCounts = new int[switchQ];
        for (int i = 0; i < switchQ; ++i) {
            switchCounts[i] = 0;
        }
        switchProbs = new double[switchQ];

        fourthOrderSwitches =
              new int[stateS * stateS * stateS * stateS * stateS * switchQ];

        try {
            for (int i = 0;;
                  ++i) {
                fourthOrderSwitches[i] = 0;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        }

        stateCounts = new int[stateS];
        thirdOrderTransitions = new int[stateS * stateS * stateS * stateS];
        secondOrderTransitions = new int[stateS * stateS * stateS];
        try {
            for (int i = 0;; ++i) {
                thirdOrderTransitions[i] = 0;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        }
        try {
            for (int i = 0;; ++i) {
                secondOrderTransitions[i] = 0;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        }

        ArrayList<Integer> wordVectorT = new ArrayList<Integer>(),
              documentVectorT = new ArrayList<Integer>(),
              sentenceVectorT = new ArrayList<Integer>();
        while ((dataReader = dirReader.nextDocumentReader()) != null) {
            try {
                String[][] sentence;
                while ((sentence = dataReader.nextSequence()) != null) {
                    for (String[] line : sentence) {
                        String word = wordNormalizer.normalize(line)[0];
                        if (!word.isEmpty()) {
                            if (!wordIdx.containsKey(word)) {
                                wordIdx.put(word, wordIdx.size());
                                idxToWord.put(idxToWord.size(), word);
                            }
                            wordVectorT.add(wordIdx.get(word));
                            documentVectorT.add(documentD);
                        }
                    }
                    wordVectorT.add(EOSi);
                    documentVectorT.add(documentD);
                    sentenceVectorT.add(wordVectorT.size());
                }
            } catch (IOException e) {
            }
            documentD++;
        }

        wordN = wordVectorT.size();
        wordW = wordIdx.size();
        wbeta = beta * wordW;
        wgamma = gamma * wordW;

        wordVector = new int[wordN];
        documentVector = new int[wordN];

        stemVector = new int[wordN];
        affixVector = new int[wordN];
        splitVector = new int[wordN];
        switchVector = new int[wordN];

        first = new int[wordN];
        second = new int[wordN];
        third = new int[wordN];

        stateVector = new int[wordN];
        topicVector = new int[wordN];
        sentenceVector = new int[sentenceVectorT.size()];

        copyToArray(wordVector, wordVectorT);
        copyToArray(documentVector, documentVectorT);
        copyToArray(sentenceVector, sentenceVectorT);

        topicCounts = new int[topicK];
        topicProbs = new double[topicK];
        for (int i = 0; i < topicK; ++i) {
            topicCounts[i] = 0;
            topicProbs[i] = 0.;
        }
        TopicByWord = new int[topicK * wordW];
        try {
            for (int i = 0;; ++i) {
                TopicByWord[i] = 0;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        }

//        documentCounts = new double[documentD];
//        try {
//            for (int i = 0;; ++i) {
//                documentCounts[i] = talpha;
//            }
//        } catch (ArrayIndexOutOfBoundsException e) {
//        }
//        try {
//            for (int i = 0;; ++i) {
//                documentCounts[documentVector[i]]++;
//            }
//        } catch (ArrayIndexOutOfBoundsException e) {
//        }
        DocumentByTopic = new int[documentD * topicK];
        try {
            for (int i = 0;; ++i) {
                DocumentByTopic[i] = 0;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        }

        stateCounts = new int[stateS];
        stateProbs = new double[stateS];
        for (int i = 0; i < stateS; ++i) {
            stateCounts[i] = 0;
            stateProbs[i] = 0;
        }

        StateByWord = new int[stateS * wordW];
        try {
            for (int i = 0;; ++i) {
                StateByWord[i] = 0;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        }

        SwitchByState = new int[topicSubStates * switchQ];
        SwitchByStateProbs = new double[topicSubStates * switchQ];
        try {
            for (int i = 0;; ++i) {
                SwitchByState[i] = 0;
                SwitchByStateProbs[i] = 0;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        }

        SampleProbs = new double[samples * wordN];
        try {
            for (int i = 0;; ++i) {
                SampleProbs[i] = 0;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        initalizeDistributions();
    }

    /**
     * Initializes from a pretrained, loaded model. Use this if the model has
     * been loaded from a pretrained model.
     */
    public void initializeFromLoadedModel(CommandLineOptions options) {
        /**
         * Initialize random number generator
         */
        if (options.getRandomSeed() != -1) {
            randomSeed = options.getRandomSeed();
        }
        mtfRand = new MersenneTwisterFast(randomSeed);

        /**
         * Revive dirReader
         */
        try {
            dirReader = new DirReader(rootDir, dataFormat);
        } catch (IOException e) {
        }

        /**
         * Revive some constants that will be used often
         */
//        talpha = alpha * topicK;
        wbeta = beta * wordW;
        wgamma = gamma * wordW;
        spsi = stateS * psi;
        qxi = xi * switchQ;
        S3 = stateS * stateS * stateS;
        S2 = stateS * stateS;

        /**
         * Revive the annealing regime
         */
        temperature = initialTemperature;
        temperatureReciprocal = 1 / temperature;
        innerIterations = iterations;
        outerIterations =
              (int) Math.round(
              (initialTemperature - targetTemperature) / temperatureDecrement) + 1;

        /**
         * These are not saved in the model so must be revived
         */
        stemVector = new int[wordN];
        affixVector = new int[wordN];

        first = new int[wordN];
        second = new int[wordN];
        third = new int[wordN];

        idxToWord = new HashMap<Integer, String>();
        for (String word : wordIdx.keySet()) {
            idxToWord.put(wordIdx.get(word), word);
        }

        StemToIdx = new HashMap<String, Integer>();
        AffixToIdx = new HashMap<String, Integer>();
        stemLexicon = new Lexicon(StemToIdx);
        affixLexicon = new Lexicon(AffixToIdx);

        topicCounts = new int[topicK];
        topicProbs = new double[topicK];
        for (int i = 0; i < topicK; ++i) {
            topicCounts[i] = 0;
            topicProbs[i] = 0.;
        }

        TopicByWord = new int[topicK * wordW];
        try {
            for (int i = 0;; ++i) {
                TopicByWord[i] = 0;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        }

        DocumentByTopic = new int[documentD * topicK];
        try {
            for (int i = 0;; ++i) {
                DocumentByTopic[i] = 0;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        }

        stateCounts = new int[stateS];
        stateProbs = new double[stateS];
        for (int i = 0; i < stateS; ++i) {
            stateCounts[i] = 0;
            stateProbs[i] = 0;
        }

        StateByWord = new int[stateS * wordW];
        try {
            for (int i = 0;; ++i) {
                StateByWord[i] = 0;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        }

        thirdOrderTransitions = new int[stateS * stateS * stateS * stateS];
        secondOrderTransitions = new int[stateS * stateS * stateS];
        try {
            for (int i = 0;; ++i) {
                thirdOrderTransitions[i] = 0;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        }
        try {
            for (int i = 0;; ++i) {
                secondOrderTransitions[i] = 0;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        }
    }

    /**
     * Each derived class uses a different set of DP and HDP combinations for
     * training. This should be specified for each model.
     */
    protected abstract void initalizeDistributions();

    /**
     * Train the model.
     */
    public void train() {
        randomInitializeParameters();

        /**
         * Training iterations
         */
        for (int outiter = 0; outiter < outerIterations;
              ++outiter) {
            System.err.print("\nouter iteration " + outiter + ":");
            System.err.print("annealing temperature " + temperature);
            stabilizeTemperature();
            trainInnerIter(innerIterations, "inner iteration");
            temperature -= temperatureDecrement;
            temperatureReciprocal = 1 / temperature;
        }
    }

    /**
     * Normalize a sample.
     */
    public void normalize() {
        affixStateDP.normalize(topicSubStates, stateS, outputPerClass,
              stateProbs);
        stemAffixTopicHDP.normalize(topicK, outputPerClass, affixStateDP,
              affixLexicon);

        normalizeWords();
        normalizeRawTopics();
        normalizeRawStates();
    }

    /**
     * Normalize the sample counts for words over topics and states by summing over possible
     * segmentations. The parameters for the segmentation were learned during 
     * the training stage.
     */
    protected abstract void normalizeWords();

    /**
     * Normalize the sample counts for words given topic.  This is NOT
     * based on the segmented word probabilities. It is only from the raw word
     * counts which had been assigned based on the segmentation based sampling.
     */
    protected void normalizeRawTopics() {
        TopWordsPerTopicFromRaw = new StringDoublePair[topicK][];
        for (int i = 0; i < topicK; ++i) {
            TopWordsPerTopicFromRaw[i] = new StringDoublePair[outputPerClass];
        }

        Double sum = 0.;
        for (int i = 0; i < topicK; ++i) {
            sum += topicProbs[i] = topicCounts[i] + wbeta;
            ArrayList<DoubleStringPair> topWords =
                  new ArrayList<DoubleStringPair>();
            /**
             * Start at one to leave out EOSi
             */
            for (int j = EOSi + 1; j < wordW; ++j) {
                topWords.add(new DoubleStringPair(
                      TopicByWord[j * topicK + i] + beta, idxToWord.get(
                      j)));
            }
            Collections.sort(topWords);
            for (int j = 0; j < outputPerClass; ++j) {
                TopWordsPerTopicFromRaw[i][j] = new StringDoublePair(
                      topWords.get(j).stringValue, topWords.get(j).doubleValue
                      / topicProbs[i]);
            }
        }

        for (int i = 0; i < topicK; ++i) {
            topicProbs[i] /= sum;
        }
    }

    /**
     * Normalize the sample counts for words given state.  This is NOT
     * based on the segmented word probabilities. It is only from the raw word
     * counts which had been assigned based on the segmentation based sampling.
     */
    protected void normalizeRawStates() {
        TopWordsPerStateFromRaw = new StringDoublePair[stateS][];
        for (int i = topicSubStates; i < stateS; ++i) {
            TopWordsPerStateFromRaw[i] = new StringDoublePair[outputPerClass];
        }

        double sum = 0.;
        for (int i = topicSubStates; i < stateS; ++i) {
            sum += stateProbs[i] = stateCounts[i] + wbeta;
            ArrayList<DoubleStringPair> topWords =
                  new ArrayList<DoubleStringPair>();
            /**
             * Start at one to leave out EOSi
             */
            for (int j = EOSi + 1; j < wordW; ++j) {
                topWords.add(new DoubleStringPair(
                      StateByWord[j * stateS + i] + beta, idxToWord.get(
                      j)));
            }
            Collections.sort(topWords);
            for (int j = 0; j < outputPerClass; ++j) {
                TopWordsPerStateFromRaw[i][j] =
                      new StringDoublePair(
                      topWords.get(j).stringValue,
                      topWords.get(j).doubleValue / stateProbs[i]);
            }
        }

        for (int i = 1; i < stateS; ++i) {
            stateProbs[i] /= sum;
        }
    }

    /**
     * Print normalized probabilities for each category to out. Print only
     * the top {@link #outputPerClass} per category.
     *
     * @param out   Buffer to write to.
     * @throws IOException
     */
    public void printTabulatedProbabilities(BufferedWriter out) throws
          IOException {
        printTopics(out);
        printNewlines(out, 4);
        printStates(out);
        printNewlines(out, 4);
        affixStateDP.print(topicSubStates, stateS, outputPerClass, stateProbs,
              out);
        printNewlines(out, 4);
        stemAffixTopicHDP.print(topicK, outputPerClass, topicProbs, out);
        printNewlines(out, 4);
        printTopicsRaw(out);
        printNewlines(out, 4);
        printStatesRaw(out);
        out.close();

    }

    /**
     * Print the normalized sample counts for each topic to out. This is NOT
     * based on the segmented word probabilities. It is only from the raw word
     * counts which had been assigned based on the segmentation based sampling.
     * 
     * @param out
     * @throws IOException
     */
    protected void printTopicsRaw(BufferedWriter out) throws IOException {
        int startt = 0, M = 4, endt = M;
        out.write("***** Unsegmented Word Probabilities by Topic *****\n\n");
        while (startt < topicK) {
            for (int i = startt; i < endt; ++i) {
                String header = "Topic_" + i;
                header = String.format("%25s\t%6.5f\t", header, topicProbs[i]);
                out.write(header);
            }

            out.newLine();
            out.newLine();

            for (int i = 0; i < outputPerClass; ++i) {
                for (int c = startt; c < endt; ++c) {
                    String line = String.format("%25s\t%6.5f\t",
                          TopWordsPerTopicFromRaw[c][i].stringValue,
                          TopWordsPerTopicFromRaw[c][i].doubleValue);
                    out.write(line);
                }
                out.newLine();
            }
            out.newLine();
            out.newLine();

            startt = endt;
            endt = java.lang.Math.min(topicK, startt + M);
        }
    }

    /**
     * Print the normalized sample counts for each state to out. This is NOT
     * based on the segmented word probabilities. It is only from the raw word
     * counts which had been assigned based on the segmentation based sampling.
     *
     * @param out
     * @throws IOException
     */
    protected void printStatesRaw(BufferedWriter out) throws IOException {
        int startt = topicSubStates, M = 4, endt = M + topicSubStates; // careful  here!!!!!!!!!!!!!!!!
        out.write("***** Unsegmented Word Probabilities by State *****\n\n");
        while (startt < stateS) {
            for (int i = startt; i < endt; ++i) {
                String header = "State_" + i;
                header = String.format("%25s\t%6.5f\t", header, stateProbs[i]);
                out.write(header);
            }

            out.newLine();
            out.newLine();

            for (int i = 0; i < outputPerClass; ++i) {
                for (int c = startt; c < endt; ++c) {
                    String line = String.format("%25s\t%6.5f\t",
                          TopWordsPerStateFromRaw[c][i].stringValue,
                          TopWordsPerStateFromRaw[c][i].doubleValue);
                    out.write(line);
                }
                out.newLine();
            }
            out.newLine();
            out.newLine();

            startt = endt;
            endt = java.lang.Math.min(stateS, startt + M);
        }
    }

    /**
     * Print the normalized sample counts for each topic to out. Print only the top {@link
     * #outputPerTopic} per given topic.
     *
     * @param out
     * @throws IOException
     */
    protected void printTopics(BufferedWriter out) throws IOException {
        int startt = 0, M = 4, endt = M;
        out.write("***** Word Probabilities by Topic *****\n\n");
        while (startt < topicK) {
            for (int i = startt; i < endt; ++i) {
                String header = "Topic_" + i;
                header = String.format("%25s\t%6.5f\t", header, topicProbs[i]);
                out.write(header);
            }

            out.newLine();
            out.newLine();

            for (int i = 0; i < outputPerClass; ++i) {
                for (int c = startt; c < endt; ++c) {
                    String line = String.format("%25s\t%6.5f\t",
                          TopWordsPerTopic[c][i].stringValue,
                          TopWordsPerTopic[c][i].doubleValue);
                    out.write(line);
                }
                out.newLine();
            }
            out.newLine();
            out.newLine();

            startt = endt;
            endt = java.lang.Math.min(topicK, startt + M);
        }
    }

    /**
     * Print the normalized sample counts for each state to out. Print only the top {@link
     * #outputPerTopic} per given state.
     *
     * @param out
     * @throws IOException
     */
    protected void printStates(BufferedWriter out) throws IOException {
        int startt = topicSubStates, M = 4, endt = M + topicSubStates;
        out.write("***** Word Probabilities by State *****\n\n");
        while (startt < stateS) {
            for (int i = startt; i < endt; ++i) {
                String header = "State_" + i;
                header = String.format("%25s\t%6.5f\t", header, stateProbs[i]);
                out.write(header);
            }

            out.newLine();
            out.newLine();

            for (int i = 0; i < outputPerClass; ++i) {
                for (int c = startt; c < endt; ++c) {
                    String line = String.format("%25s\t%6.5f\t",
                          TopWordsPerState[c][i].stringValue,
                          TopWordsPerState[c][i].doubleValue);
                    out.write(line);
                }
                out.newLine();
            }
            out.newLine();
            out.newLine();

            startt = endt;
            endt = java.lang.Math.min(stateS, startt + M);
        }
    }

    /**
     * Print text that has been segmented/tagged in a sample to output.
     * 
     * @param outDir Root of path to generate output to
     * @throws IOException
     */
    public void printAnnotatedText(String outDir) throws IOException {
        DirWriter dirWriter = new DirWriter(outDir, rootDir, dirReader);
        String root = dirWriter.getRoot();

        BufferedWriter bufferedWriter;

        int docid = 0;
        bufferedWriter = dirWriter.nextOutputBuffer();

        for (int i = 0; i < wordN; ++i) {
            if (docid != documentVector[i]) {
                bufferedWriter.close();
                bufferedWriter = dirWriter.nextOutputBuffer();
                docid = documentVector[i];
            }
            int wordid = wordVector[i];
            if (wordid != EOSi) {
                String stem = stemLexicon.getString(stemVector[i]);
                String affix = affixLexicon.getString(affixVector[i]);
                bufferedWriter.write(stem + "+" + affix);
                bufferedWriter.write("\t");
                int stateid = stateVector[i];
                int topicid = topicVector[i];
                String line = null;
                if (stateid < topicSubStates) {
                    line = String.format("S:%d,T:%d", stateid, topicid);
                } else {
                    line = String.format("S:%d,T:%d", stateid, -1);
                }
                bufferedWriter.write(line);
            }
            bufferedWriter.newLine();
        }
        bufferedWriter.close();

        bufferedWriter = new BufferedWriter(new OutputStreamWriter(
              new FileOutputStream(root + File.separator + "PARAMETERS")));

        bufferedWriter.write(modelParameterStringBuilder.toString());
        bufferedWriter.close();
    }

    /**
     * Creates a string stating the parameters used in the model. The
     * string is used for pretty printing purposes and clarity in other
     * output routines.
     */
    public void setModelParameterStringBuilder() {
        modelParameterStringBuilder = new StringBuilder();
        String line = null;
        line = String.format("stateS:%d", stateS) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("topicSubStates:%d", topicSubStates) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("topicK:%d", topicK) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("documentD:%d", documentD) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("wordW:%d", wordW) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("wordN:%d", wordN) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("alpha:%f", alpha) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("beta:%f", beta) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("wbeta:%f", wbeta) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("gamma:%f", gamma) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("wgamma:%f", wgamma) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("psi:%f", psi) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("spsi:%f", spsi) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("xi:%f", xi) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("qxi:%f", qxi) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("muStem:%f", muStem) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("muStemBase:%f", muStemBase) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("muAffix:%f", muAffix) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("muAffixBase:%f", muAffixBase) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("betaStem:%f", betaStem) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("betaStemBase:%f", betaStemBase) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("initialTemperature:%f", initialTemperature) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("temperatureDecrement:%f", temperatureDecrement) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("targetTemperature:%f", targetTemperature) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("iterations:%d", iterations) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("randomSeed:%d", randomSeed) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("rootDir:%s", rootDir) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("testRootDir:%s", testRootDir) + newline;
        modelParameterStringBuilder.append(line);
    }

    /**
     * Generates output for use in generating graphs and tables. Will generate
     * output in terms of matlab matrices and latex tables. Has no header
     * 
     * @param outputPath Destination of output
     * @throws IOException
     */
    public void printSampleScoreData(BufferedWriter out, SampleEval sampleEval) throws IOException {
        printSampleScoreData(out, sampleEval, "");
    }

    /**
     * Generates output for use in generating graphs and tables. Will generate
     * output in terms of matlab matrices and latex tables.
     *
     * @param outputPath Destination of output
     * @param header Header of output indicating what kind of output it is
     * @throws IOException
     */
    public void printSampleScoreData(BufferedWriter out, SampleEval sampleEval, String header) throws
          IOException {
        double[] logsum = new double[samples];
        for (int i = 0; i < samples; ++i) {
            logsum[i] = 0;
        }
        for (int i = 0; i < samples; ++i) {
            logsum[i] += Math.log(SampleProbs[i]);
        }

        out.write(String.format("%% ***** %s *****", header + newline));
        out.write(modelParameterStringBuilder.toString());
        out.newLine();
        out.write("%% For use in matlab" + newline);
        out.write("%% log probabilities of each sample in row vector format" + newline);
        String nums = "[";
        for (int i = 0; i < samples; ++i) {
            nums += String.format("%.8f ", logsum[i]);
        }
        nums += "]" + newline + newline;
        out.write(nums);

        out.write("%% For use in latex" + newline);
        out.write("%% probabilities of each sample in row vector format" + newline);
        nums = "";
        double average = sampleEval.average(logsum);
        nums = String.format("%.2f ", average);
        for (int i = 0; i < samples; ++i) {
            nums += String.format("& %.2f ", Math.exp(logsum[i]));
        }
        out.write(nums + newline);
        out.close();
    }

    /**
     * Copy a sequence of numbers from @ta to array @ia.
     *
     * @param <T>   Any number type
     * @param ia    Target array of integers to be copied to
     * @param ta    Source List<T> of numbers to be copied from
     */
    protected static <T extends Number> void copyToArray(int[] ia, List<T> ta) {
        for (int i = 0; i < ta.size(); ++i) {
            ia[i] = ta.get(i).intValue();
        }
    }

    /**
     * The temperature changes in floating point increments. There is a later
     * need to check whether the temperature is equal to one or not during
     * the training process. If the temperature is close enough to one,
     * this will set the temperature to one.
     *
     * @return Whether temperature has been set to one
     */
    protected boolean stabilizeTemperature() {
        if (Math.abs(temperatureReciprocal - 1) < EPSILON) {
            temperatureReciprocal = 1;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Anneal an array of probabilities. For use when every array is
     * meaningfully populated. Discards with bounds checking.
     *
     * @param classes   Array of probabilities
     * @return  Sum of annealed probabilities. Is not 1.
     */
    protected double annealProbs(double[] classes) {
        return annealProbs(0, classes);
    }

    /**
     * Anneal an array of probabilities. For use when every array from starti
     * is meaningfully populated. Discards with bounds checking.
     *
     * @param starti    Index of first element
     * @param classes   Array of probabilities
     * @return  Sum of annealed probabilities. Is not 1.
     */
    protected double annealProbs(int starti, double[] classes) {
        double sum = 0, sumw = 0;
        try {
            for (int i = starti;; ++i) {
                sum += classes[i];
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        if (temperatureReciprocal != 1) {
            try {
                for (int i = starti;; ++i) {
                    classes[i] /= sum;
                    sumw += classes[i] = Math.pow(classes[i],
                          temperatureReciprocal);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
            }
        } else {
            sumw = sum;
        }
        try {
            for (int i = starti;; ++i) {
                classes[i] /= sumw;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        /**
         * For now, we set everything so that it sums to one.
         */
        return 1;
    }

    /**
     * Anneal an array of probabilities.
     *
     * @param classes   Array of probabilities
     * @param len   Length of array
     * @return  Sum of annealed probabilities. Is not 1.
     */
    protected double annealProbs(double[] classes, int len) {
        double sum = 0, sumw = 0;
        for (int i = 0; i < len; ++i) {
            sum += classes[i];
        }
        if (temperatureReciprocal != 1) {
            for (int i = 0; i < len; ++i) {
                classes[i] /= sum;
                sumw += classes[i] = Math.pow(classes[i], temperatureReciprocal);
            }
        } else {
            sumw = sum;
        }
        for (int i = 0; i < len; ++i) {
            classes[i] /= sumw;
        }
        /**
         * For now, we set everything so that it sums to one.
         */
        return 1;
    }

    /**
     * Prints empty newlines in output. For pretty printing purposes.
     *
     * @param out   Destination of output
     * @param n     Number of new lines to create in output
     * @throws IOException
     */
    protected void printNewlines(BufferedWriter out, int n) throws IOException {
        for (int i = 0; i < n; ++i) {
            out.newLine();
        }
    }

    /**
     * Tag, annotate and split data in a test directory.
     * 
     * @param testDataDir
     * @throws IOException
     */
    public abstract void tagTestText() throws IOException;

    public void printAnnotatedTestText(String annotatedTestTextDir) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Randomly set the model parameters for use in training
     */
    protected abstract void randomInitializeParameters();

    /**
     * Training routine for the inner iterations
     *
     * @param itermax Maximum number of iterations to perform
     * @param message Message to generate
     */
    protected abstract void trainInnerIter(int itermax, String message);

    /**
     * Method for setting probability of tokens per sample.
     * 
     * @param outiter Number of sample run
     */
    protected abstract void obtainSample(int outiter);

    /**
     * Sample model output. Take {@link #sample} samples ever {@link #lag}
     * iterations.
     */
    public void sampleFromTrain() {
        /**
         * Sampling iterations
         */
        for (int outiter = 0; outiter < samples; ++outiter) {
            System.err.print("\nTaking sample #" + outiter + ": ");
            System.err.print("annealing temperature " + temperature);
            trainInnerIter(lag, "Lag");
            obtainSample(outiter);
        }
    }

    public void sampleFromTest() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
