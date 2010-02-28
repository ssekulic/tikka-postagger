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
package tikka.bhmm.model.base;

import tikka.bhmm.apps.CommandLineOptions;
import tikka.opennlp.io.*;
import tikka.structures.*;
import tikka.utils.ec.util.MersenneTwisterFast;
import tikka.utils.annealer.*;
import tikka.utils.normalizer.*;
import tikka.utils.postags.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * The "barely hidden markov model" or "bicameral hidden markov model"
 *
 * @author tsmoon
 */
public abstract class BHMM {

    /**
     * OS neutral newline character
     */
    protected final static String newline = System.getProperty("line.separator");
    /**
     * Random number generator. Preferred over Java native Rand.
     */
    static protected MersenneTwisterFast mtfRand;
    /**
     * Seed for random number generator. Default is 0.
     */
    protected int randomSeed;
    /**
     * Hyperparameter for content-state-by-sentence prior.
     */
    protected double alpha;
    /**
     * Normalization term for content-state-by-sentence multinomial
     */
    protected double calpha;
    /**
     * Hyperparameter for word-by-content-state prior
     */
    protected double beta;
    /**
     * Normalization term for word-by-content-state multinomial
     */
    protected double wbeta;
    /**
     * Hyperparameter for word-by-function-state prior
     */
    protected double delta;
    /**
     * Normalization term for word-by-function-state multinomial
     */
    protected double wdelta;
    /**
     * Hyperparameter for state transition prior
     */
    protected double gamma;
    /**
     * Normalization term for state transition multinomial
     */
    protected double sgamma;
    /**
     * Number of sentences
     */
    protected int sentenceS;
    /**
     * Number of documents
     */
    protected int documentD;
    /**
     * End of sentence marker index
     */
    protected final int EOSi = 0;
    /**
     * End of sentence marker
     */
    protected final String EOSw = ".";
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
     * Array of word indexes. Of length {@link #wordN}.
     */
    protected int[] wordVector;
    /**
     * Array of sentence indexes. Of length {@link #wordN}.
     */
    protected int[] sentenceVector;
    /**
     * Array of document indexes
     */
    protected int[] documentVector;
    /**
     * Number of content states. It also includes the initial start state
     * as an offset.
     */
    protected int stateC;
    /**
     * Number of function states
     */
    protected int stateF;
    /**
     * Sum of states that combines the start state (0), the content states and
     * the function states
     */
    protected int stateS;
    /**
     * Offset for 3rd, 2nd, and 1st order state count array
     */
    protected int S3, S2, S1;
    /**
     * 3rd order state counts
     */
    protected int[] thirdOrderTransitions;
    /**
     * 2nd order state counts
     */
    protected int[] secondOrderTransitions;
    /**
     * 1st order state counts
     */
    protected int[] firstOrderTransitions;
    /**
     * Array of counts per state
     */
    protected int[] stateCounts;
    /**
     * Array of counts per sentence
     */
    protected int[] sentenceCounts;
    /**
     * Array of counts per document
     */
    protected int[] documentCounts;
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
     * Array of full gold tags
     */
    protected int[] goldTagVector;
//    /**
//     * Array of reduced gold tags
//     */
//    protected int[] goldReducedTagVector;
//    /**
//     * Hashtable from gold tag to index for evaluation
//     */
//    HashMap<String, Integer> goldTagToIdx;
//    /**
//     * Hashtable from index to gold tag
//     */
//    HashMap<Integer, String> idxToGoldTag;
    /**
     * Array of counts for words given content states.
     */
    protected int[] contentStateByWord;
    /**
     * Array of counts for words given function states.
     */
    protected int[] functionStateByWord;
    /**
     * Array of counts for words given all states
     */
    protected int[] stateByWord;
    /**
     * Array of counts for content states given sentence
     */
    protected int[] contentStateBySentence;
    /**
     * Array of function states over documents
     */
    protected int[] functionStateByDocument;
    /**
     * Probability of each state
     */
    protected double[] stateProbs;
    /**
     * Table of top {@link #outputPerClass} words per state. Used in
     * normalization and printing.
     */
    protected StringDoublePair[][] topWordsPerState;
    /**
     * Hashtable from word to index for training data.
     */
    protected HashMap<String, Integer> trainWordIdx;
    /**
     * Hashtable from index to word for training data.
     */
    protected HashMap<Integer, String> trainIdxToWord;
    /**
     * Hashtable from word to index for training data.
     */
    protected HashMap<String, Integer> testWordIdx;
    /**
     * Hashtable from index to word for training data.
     */
    protected HashMap<Integer, String> testIdxToWord;
    /**
     * Path of training data.
     */
    protected String trainDataDir;
    /**
     * Path of test data.
     */
    protected String testDataDir;
    /**
     * Reader for each document
     */
    protected DataReader dataReader;
    /**
     * Reader for walking through training directories
     */
    protected DirReader trainDirReader;
    /**
     * Reader for walking through test directories
     */
    protected DirReader testDirReader;
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
     * Number of iterations
     */
    protected int iterations;
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
     * Format of the input data
     */
    protected DataFormatEnum.DataFormat dataFormat;
    /**
     * Specifies how to normalize words
     */
    protected WordNormalizer wordNormalizer;
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
    protected double[] sampleProbs;
    /**
     * String for maintaining all model parameters. Only for printing purposes.
     */
    protected StringBuilder modelParameterStringBuilder;
    /**
     * Object that handles the both model and gold tag sets. It also finds
     * the best mapping from one to the other.
     */
    protected TagMap tagMap;
    /**
     * Class for dealing with evaluation
     */
    protected Evaluator evaluator;

    public BHMM(CommandLineOptions options) {
        try {
            initializeFromOptions(options);
        } catch (IOException e) {
        }
    }

    /**
     * Initialize basic parameters from the command line. Depending on need
     * many parameters will be overwritten in subsequent initialization stages.
     *
     * @param options Command line options
     * @throws IOException
     */
    protected void initializeFromOptions(CommandLineOptions options) throws
          IOException {
        /**
         * Setting input data
         */
        dataFormat = options.getDataFormat();
        trainDataDir = options.getTrainDataDir();
        if (trainDataDir != null) {
            trainDirReader = new DirReader(trainDataDir, dataFormat);
        } else {
            trainDataDir = "";
        }

        testDataDir = options.getTestDataDir();
        if (testDataDir != null) {
            testDirReader = new DirReader(testDataDir, dataFormat);
            testWordIdx = new HashMap<String, Integer>();
            testIdxToWord = new HashMap<Integer, String>();
            testWordIdx.put(EOSw, EOSi);
            testIdxToWord.put(EOSi, EOSw);
        } else {
            testDataDir = "";
        }

        /**
         * Setting lexicons
         */
        trainWordIdx = new HashMap<String, Integer>();
        trainIdxToWord = new HashMap<Integer, String>();
        trainWordIdx.put(EOSw, EOSi);
        trainIdxToWord.put(EOSi, EOSw);

        /**
         * Setting dimensions
         */
        stateC = options.getContentStates();
        stateF = options.getFunctionStates();
        stateS = stateF + stateC;
        outputPerClass = options.getOutputPerClass();
        S3 = stateS * stateS * stateS;
        S2 = stateS * stateS;
        S1 = stateS;

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
        beta = options.getBeta();
        delta = options.getDelta();
        gamma = options.getGamma();

        /**
         * Initializing random number generator, etc.
         */
        randomSeed = options.getRandomSeed();
        if (randomSeed == -1) {
            randomSeed = 0;
        }
        mtfRand = new MersenneTwisterFast(randomSeed);

        tagMap = TagMapGenerator.generate(options.getTagSet(), options.getReductionLevel(), stateS);
        wordNormalizer = new WordNormalizerToLower(tagMap);

        modelName = options.getExperimentModel();
    }

    /**
     * Initialize data structures needed for inference from training data.
     */
    public void initializeFromTrainingData() {
        initializeTokenArrays(trainDirReader, trainWordIdx, trainIdxToWord);
        initializeCountArrays();
    }

    /**
     * Randomly initialize parameters for training
     */
    public abstract void initializeParametersRandom();

    /**
     * Initialize arrays that will be used to track the state, topic, split
     * position and switch of each token. The DocumentByTopic array is also
     * rewritten in sampling for test sets.
     *
     * @param dirReader Object to walk through files and directories
     * @param wordIdx   Dictionary from word to index
     * @param idxToWord Dictionary from index to word
     */
    protected void initializeTokenArrays(DirReader dirReader,
          HashMap<String, Integer> wordIdx, HashMap<Integer, String> idxToWord) {
        documentD = sentenceS = 0;
        ArrayList<Integer> wordVectorT = new ArrayList<Integer>(),
              goldFullTagVectorT = new ArrayList<Integer>(),
//              goldReducedTagVectorT = new ArrayList<Integer>(),
              sentenceVectorT = new ArrayList<Integer>(),
              documentVectorT = new ArrayList<Integer>();
        while ((dataReader = dirReader.nextDocumentReader()) != null) {
            try {
                String[][] sentence;
                while ((sentence = dataReader.nextSequence()) != null) {
                    for (String[] line : sentence) {
                        wordNormalizer.normalize(line);
                        String word = wordNormalizer.getWord();
                        String tag = wordNormalizer.getTag();
                        if (!word.isEmpty() && tag != null) {
                            if (!wordIdx.containsKey(word)) {
                                wordIdx.put(word, wordIdx.size());
                                idxToWord.put(idxToWord.size(), word);
                            }
                            wordVectorT.add(wordIdx.get(word));
                            sentenceVectorT.add(sentenceS);
                            documentVectorT.add(documentD);
                            if (tag != null) {
                                goldFullTagVectorT.add(tagMap.get(tag));
                            } else {
                                goldFullTagVectorT.add(-1);
                            }
                        }
                    }
//                    wordVectorT.add(EOSi);
//                    goldFullTagVectorT.add(tagMap.get(tag));
//                    sentenceVectorT.add(sentenceS);
//                    documentVectorT.add(documentD);
                    sentenceS++;
                }
            } catch (IOException e) {
            }
            documentD++;
        }

        wordN = wordVectorT.size();
        wordW = wordIdx.size();
        wbeta = beta * wordW;
        wdelta = delta * wordW;
        calpha = alpha * stateC;
        sgamma = gamma * stateS;

        wordVector = new int[wordN];
        goldTagVector = new int[wordN];
//        goldReducedTagVector = new int[wordN];
        sentenceVector = new int[wordN];
        documentVector = new int[wordN];

        first = new int[wordN];
        second = new int[wordN];
        third = new int[wordN];

        stateVector = new int[wordN];

        copyToArray(wordVector, wordVectorT);
        copyToArray(goldTagVector, goldFullTagVectorT);
        copyToArray(sentenceVector, sentenceVectorT);
        copyToArray(documentVector, documentVectorT);
    }

    /**
     * Initializes arrays for counting occurrences. These need to be initialized
     * regardless of whether the model being trained from raw data or whether
     * it is loaded from a saved model.
     */
    protected void initializeCountArrays() {

        stateCounts = new int[stateS];
        stateProbs = new double[stateS];
        for (int i = 0; i < stateS; ++i) {
            stateCounts[i] = 0;
            stateProbs[i] = 0;
        }

        stateByWord = new int[stateS * wordW];
        for (int i = 0; i < stateS * wordW; ++i) {
            stateByWord[i] = 0;
        }

        contentStateBySentence = new int[stateC * sentenceS];
        for (int i = 0; i < stateC * sentenceS; ++i) {
            contentStateBySentence[i] = 0;
        }

        sentenceCounts = new int[sentenceS];
        for (int i = 0; i < sentenceS; ++i) {
            sentenceCounts[i] = 0;
        }
        for (int i = 0; i < sentenceS; ++i) {
            sentenceCounts[sentenceVector[i]]++;
        }

        documentCounts = new int[documentD];
        for (int i = 0; i < documentD; ++i) {
            documentCounts[i] = 0;
        }

        functionStateByDocument = new int[stateS * documentD];
        for (int i = 0; i < stateS * documentD; ++i) {
            functionStateByDocument[i] = 0;
        }

        thirdOrderTransitions = new int[stateS * stateS * stateS * stateS];
        secondOrderTransitions = new int[stateS * stateS * stateS];
        firstOrderTransitions = new int[stateS * stateS];

        for (int i = 0; i < stateS * stateS * stateS * stateS; ++i) {
            thirdOrderTransitions[i] = 0;
        }
        for (int i = 0; i < stateS * stateS * stateS; ++i) {
            secondOrderTransitions[i] = 0;
        }

        for (int i = 0; i < stateS * stateS; ++i) {
            firstOrderTransitions[i] = 0;
        }

        sampleProbs = new double[samples];
        for (int i = 0; i < samples; ++i) {
            sampleProbs[i] = 0;
        }
    }

    /**
     * Learn parameters
     */
    public void train() {
        initializeParametersRandom();

        Annealer annealer = new SimulatedAnnealer();
        /**
         * Training iterations
         */
        for (int outiter = 0; outiter < outerIterations;
              ++outiter) {
            System.err.print("\nouter iteration " + outiter + ":");
            System.err.println("annealing temperature " + temperature);
            annealer.stabilizeTemperature();
            annealer.setTemperatureReciprocal(temperatureReciprocal);
            trainInnerIter(innerIterations, annealer);
//            trainInnerIter(innerIterations, "inner iteration");
            temperature -= temperatureDecrement;
            temperatureReciprocal = 1 / temperature;
        }
        /**
         * Increment it so sampling resumes at same temperature if it is loaded
         * from a model
         */
        temperature += temperatureDecrement;
    }

    /**
     * Training routine for the inner iterations
     *
     * @param itermax Maximum number of iterations to perform
     * @param annealer Callback to annealing process
     * @see HDPHMMLDA#sampleFromTrain()
     */
    protected abstract void trainInnerIter(int itermax, Annealer annealer);

    /**
     * Maximum posterior decoding of tag sequence
     */
    public void decode() {
        Annealer annealer = new MaximumPosteriorDecoder();
        trainInnerIter(1, annealer);
    }

    public void evaluate() {
        evaluator = new Evaluator(tagMap, DistanceMeasureEnum.Measure.JACCARD);
        evaluator.evaluateTags(stateVector, goldTagVector);
        System.err.println("One to one accuracy is " + evaluator.getOneToOneAccuracy());
        System.err.println("Many to one accuracy is " + evaluator.getManyToOneAccuracy());
    }

    public void printEvaluationScore(BufferedWriter out) throws IOException {
        out.write(modelParameterStringBuilder.toString());
        printNewlines(out, 2);
        out.write("One to one accuracy: " + evaluator.getOneToOneAccuracy());
        out.write("Many to one accuracy: " + evaluator.getManyToOneAccuracy());
    }

    /**
     * Normalize the sample counts.
     */
    public void normalize() {
        normalizeStates();
    }

    /**
     * Normalize the sample counts for words given state.
     */
    protected void normalizeStates() {
        topWordsPerState = new StringDoublePair[stateS][];
        for (int i = 1; i < stateS; ++i) {
            topWordsPerState[i] = new StringDoublePair[outputPerClass];
        }

        double sum = 0.;
        int i = 1;
        /**
         * Normalize content states
         */
        for (; i < stateC; ++i) {
            sum += stateProbs[i] = stateCounts[i] + wbeta;
            ArrayList<DoubleStringPair> topWords =
                  new ArrayList<DoubleStringPair>();
            /**
             * Start at one to leave out EOSi
             */
            for (int j = EOSi + 1; j < wordW; ++j) {
                topWords.add(new DoubleStringPair(
                      stateByWord[j * stateS + i] + beta, trainIdxToWord.get(
                      j)));
            }
            Collections.sort(topWords);
            for (int j = 0; j < outputPerClass; ++j) {
                topWordsPerState[i][j] =
                      new StringDoublePair(
                      topWords.get(j).stringValue,
                      topWords.get(j).doubleValue / stateProbs[i]);
            }
        }

        /**
         * Normalize function states
         */
        for (; i < stateS; ++i) {
            sum += stateProbs[i] = stateCounts[i] + wdelta;
            ArrayList<DoubleStringPair> topWords =
                  new ArrayList<DoubleStringPair>();
            /**
             * Start at one to leave out EOSi
             */
            for (int j = EOSi + 1; j < wordW; ++j) {
                topWords.add(new DoubleStringPair(
                      stateByWord[j * stateS + i] + delta, trainIdxToWord.get(
                      j)));
            }
            Collections.sort(topWords);
            for (int j = 0; j < outputPerClass; ++j) {
                topWordsPerState[i][j] =
                      new StringDoublePair(
                      topWords.get(j).stringValue,
                      topWords.get(j).doubleValue / stateProbs[i]);
            }
        }

        for (i = 1; i < stateS; ++i) {
            stateProbs[i] /= sum;
        }
    }

    /**
     * Print text that has been segmented/tagged in a training sample to output.
     *
     * @param outDir Root of path to generate output to
     * @throws IOException
     */
    public void printAnnotatedTrainText(String outDir) throws IOException {
        printAnnotatedText(outDir, trainDataDir, trainDirReader, trainIdxToWord);
    }

    /**
     * Print annotated text.
     *
     * @param outDir Root of path to generate output to
     * @param dataDir   Origin of data
     * @param dirReader DirReader for data
     * @param idxToWord Dictionary from index to word
     * @throws IOException
     * @see HDPHMMLDA#printAnnotatedTestText(java.lang.String)
     * @see HDPHMMLDA#printAnnotatedTrainText(java.lang.String)
     */
    public void printAnnotatedText(String outDir, String dataDir,
          DirReader dirReader, HashMap<Integer, String> idxToWord)
          throws IOException {
        DirWriter dirWriter = new DirWriter(outDir, dataDir, dirReader);
        String root = dirWriter.getRoot();

        BufferedWriter bufferedWriter;

        int docid = 0, cursent = 0, prevsent = 0;
        String word;
        bufferedWriter = dirWriter.nextOutputBuffer();

        for (int i = 0; i < wordN; ++i) {
            cursent = sentenceVector[i];
            if (docid != documentVector[i]) {
                bufferedWriter.close();
                bufferedWriter = dirWriter.nextOutputBuffer();
                docid = documentVector[i];
            }

            int wordid = wordVector[i];

            if (cursent != prevsent) {
                bufferedWriter.newLine();
            }

            word = idxToWord.get(wordid);
            bufferedWriter.write(word);
            bufferedWriter.write("\t");
            int stateid = stateVector[i];
            int goldid = goldTagVector[i];
            String tag = String.format("F:%s", tagMap.getOneToOneTagString(stateid));
            bufferedWriter.write(tag);
            bufferedWriter.write("\t");
            tag = String.format("R:%s", tagMap.getManyToOneTagString(stateid));
            bufferedWriter.write(tag);
            bufferedWriter.write("\t");
            tag = String.format("G:%s", tagMap.getGoldReducedTagString(goldid));
            bufferedWriter.write(tag);
            bufferedWriter.newLine();

            prevsent = cursent;
        }
        bufferedWriter.close();

        bufferedWriter = new BufferedWriter(new OutputStreamWriter(
              new FileOutputStream(root + File.separator + "PARAMETERS")));

        bufferedWriter.write(modelParameterStringBuilder.toString());
        bufferedWriter.close();
    }

    /**
     * Print the normalized sample counts to out. Print only the top {@link
     * #outputPerTopic} per given state and topic.
     *
     * @param out Output buffer to write to.
     * @throws IOException
     */
    public void printTabulatedProbabilities(BufferedWriter out) throws
          IOException {
        printStates(out);
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
     * Print the normalized sample counts for each state to out. Print only the top {@link
     * #outputPerTopic} per given state.
     *
     * @param out
     * @throws IOException
     */
    protected void printStates(BufferedWriter out) throws IOException {
        int startt = 1, M = 4, endt = Math.min(M + 1, stateProbs.length);
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
                          topWordsPerState[c][i].stringValue,
                          topWordsPerState[c][i].doubleValue);
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
     * Creates a string stating the parameters used in the model. The
     * string is used for pretty printing purposes and clarity in other
     * output routines.
     */
    public void setModelParameterStringBuilder() {
        modelParameterStringBuilder = new StringBuilder();
        String line = null;
        line = String.format("stateS:%d", stateS) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("wordW:%d", wordW) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("wordN:%d", wordN) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("gamma:%f", gamma) + newline;
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
        line = String.format("rootDir:%s", trainDataDir) + newline;
        modelParameterStringBuilder.append(line);
        line = String.format("testRootDir:%s", testDataDir) + newline;
        modelParameterStringBuilder.append(line);
    }

    /**
     * Copy a sequence of numbers from ta to array ia.
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
}
