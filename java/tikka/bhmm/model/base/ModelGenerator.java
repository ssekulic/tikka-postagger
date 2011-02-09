///////////////////////////////////////////////////////////////////////////////
// To change this template, choose Tools | Templates
// and open the template in the editor.
///////////////////////////////////////////////////////////////////////////////
package tikka.bhmm.model.base;

import tikka.bhmm.apps.CommandLineOptions;
import tikka.bhmm.models.*;

/**
 *
 * @author tsmoon
 */
public class ModelGenerator {

    public static HMMBase generator(CommandLineOptions options) {
        String modelName = options.getExperimentModel();
        return generator(modelName, options);
    }

    public static HMMBase generator(String modelName, CommandLineOptions options) {
        HMMBase bhmm = null;
        if (modelName.equals("m1")) {
            bhmm = new CDHMMS(options);
        } else if (modelName.equals("m2")) {
            bhmm = new HMM(options);
        } else if (modelName.equals("m3")) {
            bhmm = new HMMP(options);
        } else if (modelName.equals("m4")) {
            bhmm = new LDAHMM(options);
        } else if (modelName.equals("m6")) {
            bhmm = new CDHMMD(options);
        }
        return bhmm;
    }
}
