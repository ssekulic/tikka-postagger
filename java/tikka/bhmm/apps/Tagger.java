///////////////////////////////////////////////////////////////////////////////
// To change this template, choose Tools | Templates
// and open the template in the editor.
///////////////////////////////////////////////////////////////////////////////
package tikka.bhmm.apps;

import tikka.bhmm.model.base.*;
import tikka.bhmm.models.*;

import java.io.IOException;

import org.apache.commons.cli.*;

/**
 *
 * @author tsmoon
 */
public class Tagger extends MainBase {

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
            String modelInputPath = modelOptions.getModelInputPath();

            /**
             * Choose whether to load from previously saved model or train on new
             */
            if (modelInputPath != null) {
                System.err.println("Loading from model:" + modelInputPath);
                SerializableModel serializableModel = new SerializableModel();
                hmm = serializableModel.loadModel(modelOptions, modelInputPath);
                hmm.initializeFromLoadedModel(modelOptions);
            } else {
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
            }

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

            System.err.println("Maximum posterior decoding");
            hmm.decode();

            /**
             * Set the string of parameters.
             */
            hmm.setModelParameterStringBuilder();

            String evaluationOutputFilename = modelOptions.getEvaluationOutputFilename();
            if (evaluationOutputFilename != null) {
                System.err.println("Performing evaluation");
                hmm.evaluate();
                System.err.println("Also printing evaluation results to " + evaluationOutputFilename);
                hmm.printEvaluationScore(modelOptions.getEvaluationOutput());
                modelOptions.getEvaluationOutput().close();
            }

            /**
             * Tag and segment training files from last iteration if specified
             */
            String annotatedTextDir = modelOptions.getAnnotatedTrainTextOutDir();
            if (annotatedTextDir != null) {
                System.err.println("Printing annotated text to :"
                      + annotatedTextDir);
                hmm.printAnnotatedTrainText(annotatedTextDir);
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
                modelOptions.getTabulatedOutput().close();
            }

        } catch (ParseException exp) {
            System.out.println("Unexpected exception parsing command line options:" + exp.getMessage());
        } catch (IOException exp) {
            System.out.println("IOException:" + exp.getMessage());
            System.exit(0);
        }
    }
}
