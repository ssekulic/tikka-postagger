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
package tikka.bhmm.apps;

import tikka.bhmm.model.base.*;
import tikka.bhmm.models.*;

import java.io.*;

import org.apache.commons.cli.*;

/**
 * Train and test a tagger.
 *
 * @author  Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
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
                formatter.printHelp("java Tag Model", options);
                System.exit(0);
            }

            CommandLineOptions modelOptions = new CommandLineOptions(cline);

            BHMM hmm = null;

            String experimentModel = modelOptions.getExperimentModel();

            if (experimentModel.equals("m1")) {
                System.err.println("Using BHMM M1!");
                hmm = new BHMMm1(modelOptions);
            } else if (experimentModel.equals("m2")) {
                System.err.println("Using BHMM M2!");
                hmm = new BHMMm2(modelOptions);
            } else if (experimentModel.equals("m3")) {
                System.err.println("Using BHMM M3!");
                hmm = new BHMMm3(modelOptions);
            } else if (experimentModel.equals("m4")) {
                System.err.println("Using BHMM M4!");
                hmm = new BHMMm4(modelOptions);
            } else if (experimentModel.equals("m5")) {
                System.err.println("Using BHMM M5!");
                hmm = new BHMMm5(modelOptions);
            } else if (experimentModel.equals("m5s2")) {
                System.err.println("Using BHMM M5 S2!");
                hmm = new BHMMm5s2(modelOptions);
            } else if (experimentModel.equals("m6")) {
                System.err.println("Using BHMM M6!");
                hmm = new BHMMm6(modelOptions);
            } else if (experimentModel.equals("m7")) {
                System.err.println("Using BHMM M7!");
                hmm = new BHMMm7(modelOptions);
            }

            System.err.println("Randomly initializing values!");
            hmm.initializeFromTrainingData();
            System.err.println("Beginning training!");
            hmm.train();

            /**
             * Save model if specified
             */
            String modelOutputPath = modelOptions.getModelOutputPath();
            if (modelOutputPath != null) {
                System.err.println("Saving model to :"
                      + modelOutputPath);
                SerializableModel serializableModel = null;

                serializableModel = new SerializableModel(hmm);
                serializableModel.saveModel(modelOutputPath);
            }

            /**
             * Save tabulated probabilities
             */
            if (modelOptions.getTabularOutputFilename() != null) {
                System.err.println("Normalizing parameters!");
                hmm.normalize();
                System.err.println("Printing tabulated output to :"
                      + modelOptions.getTabularOutputFilename());
                hmm.printTabulatedProbabilities(modelOptions.getTabulatedOutput());
            }

        } catch (ParseException exp) {
            System.out.println("Unexpected exception parsing command line options:" + exp.getMessage());
        } catch (IOException exp) {
            System.out.println("IOException:" + exp.getMessage());
            System.exit(0);
        }
    }
}
