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

import tikka.opennlp.io.DataFormatEnum;

import tikka.bhmm.apps.CommandLineOptions;

import tikka.bhmm.models.BHMMm1;
import tikka.bhmm.models.BHMMm2;
import tikka.bhmm.models.BHMMm3;
import tikka.bhmm.models.BHMMm4;
import tikka.bhmm.models.BHMMm5;
import tikka.bhmm.models.BHMMm5s2;
import tikka.bhmm.models.BHMMm6;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Object where model parameters are saved. Includes both constant parameters
 * and inferred parameters.
 * 
 * @author tsmoon
 */
public class SerializableModel implements Serializable {

    static private final long serialVersionUID = 42L;
    /**
     * Model buffer for loading from file. Fields are read to loadBuffer than
     * copied to the enclosing class (this).
     */
    protected SerializableModel loadBuffer = null;
    /**
     * Format of the input data
     */
    protected DataFormatEnum.DataFormat dataFormat;
    /**
     * Seed for random number generator.
     */
    protected int randomSeed;
    /**
     * Number of iterations
     */
    protected int iterations;
    /**
     * Number of word types
     */
    protected int wordW;
    /**
     * Number of word tokens
     */
    protected int wordN;
    /**
     * Hyperparameter for content state distributions given sentences
     */
    protected double alpha;
    /**
     * Hyperparameter for word emissions for content states
     */
    protected double beta;
    /**
     * Hyperparameter for state emissions
     */
    protected double gamma;
    /**
     * Hyperparameter for word emissions for function states
     */
    protected double delta;
    /**
     * Hashtable from word to index.
     */
    protected HashMap<String, Integer> wordIdx;
    /**
     * Array of word indexes. Of length {@link #wordN}.
     */
    protected int[] wordVector;
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
     * Number of states including topic states and sentence boundary state
     */
    protected int stateS;
    /**
     * Token indexes for sentences
     */
    protected int[] sentenceVector;
    /**
     * Array of states over tokens
     */
    protected int[] stateVector;
    /**
     * Path of training data.
     */
    protected String trainDataDir;
    /**
     * Type of model that is being run.
     */
    protected String modelName;

    /**
     * Constructor to use when model is being saved.
     * 
     * @param hmm Model to be saved
     */
    public SerializableModel(BHMM m) {
        alpha = m.alpha;
        beta = m.beta;
        dataFormat = m.dataFormat;
        delta = m.delta;
        gamma = m.gamma;
        initialTemperature = m.initialTemperature;
        iterations = m.iterations;
        modelName = m.modelName;
        randomSeed = m.randomSeed;
        trainDataDir = m.trainDataDir;
        sentenceVector = m.sentenceVector;
        stateVector = m.stateVector;
        stateS = m.stateS;
        targetTemperature = m.targetTemperature;
        temperatureDecrement = m.temperatureDecrement;
        wordIdx = m.trainWordIdx;
        wordN = m.wordN;
        wordVector = m.wordVector;
        wordW = m.wordW;
    }

    /**
     * Constructor to use when model is being loaded
     */
    public SerializableModel() {
    }

    /**
     * Load a previously trained model.
     *
     * @param filename  Full path of model location.
     * @return  The model that has been loaded.
     * @throws IOException
     * @throws FileNotFoundException
     */
    public BHMM loadModel(CommandLineOptions options, String filename)
          throws IOException,
          FileNotFoundException {
        ObjectInputStream modelIn =
              new ObjectInputStream(new GZIPInputStream(new FileInputStream(
              filename)));
        try {
            loadBuffer = (SerializableModel) modelIn.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        copy(loadBuffer);
        loadBuffer = null;
        modelIn.close();

        BHMM hmm = null;
        if (modelName.equals("m1")) {
            hmm = new BHMMm1(options);
        } else if (modelName.equals("m2")) {
            hmm = new BHMMm2(options);
        } else if (modelName.equals("m3")) {
            hmm = new BHMMm3(options);
        } else if (modelName.equals("m4")) {
            hmm = new BHMMm4(options);
        } else if (modelName.equals("m5")) {
            hmm = new BHMMm5(options);
        } else if (modelName.equals("m5s2")) {
            hmm = new BHMMm5s2(options);
        } else if (modelName.equals("m6")) {
            hmm = new BHMMm6(options);
        }

        return copy(hmm);
    }

    /**
     * Save the trained model.
     *
     * @param filename  Full path of model location.
     * @throws IOException
     */
    public void saveModel(String filename) throws IOException {
        ObjectOutputStream modelOut =
              new ObjectOutputStream(new GZIPOutputStream(
              new FileOutputStream(filename)));
        modelOut.writeObject(this);
        modelOut.close();
    }

    protected void copy(SerializableModel sm) {
        alpha = sm.alpha;
        beta = sm.beta;
        dataFormat = sm.dataFormat;
        gamma = sm.gamma;
        initialTemperature = sm.initialTemperature;
        iterations = sm.iterations;
        modelName = sm.modelName;
        randomSeed = sm.randomSeed;
        trainDataDir = sm.trainDataDir;
        sentenceVector = sm.sentenceVector;
        stateVector = sm.stateVector;
        stateS = sm.stateS;
        targetTemperature = sm.targetTemperature;
        temperatureDecrement = sm.temperatureDecrement;
        wordIdx = sm.wordIdx;
        wordN = sm.wordN;
        wordVector = sm.wordVector;
        wordW = sm.wordW;
    }

    protected BHMM copy(BHMM hmm) {
        hmm.alpha = alpha;
        hmm.beta = beta;
        hmm.dataFormat = dataFormat;
        hmm.gamma = gamma;
        hmm.initialTemperature = initialTemperature;
        hmm.iterations = iterations;
        hmm.modelName = modelName;
        hmm.randomSeed = randomSeed;
        hmm.trainDataDir = trainDataDir;
        hmm.sentenceVector = sentenceVector;
        hmm.stateVector = stateVector;
        hmm.stateS = stateS;
        hmm.targetTemperature = targetTemperature;
        hmm.temperatureDecrement = temperatureDecrement;
        hmm.trainWordIdx = wordIdx;
        hmm.wordN = wordN;
        hmm.wordVector = wordVector;
        hmm.wordW = wordW;

        return hmm;
    }
}
