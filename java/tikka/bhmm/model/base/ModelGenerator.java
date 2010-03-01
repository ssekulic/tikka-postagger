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

    public static BHMM generator(CommandLineOptions options) {
        String modelName = options.getExperimentModel();
        return generator(modelName, options);
    }

    public static BHMM generator(String modelName, CommandLineOptions options) {
        BHMM bhmm = null;
        if (modelName.equals("m1")) {
            bhmm = new BHMMm1(options);
        } else if (modelName.equals("m2")) {
            bhmm = new BHMMm2(options);
        } else if (modelName.equals("m3")) {
            bhmm = new BHMMm3(options);
        } else if (modelName.equals("m4")) {
            bhmm = new BHMMm1(options);
        } else if (modelName.equals("m5")) {
            bhmm = new xBHMMm5(options);
        } else if (modelName.equals("m5s2")) {
            bhmm = new xBHMMm5s2(options);
        } else if (modelName.equals("m6")) {
            bhmm = new BHMMm6(options);
        } else if (modelName.equals("m7")) {
            bhmm = new BHMMm7(options);
        }
        return bhmm;
    }
}
