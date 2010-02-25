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
public class PennTagsCE extends TagSet {

    @Override
    public HashSet<String> setTags() {
        return setPennTags();
    }

    @Override
    public HashSet<String> reduceTag() {
        put("CD", "ADJ");
        put("JJ", "ADJ");
        put("JJR", "ADJ");
        put("JJS", "ADJ");
        put("PRP$", "ADJ");
        put("CC", "CONJ");
        put(".", "ENDPUNC");
        put("-LRB-", "LPUNC");
        put("``", "LPUNC");
        put("POS", "POS");
        put("RP", "PRT");
        put("TO", "TO");
        put("MD", "V");
        put("VBD", "V");
        put("VBP", "V");
        put("VB", "V");
        put("VBZ", "V");
        put("VBG", "VBG");
        put("RB", "ADV");
        put("RBR", "ADV");
        put("RBS", "ADV");
        put("DT", "DET");
        put("PDT", "DET");
        put(",", "INPUNC");
        put(":", "INPUNC");
        put("LS", "INPUNC");
        put("SYM", "INPUNC");
        put("UH", "INPUNC");
        put("EX", "N");
        put("FW", "N");
        put("NN", "N");
        put("NNP", "N");
        put("NNPS", "N");
        put("NNS", "N");
        put("PRP", "N");
        put("IN", "PREP");
        put("-RRB-", "RPUNC");
        put("''", "RPUNC");
        put("WDT", "W");
        put("WP$", "W");
        put("WP", "W");
        put("WRB", "W");
        put("VBN", "VBN");

//        put("ADJ", "CD");
//        put("ADJ", "JJ");
//        put("ADJ", "JJR");
//        put("ADJ", "JJS");
//        put("ADJ", "PRP$");
//        put("CONJ", "CC");
//        put("ENDPUNC", ".");
//        put("LPUNC", "-LRB-");
//        put("LPUNC", "``");
//        put("POS", "POS");
//        put("PRT", "RP");
//        put("TO", "TO");
//        put("V", "MD");
//        put("V", "VBD");
//        put("V", "VBP");
//        put("V", "VB");
//        put("V", "VBZ");
//        put("VBG", "VBG");
//        put("ADV", "RB");
//        put("ADV", "RBR");
//        put("ADV", "RBS");
//        put("DET", "DT");
//        put("DET", "PDT");
//        put("INPUNC", ",");
//        put("INPUNC", ":");
//        put("INPUNC", "LS");
//        put("INPUNC", "SYM");
//        put("INPUNC", "UH");
//        put("N", "EX");
//        put("N", "FW");
//        put("N", "NN");
//        put("N", "NNP");
//        put("N", "NNPS");
//        put("N", "NNS");
//        put("N", "PRP");
//        put("PREP", "IN");
//        put("RPUNC", "-RRB-");
//        put("RPUNC", "''");
//        put("W", "WDT");
//        put("W", "WP$");
//        put("W", "WP");
//        put("W", "WRB");
//        put("VBN", "VBN");
        return reducedTagSet;
    }
}
