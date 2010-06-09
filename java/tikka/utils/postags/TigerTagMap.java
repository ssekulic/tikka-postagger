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
 *
 * @author Taesun Moon <tsunmoon@gmail.com>
 */
public class TigerTagMap extends TagMap {

    protected final HashSet<String> tigerContentTagSet = new HashSet<String>(Arrays.asList(
          "ADJA", // adjective, attributive
          "ADJD", // adjective, adverbial or predicative
          "ADV", // adverb
          "FM", // foreign language material
          "NE", // proper noun
          "NN", // common noun
          "NNE", //
          "VVFIN", // finite verb, full
          "VVIMP", // imperative, full
          "VVINF", // infinitive, full
          "VVIZU", // Infinitive with zu, full
          "VVPP" // perfect participle, full
          ));
    protected final HashSet<String> tigerFunctionTagSet = new HashSet<String>(Arrays.asList(
          "$(",
          "$,",
          "$.",
          "APPO", // postposition
          "APPR", // preposition; circumposition left
          "APPRART", // preposition with article
          "APZR", // circumposition right
          "ART", // definite or indefinite article
          "CARD", // cardinal number
          "ITJ", // interjection
          "KOKOM", // comparative conjunction
          "KON", // coordinate conjunction
          "KOUI", // subordinate conjunction with zu and infinitive
          "KOUS", // subordinate conjunction with sentence
          "PDAT", // attributive demonstrative pronoun
          "PDS", // substituting demonstrative pronoun
          "PIAT", // attributive indefinite pronoun without determiner
          "PIS", // substituting indefinite pronoun
          "PPER", // non-reflexive personal pronoun
          "PPOSAT", // attributive possessive pronoun
          "PPOSS", // substituting possessive pronoun
          "PRELAT", // attributive relative pronoun
          "PRELS", // substituting relative pronoun
          "PRF", // reflexive personal pronoun
          "PTKA", // particle with adjective or adverb
          "PTKANT", // answer particle
          "PTKNEG", // negative particle
          "PTKVZ", // separable verbal particle
          "PTKZU", // zu before infinitive
          "PWAT", // attributive interrogative pronoun
          "PWAV", // adverbial interrogative or relative pronoun
          "PWS", // substituting interrogative pronoun
          "TRUNC", // word remnant
          "VAFIN", // finite verb, auxiliary
          "VAIMP", // imperative, auxiliary
          "VAINF", // infinitive, auxiliary
          "VAPP", // perfect participle, auxiliary
          "VMFIN", // finite verb, modal
          "VMINF", // infinitive, modal
          "VMPP", // perfect participle, modal
          "XY" // non-word containing non-letter
          ));
    protected final HashSet<String> tigerFullTagSet = new HashSet<String>(Arrays.asList(
          "$(", // other sentence-internal punctuation mark
          "$,", // comma
          "$.", // sentence-final punctuation mark
          "ADJA", // adjective, attributive
          "ADJD", // adjective, adverbial or predicative
          "ADV", // adverb
          "APPO", // postposition
          "APPR", // preposition; circumposition left
          "APPRART", // preposition with article
          "APZR", // circumposition right
          "ART", // definite or indefinite article
          "CARD", // cardinal number
          "FM", // foreign language material
          "ITJ", // interjection
          "KOKOM", // comparative conjunction
          "KON", // coordinate conjunction
          "KOUI", // subordinate conjunction with zu and infinitive
          "KOUS", // subordinate conjunction with sentence
          "NE", // proper noun
          "NN", // common noun
          "NNE", // seems to be a variant of proper nouns, composites?
          // "PAV",       // pronominal adverb
          "PDAT", // attributive demonstrative pronoun
          "PDS", // substituting demonstrative pronoun
          "PIAT", // attributive indefinite pronoun without determiner
          // "PIDAT",     // attributive indefinite pronoun with determiner
          "PIS", // substituting indefinite pronoun
          "PPER", // non-reflexive personal pronoun
          "PPOSAT", // attributive possessive pronoun
          "PPOSS", // substituting possessive pronoun
          "PRELAT", // attributive relative pronoun
          "PRELS", // substituting relative pronoun
          "PRF", // reflexive personal pronoun
          "PTKA", // particle with adjective or adverb
          "PTKANT", // answer particle
          "PTKNEG", // negative particle
          "PTKVZ", // separable verbal particle
          "PTKZU", // zu before infinitive
          "PWAT", // attributive interrogative pronoun
          "PWAV", // adverbial interrogative or relative pronoun
          "PWS", // substituting interrogative pronoun
          // "SGML",      // "SGML",      // markup
          // "SPELL",     // letter sequence
          "TRUNC", // word remnant
          "VAFIN", // finite verb, auxiliary
          "VAIMP", // imperative, auxiliary
          "VAINF", // infinitive, auxiliary
          "VAPP", // perfect participle, auxiliary
          "VMFIN", // finite verb, modal
          "VMINF", // infinitive, modal
          "VMPP", // perfect participle, modal
          "VVFIN", // finite verb, full
          "VVIMP", // imperative, full
          "VVINF", // infinitive, full
          "VVIZU", // Infinitive with zu, full
          "VVPP", // perfect participle, full
          "XY" // non-word containing non-letter
          ));
    protected final HashSet<String> tigerIgnoreSet = new HashSet<String>(Arrays.asList(
          "PAV",       // pronominal adverb
          "PIDAT",     // attributive indefinite pronoun with determiner
          "SGML",      // "SGML",      // markup
          "SPELL"     // letter sequence
          ));

    /**
     *
     * @param _modelTagSize
     */
    public TigerTagMap(int _modelTagSize) {
        super(_modelTagSize);
        contentTagSet = tigerContentTagSet;
        functionTagSet = tigerFunctionTagSet;
        fullTagSet = tigerFullTagSet;
        initialize(_modelTagSize);
        reduceTag();
    }

    @Override
    protected HashSet<String> reduceTag() {
        fullTagToReducedTag.put("$(", "");
        fullTagToReducedTag.put("$,", "INPUNC");
        fullTagToReducedTag.put("$.", "ENDPUNC");
        fullTagToReducedTag.put("ADJA", "ADJ");
        fullTagToReducedTag.put("ADJD", "ADJ");
        fullTagToReducedTag.put("ADV", "ADV");
        fullTagToReducedTag.put("APPO", "PREP");
        fullTagToReducedTag.put("APPR", "PREP");
        fullTagToReducedTag.put("APPRART", "PREP");
        fullTagToReducedTag.put("APZR", "PREP");
        fullTagToReducedTag.put("ART", "DET");
        fullTagToReducedTag.put("CARD", "ADJ");
        fullTagToReducedTag.put("FM", "N");
        fullTagToReducedTag.put("ITJ", "INPUNC");
        fullTagToReducedTag.put("KOKOM", "CONJ");
        fullTagToReducedTag.put("KON", "CONJ");
        fullTagToReducedTag.put("KOUI", "PREP");
        fullTagToReducedTag.put("KOUS", "PREP");
        fullTagToReducedTag.put("NE", "N");
        fullTagToReducedTag.put("NN", "N");
        fullTagToReducedTag.put("NNE", "N");
        fullTagToReducedTag.put("PDAT", "N");
        fullTagToReducedTag.put("PDS", "N");
        fullTagToReducedTag.put("PIAT", "N");
        fullTagToReducedTag.put("PIS", "N");
        fullTagToReducedTag.put("PPER", "N");
        fullTagToReducedTag.put("PPOSAT", "ADJ");
        fullTagToReducedTag.put("PPOSS", "ADJ");
        fullTagToReducedTag.put("PRELAT", "IN");
        fullTagToReducedTag.put("PRELS", "N");
        fullTagToReducedTag.put("PRF", "N");
        fullTagToReducedTag.put("PTKA", "PREP");
        fullTagToReducedTag.put("PTKANT", "INPUNC");
        fullTagToReducedTag.put("PTKNEG", "ADV");
        fullTagToReducedTag.put("PTKVZ", "");
        fullTagToReducedTag.put("PTKZU", "TO");
        fullTagToReducedTag.put("PWAT", "W");
        fullTagToReducedTag.put("PWAV", "W");
        fullTagToReducedTag.put("PWS", "W");
        fullTagToReducedTag.put("TRUNC", "");
        fullTagToReducedTag.put("VAFIN", "V");
        fullTagToReducedTag.put("VAIMP", "V");
        fullTagToReducedTag.put("VAINF", "V");
        fullTagToReducedTag.put("VAPP", "V");
        fullTagToReducedTag.put("VMFIN", "V");
        fullTagToReducedTag.put("VMINF", "V");
        fullTagToReducedTag.put("VMPP", "V");
        fullTagToReducedTag.put("VVFIN", "V");
        fullTagToReducedTag.put("VVIMP", "V");
        fullTagToReducedTag.put("VVINF", "V");
        fullTagToReducedTag.put("VVIZU", "V");
        fullTagToReducedTag.put("VVPP", "V");
        fullTagToReducedTag.put("XY", "");

        reducedTagSet.remove("POS");
        reducedTagSet.remove("LPUNC");
        reducedTagSet.remove("RPUNC");
        
        idxToReducedTag = new HashMap<Integer, String>();
        setIdxMap(reducedTagSet, idxToReducedTag);
        return reducedTagSet;
    }
}
