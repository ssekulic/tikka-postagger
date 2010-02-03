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
package tikka.utils;

import tikka.opennlp.io.DataFormatEnum;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.cli.*;

/**
 * Handles options from the command line. Also sets the default parameter
 * values.
 *
 * @author tsmoon
 */
public class CommandLineOptions {

    /**
     * Number of topics
     */
    protected int topics = 50;
    /**
     * Number of types (either word or morpheme) to print per state or topic
     */
    protected int outputPerClass = 50;
    /**
     * Hyperparameter of topic prior
     */
    protected double alpha = 50 / topics;
    /**
     * Hyperparameter for word/topic prior
     */
    protected double beta = 0.01;
    /**
     * Path for training data. Should be a full directory
     */
    protected String dataDir;
    /**
     * full path of model to be loaded
     */
    protected String modelInputPath = null;
    /**
     * full path to save model to
     */
    protected String modelOutputPath = null;
    /**
     * Root of path to output annotated texts to
     */
    protected String annotatedTextDir = null;
    /**
     * Number of training iterations
     */
    protected int numIterations = 100;
    /**
     * Number to seed random number generator
     */
    protected int randomSeed = 0;
    /**
     * Specifier of training data format.
     */
    protected DataFormatEnum.DataFormat dataFormat =
            DataFormatEnum.DataFormat.CONLL2K;
    /**
     * Output buffer to write normalized data to.
     */
    protected BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
            System.out));
    /**
     * Temperature at which to start annealing process
     */
    protected double initialTemperature = 1;
    /**
     * Decrement at which to reduce the temperature in annealing process
     */
    protected double temperatureDecrement = 0.1;
    /**
     * Stop changing temperature after the following temp has been reached.
     */
    protected double targetTemperature = 1;
    /**
     * Hyperparameters for (hierarchical) dirichlet processes
     */
    protected double muStemBase = 3000, muAffixBase = 3000, muStem = 300,
            muAffix = 300, betaStemBase = 3000, betaStem = 300;
    /**
     * Probability of null morph or ,equivalently, the probability of a
     * morpheme boundary
     */
    protected double stemBoundaryProb = 0.2, affixBoundaryProb = 0.2;
    /**
     * Hyperparameter for state transition priors
     */
    protected double psi = 0.1;
    /**
     * Hyperparameter for "switch" prior. See {@link  #switchVector}
     * and {@link #fourthOrderSwitches}.
     */
    protected double xi = 0.1;
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
     * Model to use for training. Use unhelpful, non-mnemonic names
     */
    protected String experimentModel = "m1";
    /**
     * Hyperparameter for word emission priors
     */
    protected double gamma = 0.01;
    /**
     * Number of states in HMM.
     */
    protected int states = 15;
    /**
     * Number of topic states. This is a less than {@link #stateS} and entails
     * that the topic states are a subset of the full states.
     */
    protected int topicSubStates = 1;

    /**
     * 
     * @param cline
     * @throws IOException
     */
    public CommandLineOptions(CommandLine cline) throws IOException {

        String opt = null;

        for (Option option : cline.getOptions()) {
            String value = option.getValue();
            switch (option.getOpt().charAt(0)) {
                case 'a':
                    alpha = Double.parseDouble(value);
                    break;
                case 'c':
                    if (value.equals("conll2k")) {
                        dataFormat = DataFormatEnum.DataFormat.CONLL2K;
                    } else if (value.equals("hashslash")) {
                        dataFormat = DataFormatEnum.DataFormat.HASHSLASH;
                    } else if (value.equals("pipesep")) {
                        dataFormat = DataFormatEnum.DataFormat.PIPESEP;
                    } else if (value.equals("raw")) {
                        dataFormat = DataFormatEnum.DataFormat.RAW;
                    } else {
                        System.err.println(
                                "\"" + value + "\" is an unknown data format option.");
                        System.exit(1);
                    }
                    break;
                case 'd':
                    if (value.endsWith("" + File.separator)) {
                        dataDir = value.substring(0, value.length() - 1);
                    } else {
                        dataDir = value;
                    }
                    break;
                case 'e':
                    experimentModel = value;
                    break;
                case 'i':
                    numIterations = Integer.parseInt(value);
                    break;
                case 'l':
                    modelInputPath = value;
                    break;
                case 'm':
                    modelOutputPath = value;
                    break;
                case 'n':
                    annotatedTextDir = value;
                    break;
                case 'o':
                    out.close();
                    out = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(value)));
                    break;
                case 'p':
                    opt = option.getOpt();
                    if (opt.equals("pi")) {
                        initialTemperature = Double.parseDouble(value);
                    } else if (opt.equals("pr")) {
                        targetTemperature = Double.parseDouble(value);
                    } else if (opt.equals("pt")) {
                        temperatureDecrement = Double.parseDouble(value);
                    }
                    break;
                case 'r':
                    randomSeed = Integer.valueOf(value);
                    break;
                case 's':
                    states = Integer.parseInt(value);
                    break;
                case 't':
                    topics = Integer.parseInt(value);
                    break;
                case 'w':
                    outputPerClass = Integer.parseInt(value);
                    break;
                case 'x':
                    opt = option.getOpt();
                    if (opt.equals("xmustembase")) {
                        muStemBase = Double.parseDouble(value);
                    } else if (opt.equals("xmustem")) {
                        muStem = Double.parseDouble(value);
                    } else if (opt.equals("xbetastembase")) {
                        betaStemBase = Double.parseDouble(value);
                    } else if (opt.equals("xbetastem")) {
                        betaStem = Double.parseDouble(value);
                    } else if (opt.equals("xmuaffixbase")) {
                        muAffixBase = Double.parseDouble(value);
                    } else if (opt.equals("xmuaffix")) {
                        muAffix = Double.parseDouble(value);
                    } else if (opt.equals("xpsi")) {
                        psi = Double.parseDouble(value);
                    } else if (opt.equals("xxi")) {
                        xi = Double.parseDouble(value);
                    } else if (opt.equals("xstemboundaryprob")) {
                        stemBoundaryProb = Double.parseDouble(value);
                    } else if (opt.equals("xaffixboundaryprob")) {
                        affixBoundaryProb = Double.parseDouble(value);
                    }
                    break;
            }
        }
    }

    public double getAlpha() {
        return alpha;
    }

    public double getBeta() {
        return beta;
    }

    public String getDataDir() {
        return dataDir;
    }

    public DataFormatEnum.DataFormat getDataFormat() {
        return dataFormat;
    }

    public int getNumIterations() {
        return numIterations;
    }

    public BufferedWriter getOutput() {
        return out;
    }

    public int getOutputPerClass() {
        return outputPerClass;
    }

    public int getRandomSeed() {
        return randomSeed;
    }

    public double getInitialTemperature() {
        return initialTemperature;
    }

    public double getTargetTemperature() {
        return targetTemperature;
    }

    public double getTemperatureDecrement() {
        return temperatureDecrement;
    }

    public int getTopics() {
        return topics;
    }

    public double getBetaStemBase() {
        return betaStemBase;
    }

    public double getBetaStem() {
        return betaStem;
    }

    public String getExperimentModel() {
        return experimentModel;
    }

    public double getMuStemBase() {
        return muStemBase;
    }

    public double getMuAffixBase() {
        return muAffixBase;
    }

    public double getMuStem() {
        return muStem;
    }

    public double getMuAffix() {
        return muAffix;
    }

    public double getPsi() {
        return psi;
    }

    public double getXi() {
        return xi;
    }

    public double getStemBoundaryProb() {
        return stemBoundaryProb;
    }

    public double getAffixBoundaryProb() {
        return affixBoundaryProb;
    }

    public double getGamma() {
        return gamma;
    }

    public int getStates() {
        return states;
    }

    public int getTopicSubStates() {
        return topicSubStates;
    }

    public String getModelInputPath() {
        return modelInputPath;
    }

    public String getModelOutputPath() {
        return modelOutputPath;
    }

    public String getAnnotatedTextDir() {
        return annotatedTextDir;
    }
}
