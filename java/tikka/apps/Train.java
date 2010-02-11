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

import tikka.models.hhl.HDPHMMLDA;
import tikka.models.hhl.m1.HDPHMMLDAm1;

import java.io.IOException;

import org.apache.commons.cli.*;
import tikka.models.hhl.SerializableModel;
import tikka.utils.math.BayesFactorEval;

/**
 * Command line module for learning parameters for HDPHMMLDA from training text.
 * This does not tag test text.
 *
 * @author tsmoon
 */
public class Train extends MainBase {

    public static void main(String[] args) {
        CommandLineParser optparse = new PosixParser();
        Options options = new Options();
        setOptions(options);

        try {
            CommandLine cline = optparse.parse(options, args);

            if (cline.hasOption('h')) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java HybridHMMLDA Model", options);
                System.exit(0);
            }

            CommandLineOptions modelOptions = new CommandLineOptions(cline);

            HDPHMMLDA hhl = null;
            String experimentModel = modelOptions.getExperimentModel();

            if (experimentModel.equals("m1")) {
                System.err.println("Using model 1!");
                hhl = new HDPHMMLDAm1(modelOptions);
            } else {
                System.err.println("Using model 1!");
                hhl = new HDPHMMLDAm1(modelOptions);
            }

            System.err.println("Randomly initializing values!");
            hhl.initializeFromTrainingData();
            System.err.println("Beginning training!");
            hhl.train();
            System.err.println("Normalizing parameters!");
            hhl.normalize();

            /**
             * Save tabulated probabilities
             */
            if (modelOptions.getTabularOutputFilename() != null) {
                System.err.println("Printing tabulated output to :"
                      + modelOptions.getTabularOutputFilename());
                hhl.printTabulatedProbabilities(modelOptions.getTabulatedOutput());
            }

            /**
             * Save model if specified
             */
            String modelOutputPath = modelOptions.getModelOutputPath();
            if (modelOutputPath != null) {
                System.err.println("Saving model to :"
                      + modelOutputPath);
                SerializableModel serializableModel = new SerializableModel(hhl);
                serializableModel.saveModel(modelOutputPath);
            }

            /**
             * Set the string of parameters.
             */
            hhl.setModelParameterStringBuilder();
            /**
             * Calculate sample score, aka Bayes factor
             */
            if (modelOptions.getSampleScoreOutputFilename() != null) {
                System.err.println("Beginning sampling");
                hhl.sampleFromTrain();
                System.err.println("Saving sample scores to :"
                      + modelOptions.getSampleScoreOutputFilename());
                sampleEval = new BayesFactorEval();
                hhl.printSampleScoreData(modelOptions.getSampleScoreOutput(),
                      sampleEval, "Scores from TRAINING data");
                modelOptions.getSampleScoreOutput().close();
            }

            /**
             * Tag and segment training files from last iteration if specified
             */
            String annotatedTextDir = modelOptions.getAnnotatedTextOutDir();
            if (annotatedTextDir != null) {
                System.err.println("Printing annotated text to :"
                      + annotatedTextDir);
                hhl.printAnnotatedTrainText(annotatedTextDir);
            }
        } catch (ParseException exp) {
            System.out.println("Unexpected exception parsing command line options:"
                  + exp.getMessage());
        } catch (IOException exp) {
            System.out.println("IOException:" + exp.getMessage());
            System.exit(0);
        }
    }
}
