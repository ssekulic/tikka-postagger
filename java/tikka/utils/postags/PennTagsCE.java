///////////////////////////////////////////////////////////////////////////////
// To change this template, choose Tools | Templates
// and open the template in the editor.
///////////////////////////////////////////////////////////////////////////////
package tikka.utils.postags;

import java.util.Arrays;
import java.util.HashSet;

/**
 *
 * @author tsmoon
 */
public class PennTagsCE extends TagMap {

    @Override
    protected HashSet<String> setTags() {
        return setPennTags();
    }

    @Override
    protected HashSet<String> reduceTag() {
        fullTagToReducedTag.put("CD", "ADJ");
        fullTagToReducedTag.put("JJ", "ADJ");
        fullTagToReducedTag.put("JJR", "ADJ");
        fullTagToReducedTag.put("JJS", "ADJ");
        fullTagToReducedTag.put("PRP$", "ADJ");
        fullTagToReducedTag.put("CC", "CONJ");
        fullTagToReducedTag.put(".", "ENDPUNC");
        fullTagToReducedTag.put("-LRB-", "LPUNC");
        fullTagToReducedTag.put("``", "LPUNC");
        fullTagToReducedTag.put("POS", "POS");
        fullTagToReducedTag.put("RP", "PRT");
        fullTagToReducedTag.put("TO", "TO");
        fullTagToReducedTag.put("MD", "V");
        fullTagToReducedTag.put("VBD", "V");
        fullTagToReducedTag.put("VBP", "V");
        fullTagToReducedTag.put("VB", "V");
        fullTagToReducedTag.put("VBZ", "V");
        fullTagToReducedTag.put("VBG", "VBG");
        fullTagToReducedTag.put("RB", "ADV");
        fullTagToReducedTag.put("RBR", "ADV");
        fullTagToReducedTag.put("RBS", "ADV");
        fullTagToReducedTag.put("DT", "DET");
        fullTagToReducedTag.put("PDT", "DET");
        fullTagToReducedTag.put(",", "INPUNC");
        fullTagToReducedTag.put(":", "INPUNC");
        fullTagToReducedTag.put("LS", "INPUNC");
        fullTagToReducedTag.put("SYM", "INPUNC");
        fullTagToReducedTag.put("UH", "INPUNC");
        fullTagToReducedTag.put("EX", "N");
        fullTagToReducedTag.put("FW", "N");
        fullTagToReducedTag.put("NN", "N");
        fullTagToReducedTag.put("NNP", "N");
        fullTagToReducedTag.put("NNPS", "N");
        fullTagToReducedTag.put("NNS", "N");
        fullTagToReducedTag.put("PRP", "N");
        fullTagToReducedTag.put("IN", "PREP");
        fullTagToReducedTag.put("-RRB-", "RPUNC");
        fullTagToReducedTag.put("''", "RPUNC");
        fullTagToReducedTag.put("WDT", "W");
        fullTagToReducedTag.put("WP$", "W");
        fullTagToReducedTag.put("WP", "W");
        fullTagToReducedTag.put("WRB", "W");
        fullTagToReducedTag.put("VBN", "VBN");

        setIdxMap(reducedTagSet);
        return reducedTagSet;
    }
}
