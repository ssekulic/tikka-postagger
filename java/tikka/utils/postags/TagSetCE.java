///////////////////////////////////////////////////////////////////////////////
// To change this template, choose Tools | Templates
// and open the template in the editor.
///////////////////////////////////////////////////////////////////////////////
package tikka.utils.postags;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * TagMap for handling reduced tag set used in Noah Smith's Contrastive
 * Estimation (2005) paper
 *
 * @author tsmoon
 */
public abstract class TagSetCE extends TagMap {

    public TagSetCE(int modelTagSize) {
        super(modelTagSize);
    }

    /**
     *
     * @return
     */
    @Override
    protected HashSet<String> reduceTag() {

        fullTagToReducedTag = new HashMap<String, String>();
        for (String tag : fullTagSet) {
            fullTagToReducedTag.put(tag, tag);
        }

        reducedTagSet = new HashSet<String>(Arrays.asList(
              "ADJ", //CD JJ JJR JJS PRP$
              "CONJ",//CC
              "ENDPUNC",//.
              "LPUNC",//“ -LRB
              "POS",//POS
              "PRT",//RP
              "TO",//TO
              "V",//MD VBD VBP VB VBZ
              "VBG",//VBG
              "ADV",//RB RBR RBS
              "DET",//DT PDT
              "INPUNC",//,: LS SYM UH
              "N",//EX FW NN NNP NNPS NNS PRP
              "PREP",//IN
              "RPUNC",//” -RRB-
              "W",//WDT WP$ WP WRB
              "VBN"//VBN
              ));
        return reducedTagSet;
    }
}
