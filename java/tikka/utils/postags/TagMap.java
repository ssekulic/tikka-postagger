///////////////////////////////////////////////////////////////////////////////
// To change this template, choose Tools | Templates
// and open the template in the editor.
///////////////////////////////////////////////////////////////////////////////
package tikka.utils.postags;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class for handling mappings from gold tag sets and their indexes. It also
 * manages the reduced tag set and their mappings.
 * 
 * @author tsmoon
 */
public abstract class TagMap extends HashMap<String, Integer> {

    protected HashSet<String> fullTagSet;
    protected HashSet<String> reducedTagSet;
    protected HashMap<Integer, String> idxToTag;
    protected HashMap<String, String> fullTagToReducedTag;
    protected HashMap<Integer, Integer> oneToOneTagMap;
    protected HashMap<Integer, Integer> manyToOneTagMap;
    protected HashMap<Integer, Integer> goldToModelTagMap;

    /**
     * 
     * @param modelTagSize
     */
    public TagMap(int modelTagSize) {
        oneToOneTagMap = new HashMap<Integer, Integer>();
        manyToOneTagMap = new HashMap<Integer, Integer>();
        for (int i = 0; i < modelTagSize; ++i) {
            oneToOneTagMap.put(i, -1);
            manyToOneTagMap.put(i, -1);
        }
        goldToModelTagMap = new HashMap<Integer, Integer>();
        setTags();
        reduceTag();
    }

    /**
     * 
     * @param tagSet
     */
    protected void setIdxMap(HashSet<String> tagSet) {
        idxToTag = new HashMap<Integer, String>();
        int idx = 0;
        for (String tag : tagSet) {
            idxToTag.put(idx, tag);
            put(tag, idx++);
        }
    }

    /**
     *
     */
    protected void reset() {
        clear();
        idxToTag = new HashMap<Integer, String>();
    }

    /**
     *
     * @return
     */
    protected HashSet<String> setBrownTags() {
        fullTagSet = new HashSet<String>(Arrays.asList(
              ".", //sentence closer (. ; ? *)
              "(", //left paren
              ")", //right paren
              "*", //not, n't
              "--", //dash
              ",", //comma
              ":", //colon
              "ABL", //pre-qualifier (quite, rather)
              "ABN", //pre-quantifier (half, all)
              "ABX", //pre-quantifier (both)
              "AP", //post-determiner (many, several, next)
              "AT", //article (a, the, no)
              "BE", //be
              "BED", //were
              "BEDZ", //was
              "BEG", //being
              "BEM", //am
              "BEN", //been
              "BER", //are, art
              "BEZ", //is
              "CC", //coordinating conjunction (and, or)
              "CD", //cardinal numeral (one, two, 2, etc.)
              "CS", //subordinating conjunction (if, although)
              "DO", //do
              "DOD", //did
              "DOZ", //does
              "DT", //singular determiner/quantifier (this, that)
              "DTI", //singular or plural determiner/quantifier (some, any)
              "DTS", //plural determiner (these, those)
              "DTX", //determiner/double conjunction (either)
              "EX", //existential there
              "FW", //foreign word (hyphenated before regular tag)
              "HV", //have
              "HVD", //had (past tense)
              "HVG", //having
              "HVN", //had (past participle)
              "IN", //preposition
              "JJ", //adjective
              "JJR", //comparative adjective
              "JJS", //semantically superlative adjective (chief, top)
              "JJT", //morphologically superlative adjective (biggest)
              "MD", //modal auxiliary (can, should, will)
              "NC", //cited word (hyphenated after regular tag)
              "NN", //singular or mass noun
              "NN$", //possessive singular noun
              "NNS", //plural noun
              "NNS$", //possessive plural noun
              "NP", //proper noun or part of name phrase
              "NP$", //possessive proper noun
              "NPS", //plural proper noun
              "NPS$", //possessive plural proper noun
              "NR", //adverbial noun (home, today, west)
              "OD", //ordinal numeral (first, 2nd)
              "PN", //nominal pronoun (everybody, nothing)
              "PN$", //possessive nominal pronoun
              "PP$", //possessive personal pronoun (my, our)
              "PP$$", //second (nominal) possessive pronoun (mine, ours)
              "PPL", //singular reflexive/intensive personal pronoun (myself)
              "PPLS", //plural reflexive/intensive personal pronoun (ourselves)
              "PPO", //objective personal pronoun (me, him, it, them)
              "PPS", //3rd. singular nominative pronoun (he, she, it, one)
              "PPSS", //other nominative personal pronoun (I, we, they, you)
              "QL", //qualifier (very, fairly)
              "QLP", //post-qualifier (enough, indeed)
              "RB", //adverb
              "RBR", //comparative adverb
              "RBT", //superlative adverb
              "RN", //nominal adverb (here, then, indoors)
              "RP", //adverb/particle (about, off, up)
              "TO", //infinitive marker to
              "UH", //interjection, exclamation
              "VB", //verb, base form
              "VBD", //verb, past tense
              "VBG", //verb, present participle/gerund
              "VBN", //verb, past participle
              "VBZ", //verb, 3rd. singular present
              "WDT", //wh- determiner (what, which)
              "WP$", //possessive wh- pronoun (whose)
              "WPO", //objective wh- pronoun (whom, which, that)
              "WPS", //nominative wh- pronoun (who, which, that)
              "WQL", //wh- qualifier (how)
              "WRB" //wh- adverb (how, where, when)
              ));

        return fullTagSet;
    }

    /**
     * 
     * @return
     */
    protected HashSet<String> setPennTags() {
        fullTagSet = new HashSet<String>(Arrays.asList(
              "$", //dollar $ -$ --$ A$ C$ HK$ M$ NZ$ S$ U.S.$ US$
              "``", //opening quotation mark ` ``
              "''", //closing quotation mark ' ''
              "(", //opening parenthesis ( [ {
              ")", //closing parenthesis ) ] }
              ",", //comma ,
              "-- ", //dash --
              ".", //sentence terminator . ! ?
              ":", //colon or ellipsis : ; ...
              "CC", //Coordinating conjunction
              "CD", //Cardinal number
              "DT", //Determiner
              "EX", //Existential there
              "FW", //Foreign word
              "IN", //Preposition or subordinating conjunction
              "JJ", //Adjective
              "JJR", //Adjective, comparative
              "JJS", //Adjective, superlative
              "LS", //List item marker
              "MD", //Modal
              "NN", //Noun, singular or mass
              "NNS", //Noun, plural
              "NP", //Proper noun, singular
              "NPS", //Proper noun, plural
              "PDT", //Predeterminer
              "POS", //Possessive ending
              "PP", //Personal pronoun
              "PP$", //Possessive pronoun
              "RB", //Adverb
              "RBR", //Adverb, comparative
              "RBS", //Adverb, superlative
              "RP", //Particle
              "SYM", //Symbol
              "TO", //to
              "UH", //Interjection
              "VB", //Verb, base form
              "VBD", //Verb, past tense
              "VBG", //Verb, gerund or present participle
              "VBN", //Verb, past participle
              "VBP", //Verb, non-3rd person singular present
              "VBZ", //Verb, 3rd person singular present
              "WDT", //Wh-determiner
              "WP", //Wh-pronoun
              "WP$", //Possessive wh-pronoun
              "WRB" //Wh-adverb
              ));
        return fullTagSet;
    }

    /**
     *
     * @return
     */
    protected abstract HashSet<String> setTags();

    /**
     *
     * @return
     */
    protected HashSet<String> reduceTag() {
        reducedTagSet = fullTagSet;
        fullTagToReducedTag = new HashMap<String, String>();
        for (String tag : fullTagSet) {
            fullTagToReducedTag.put(tag, tag);
        }

        setIdxMap(reducedTagSet);
        return reducedTagSet;
    }

    /**
     * If the tag exists
     * @param tag
     * @return
     */
    public String getReducedTag(String tag) {
        String rtag = "";
        if (fullTagSet.contains(tag)) {
            rtag = fullTagToReducedTag.get(tag);
        }

        return rtag;
    }

    /**
     * 
     * @param tag
     * @return
     */
    public String getFullTag(String tag) {
        if (fullTagSet.contains(tag)) {
            return tag;
        } else {
            return "";
        }
    }

    public int getReducedGoldTagSize() {
        return reducedTagSet.size();
    }

    public int getFullGoldTagSize() {
        return fullTagSet.size();
    }

    public int getModelTagSize() {
        return oneToOneTagMap.size();
    }
}
