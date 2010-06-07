///////////////////////////////////////////////////////////////////////////////
//  Copyright (C) 2010 Taesun Moon <tsunmoon@gmail.com>
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
///////////////////////////////////////////////////////////////////////////////
package tikka.utils.postags;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * EnglishTagMap for handling reduced tag set used in Noah Smith's Contrastive
 * Estimation (2005) paper
 *
 * @author tsmoon
 */
public abstract class EnglishTagSetCE extends EnglishTagMap {

    public EnglishTagSetCE(int modelTagSize) {
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
