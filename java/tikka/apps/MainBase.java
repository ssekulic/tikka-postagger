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
package tikka.apps;

import org.apache.commons.cli.Options;

/**
 * Base class for all command line applications. All it does is set the option
 * parameters.
 * 
 * @author tsmoon
 * @see CommandLineOptions Parser for command line options
 */
public class MainBase {

    /**
     * Sets option parameters. Must be called in any derived class.
     * 
     * @param options Command line option handler
     */
    public static void setOptions(Options options) {
        options.addOption("a", "alpha", true, "alpha value (default=50/topics)");
        options.addOption("c", "data-format", true,
                "format of input data [conll2k, hashslash, pipesep, raw; default=conll2k]");
        options.addOption("d", "data-dir", true,
                "full path to directory containing training documents");
        options.addOption("e", "experiment-model", true,
                "model to use [m1,m2; default=m1]");
        options.addOption("h", "help", false, "print help");
        options.addOption("i", "iterations", true,
                "number of training iterations (default=100)");
        options.addOption("j", "annotated-test-text", true,
                "full path to save annotated test set text to");
        options.addOption("ks", "samples", true,
                "number of samples to take (default=100)");
        options.addOption("kl", "lag", true,
                "number of iterations between samples (default=100)");
        options.addOption("l", "model-input-path", true,
                "full path of model to be loaded");
        options.addOption("m", "model-output-path", true,
                "full path to save model to");
        options.addOption("n", "annotated-text", true,
                "full path to save annotated text to");
        options.addOption("os", "output-sample-score", true,
                "path of output for bayes factors and perplexity measures for " +
                "samples taken for the training and/or training data");
        options.addOption("ot", "output-tabulated-probabilities", true,
                "path of tabulated probability output");
        options.addOption("pi", "initial-temperature", true,
                "initial temperature for annealing regime (default=0.1)");
        options.addOption("pr", "target-temperature", true,
                "temperature at which to stop annealing (default=1)");
        options.addOption("pt", "temperature-increment", true,
                "temperature increment steps (default=0.1)");
        options.addOption("s", "states", true,
                "number of states in HMM (default=15)");
        options.addOption("r", "random-seed", true,
                "seed random number generator (default=false)");
        options.addOption("t", "topics", true, "number of topics (default=50)");
        options.addOption("u", "topic-substates", true, "number of substates given to topics " +
                "(default=1)");
        options.addOption("w", "words-class", true,
                "number of words to print per class (default=50)");
        options.addOption("xmustembase", "mu-stem-base", true, "(default=3000)");
        options.addOption("xmustem", "mu-stem", true, "(default=300)");
        options.addOption("xbetastembase", "beta-stem-base", true,
                "(default=3000)");
        options.addOption("xbetastem", "beta-stem", true, "(default=300)");
        options.addOption("xmuaffixbase", "mu-affix-base", true,
                "(default=3000)");
        options.addOption("xmuaffix", "mu-affix", true, "(default=300)");
        options.addOption("xxi", "xi", true, "(default=0.1)");
        options.addOption("xpsi", "psi", true, "(default=0.1)");
        options.addOption("xstemboundaryprob", "stem-boundary-prob", true,
                "(default=0.2)");
        options.addOption("xaffixboundaryprob", "affix-boundary-prob", true,
                "(default=0.2)");
    }
}