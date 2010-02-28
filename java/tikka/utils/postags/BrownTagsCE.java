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
public class BrownTagsCE extends TagSetCE {

    public BrownTagsCE(int modelTagSize) {
        super(modelTagSize);
    }

    @Override
    protected HashSet<String> setTags() {
        return setBrownTags();
    }

    @Override
    protected HashSet<String> reduceTag() {
        super.reduceTag();

        fullTagToReducedTag.put(".", "ENDPUNC"); //sentence closer (. ; ? *)
        fullTagToReducedTag.put("(", "LPUNC"); //left paren
        fullTagToReducedTag.put(")", "RPUNC"); //right paren
        fullTagToReducedTag.put("*", "ADV"); //not, n't
        fullTagToReducedTag.put(",", "INPUNC"); //comma
        fullTagToReducedTag.put(":", "INPUNC"); //colon
        fullTagToReducedTag.put("ABL", "ADV"); //pre-qualifier (quite, rather)
        fullTagToReducedTag.put("ABN", "DET"); //pre-quantifier (half, all)
        fullTagToReducedTag.put("ABX", "DET"); //pre-quantifier (both)
        fullTagToReducedTag.put("AP", "ADJ"); //post-determiner (many, several, next)
        fullTagToReducedTag.put("AT", "DET"); //article (a, the, no)
        fullTagToReducedTag.put("BE", "V"); //be
        fullTagToReducedTag.put("BED", "V"); //were
        fullTagToReducedTag.put("BEDZ", "V"); //was
        fullTagToReducedTag.put("BEG", "VBG"); //being
        fullTagToReducedTag.put("BEM", "V"); //am
        fullTagToReducedTag.put("BEN", "VBN"); //been
        fullTagToReducedTag.put("BER", "V"); //are, art
        fullTagToReducedTag.put("BEZ", "V"); //is
        fullTagToReducedTag.put("CC", "CONJ"); //coordinating conjunction (and, or)
        fullTagToReducedTag.put("CD", "ADJ"); //cardinal numeral (one, two, 2, etc.)
        fullTagToReducedTag.put("CS", "PREP"); //subordinating conjunction (if, although)
        fullTagToReducedTag.put("DO", "V"); //do
        fullTagToReducedTag.put("DOD", "V"); //did
        fullTagToReducedTag.put("DOZ", "V"); //does
        fullTagToReducedTag.put("DT", "DET"); //singular determiner/quantifier (this, that)
        fullTagToReducedTag.put("DTI", "DET"); //singular or plural determiner/quantifier (some, any)
        fullTagToReducedTag.put("DTS", "DET"); //plural determiner (these, those)
        fullTagToReducedTag.put("DTX", "DET"); //determiner/double conjunction (either)
        fullTagToReducedTag.put("EX", "N"); //existential there
        fullTagToReducedTag.put("FW", "N"); //foreign word (hyphenated before regular tag)
        fullTagToReducedTag.put("HV", "V"); //have
        fullTagToReducedTag.put("HVD", "V"); //had (past tense)
        fullTagToReducedTag.put("HVG", "VBG"); //having
        fullTagToReducedTag.put("HVN", "V"); //had (past participle)
        fullTagToReducedTag.put("IN", "PREP"); //preposition
        fullTagToReducedTag.put("JJ", "ADJ"); //adjective
        fullTagToReducedTag.put("JJR", "ADJ"); //comparative adjective
        fullTagToReducedTag.put("JJS", "ADJ"); //semantically superlative adjective (chief, top)
        fullTagToReducedTag.put("JJT", "ADJ"); //morphologically superlative adjective (biggest)
        fullTagToReducedTag.put("MD", "V"); //modal auxiliary (can, should, will)
        fullTagToReducedTag.put("NN", "N"); //singular or mass noun
        fullTagToReducedTag.put("NN$", "ADJ"); //possessive singular noun
        fullTagToReducedTag.put("NNS", "N"); //plural noun
        fullTagToReducedTag.put("NNS$", "ADJ"); //possessive plural noun
        fullTagToReducedTag.put("NP", "N"); //proper noun or part of name phrase
        fullTagToReducedTag.put("NP$", "ADJ"); //possessive proper noun
        fullTagToReducedTag.put("NPS", "N"); //plural proper noun
        fullTagToReducedTag.put("NPS$", "ADJ"); //possessive plural proper noun
        fullTagToReducedTag.put("NR", "N"); //adverbial noun (home, today, west)
        fullTagToReducedTag.put("OD", "ADJ"); //ordinal numeral (first, 2nd)
        fullTagToReducedTag.put("PN", "N"); //nominal pronoun (everybody, nothing)
        fullTagToReducedTag.put("PN$", "ADJ"); //possessive nominal pronoun
        fullTagToReducedTag.put("PP$", "ADJ"); //possessive personal pronoun (my, our)
        fullTagToReducedTag.put("PP$$", "ADJ"); //second (nominal) possessive pronoun (mine, ours)
        fullTagToReducedTag.put("PPL", "N"); //singular reflexive/intensive personal pronoun (myself)
        fullTagToReducedTag.put("PPLS", "N"); //plural reflexive/intensive personal pronoun (ourselves)
        fullTagToReducedTag.put("PPO", "N"); //objective personal pronoun (me, him, it, them)
        fullTagToReducedTag.put("PPS", "N"); //3rd. singular nominative pronoun (he, she, it, one)
        fullTagToReducedTag.put("PPSS", "N"); //other nominative personal pronoun (I, we, they, you)
        fullTagToReducedTag.put("QL", "ADV"); //qualifier (very, fairly)
        fullTagToReducedTag.put("QLP", "ADV"); //post-qualifier (enough, indeed)
        fullTagToReducedTag.put("RB", "ADV"); //adverb
        fullTagToReducedTag.put("RBR", "ADV"); //comparative adverb
        fullTagToReducedTag.put("RBT", "ADV"); //superlative adverb
        fullTagToReducedTag.put("RN", "ADV"); //nominal adverb (here, then, indoors)
        fullTagToReducedTag.put("RP", "PRT"); //adverb/particle (about, off, up)
        fullTagToReducedTag.put("TO", "TO"); //infinitive marker to
        fullTagToReducedTag.put("UH", "INPUNC"); //interjection, exclamation
        fullTagToReducedTag.put("VB", "V"); //verb, base form
        fullTagToReducedTag.put("VBD", "V"); //verb, past tense
        fullTagToReducedTag.put("VBG", "VBG"); //verb, present participle/gerund
        fullTagToReducedTag.put("VBN", "VBN"); //verb, past participle
        fullTagToReducedTag.put("VBZ", "V"); //verb, 3rd. singular present
        fullTagToReducedTag.put("WDT", "W"); //wh- determiner (what, which)
        fullTagToReducedTag.put("WP$", "W"); //possessive wh- pronoun (whose)
        fullTagToReducedTag.put("WPO", "W"); //objective wh- pronoun (whom, which, that)
        fullTagToReducedTag.put("WPS", "W"); //nominative wh- pronoun (who, which, that)
        fullTagToReducedTag.put("WQL", "W"); //wh- qualifier (how)
        fullTagToReducedTag.put("WRB", "W"); //wh- adverb (how, where, when)

        /**
         * The reduced tag set for brown does not use the POS label
         */
        reducedTagSet.remove("POS");
        setIdxMap(reducedTagSet);
        return reducedTagSet;
    }

    @Override
    public String getReducedTag(String tag) {
        String[] tags = tag.split("[+\\-]");
        return super.getReducedTag(tags[0]);
    }
}
