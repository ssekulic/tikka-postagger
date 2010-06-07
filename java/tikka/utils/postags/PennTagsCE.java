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

import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author tsmoon
 */
public class PennTagsCE extends PennTags {

    public PennTagsCE(int _modelTagSize) {
        super(_modelTagSize);
        reduceTag();
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
        
        idxToReducedTag = new HashMap<Integer, String>();
        setIdxMap(reducedTagSet, idxToReducedTag);
        return reducedTagSet;
    }
}
