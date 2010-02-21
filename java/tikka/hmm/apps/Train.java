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
package tikka.hmm.apps;

import java.io.*;

import org.apache.commons.cli.*;
import tikka.hmm.model.base.HMM;
import tikka.hmm.model.em.EMHMM;

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

            HMM hmm = null;

            String experimentModel = modelOptions.getExperimentModel();

            if (experimentModel.equals("m1")) {
                System.err.println("Using EM HMM!");
                hmm = new EMHMM(modelOptions);
            } else if (experimentModel.equals("m2")) {
            }

            hmm.train();
            hmm.normalize();

        } catch (ParseException exp) {
            System.out.println("Unexpected exception parsing command line options:" + exp.getMessage());
        } catch (IOException exp) {
            System.out.println("IOException:" + exp.getMessage());
            System.exit(0);
        }
    }
}
