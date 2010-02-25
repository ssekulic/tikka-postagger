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

    @Override
    public HashSet<String> setTags() {
        return setBrownTags();
    }

    @Override
    public HashSet<String> reduceTag() {
        put(".", "ENDPUNC"); //sentence closer (. ; ? *)
        put("(", "LPUNC"); //left paren
        put(")", "RPUNC"); //right paren
        put("*", "ADV"); //not, n't
        put(",", "INPUNC"); //comma
        put(":", "INPUNC"); //colon
        put("ABL", "ADV"); //pre-qualifier (quite, rather)
        put("ABN", "DET"); //pre-quantifier (half, all)
        put("ABX", "DET"); //pre-quantifier (both)
        put("AP", "ADJ"); //post-determiner (many, several, next)
        put("AT", "DET"); //article (a, the, no)
        put("BE", "V"); //be
        put("BED", "V"); //were
        put("BEDZ", "V"); //was
        put("BEG", "VBG"); //being
        put("BEM", "V"); //am
        put("BEN", "VBN"); //been
        put("BER", "V"); //are, art
        put("BEZ", "V"); //is
        put("CC", "CONJ"); //coordinating conjunction (and, or)
        put("CD", "ADJ"); //cardinal numeral (one, two, 2, etc.)
        put("CS", "PREP"); //subordinating conjunction (if, although)
        put("DO", "V"); //do
        put("DOD", "V"); //did
        put("DOZ", "V"); //does
        put("DT", "DET"); //singular determiner/quantifier (this, that)
        put("DTI", "DET"); //singular or plural determiner/quantifier (some, any)
        put("DTS", "DET"); //plural determiner (these, those)
        put("DTX", "DET"); //determiner/double conjunction (either)
        put("EX", "N"); //existential there
        put("FW", "N"); //foreign word (hyphenated before regular tag)
        put("HV", "V"); //have
        put("HVD", "V"); //had (past tense)
        put("HVG", "VBG"); //having
        put("HVN", "V"); //had (past participle)
        put("IN", "PREP"); //preposition
        put("JJ", "ADJ"); //adjective
        put("JJR", "ADJ"); //comparative adjective
        put("JJS", "ADJ"); //semantically superlative adjective (chief, top)
        put("JJT", "ADJ"); //morphologically superlative adjective (biggest)
        put("MD", "V"); //modal auxiliary (can, should, will)
        put("NN", "N"); //singular or mass noun
        put("NN$", "ADJ"); //possessive singular noun
        put("NNS", "N"); //plural noun
        put("NNS$", "ADJ"); //possessive plural noun
        put("NP", "N"); //proper noun or part of name phrase
        put("NP$", "ADJ"); //possessive proper noun
        put("NPS", "N"); //plural proper noun
        put("NPS$", "ADJ"); //possessive plural proper noun
        put("NR", "N"); //adverbial noun (home, today, west)
        put("OD", "ADJ"); //ordinal numeral (first, 2nd)
        put("PN", "N"); //nominal pronoun (everybody, nothing)
        put("PN$", "ADJ"); //possessive nominal pronoun
        put("PP$", "ADJ"); //possessive personal pronoun (my, our)
        put("PP$$", "ADJ"); //second (nominal) possessive pronoun (mine, ours)
        put("PPL", "N"); //singular reflexive/intensive personal pronoun (myself)
        put("PPLS", "N"); //plural reflexive/intensive personal pronoun (ourselves)
        put("PPO", "N"); //objective personal pronoun (me, him, it, them)
        put("PPS", "N"); //3rd. singular nominative pronoun (he, she, it, one)
        put("PPSS", "N"); //other nominative personal pronoun (I, we, they, you)
        put("QL", "ADV"); //qualifier (very, fairly)
        put("QLP", "ADV"); //post-qualifier (enough, indeed)
        put("RB", "ADV"); //adverb
        put("RBR", "ADV"); //comparative adverb
        put("RBT", "ADV"); //superlative adverb
        put("RN", "ADV"); //nominal adverb (here, then, indoors)
        put("RP", "PRT"); //adverb/particle (about, off, up)
        put("TO", "TO"); //infinitive marker to
        put("UH", "INPUNC"); //interjection, exclamation
        put("VB", "V"); //verb, base form
        put("VBD", "V"); //verb, past tense
        put("VBG", "VBG"); //verb, present participle/gerund
        put("VBN", "VBN"); //verb, past participle
        put("VBZ", "V"); //verb, 3rd. singular present
        put("WDT", "W"); //wh- determiner (what, which)
        put("WP$", "W"); //possessive wh- pronoun (whose)
        put("WPO", "W"); //objective wh- pronoun (whom, which, that)
        put("WPS", "W"); //nominative wh- pronoun (who, which, that)
        put("WQL", "W"); //wh- qualifier (how)
        put("WRB", "W"); //wh- adverb (how, where, when)

        return reducedTagSet;
    }
}
