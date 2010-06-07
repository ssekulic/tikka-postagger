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
 * Class for handling mappings from gold tag sets and their indexes. It also
 * manages the reduced tag set and their mappings.
 * 
 * @author tsmoon
 */
public abstract class EnglishTagMap extends TagMap {

    /**
     * 
     * @param _modelTagSize
     */
    public EnglishTagMap(int _modelTagSize) {
        super(_modelTagSize);
        oneToOneTagMap = new HashMap<Integer, Integer>();
        manyToOneTagMap = new HashMap<Integer, Integer>();
        for (int i = 0; i < _modelTagSize; ++i) {
            oneToOneTagMap.put(i, -1);
            manyToOneTagMap.put(i, -1);
        }
        goldToModelTagMap = new HashMap<Integer, Integer>();
        setTags();
        reduceTag();
    }

    /**
     * 
     * @param _tagSet
     */
    protected void setIdxMap(HashSet<String> _tagSet,
          HashMap<Integer, String> _idxToTag) {
        int idx = 0;
        for (String tag : _tagSet) {
            _idxToTag.put(idx, tag);
            put(tag, idx++);
        }
    }

    /**
     *
     */
    protected void reset() {
        clear();
        idxToFullTag = new HashMap<Integer, String>();
        idxToReducedTag = new HashMap<Integer, String>();
    }

    /**
     *
     * @return
     */
    protected HashSet<String> setBrownTags() {

        contentTagSet = new HashSet<String>(Arrays.asList(
              "FW", //foreign word (hyphenated before regular tag)
              "JJ", //adjective
              "JJR", //comparative adjective
              "JJS", //semantically superlative adjective (chief, top)
              "JJT", //morphologically superlative adjective (biggest)
              "NN", //singular or mass noun
              "NN$", //possessive singular noun
              "NNS", //plural noun
              "NNS$", //possessive plural noun
              "NP", //proper noun or part of name phrase
              "NP$", //possessive proper noun
              "NPS", //plural proper noun
              "NPS$", //possessive plural proper noun
              "NR", //adverbial noun (home, today, west)
              "QL", //qualifier (very, fairly)
              "QLP", //post-qualifier (enough, indeed)
              "RB", //adverb
              "RBR", //comparative adverb
              "RBT", //superlative adverb
              "RN", //nominal adverb (here, then, indoors)
              "VB", //verb, base form
              "VBD", //verb, past tense
              "VBG", //verb, present participle/gerund
              "VBN", //verb, past participle
              "VBZ" //verb, 3rd. singular present
              ));

        functionTagSet = new HashSet<String>(Arrays.asList(
              ".", //sentence closer (. ; ? *)
              "(", //left paren
              ")", //right paren
              "*", //not, n't
              //              "--", //dash
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
              "HV", //have
              "HVD", //had (past tense)
              "HVG", //having
              "HVN", //had (past participle)
              "IN", //preposition
              "MD", //modal auxiliary (can, should, will)
              //              "NC", //cited word (hyphenated after regular tag)
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
              "RP", //adverb/particle (about, off, up)
              "TO", //infinitive marker to
              "UH", //interjection, exclamation
              "WDT", //wh- determiner (what, which)
              "WP$", //possessive wh- pronoun (whose)
              "WPO", //objective wh- pronoun (whom, which, that)
              "WPS", //nominative wh- pronoun (who, which, that)
              "WQL", //wh- qualifier (how)
              "WRB" //wh- adverb (how, where, when)
              ));

        fullTagSet = new HashSet<String>(Arrays.asList(
              ".", //sentence closer (. ; ? *)
              "(", //left paren
              ")", //right paren
              "*", //not, n't
              //              "--", //dash
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
              //              "NC", //cited word (hyphenated after regular tag)
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

        contentTagSet = new HashSet<String>(Arrays.asList(
              "FW", //Foreign word
              "JJ", //Adjective
              "JJR", //Adjective, comparative
              "JJS", //Adjective, superlative
              "NN", //Noun, singular or mass
              "NNS", //Noun, plural
              "NNP", //Proper noun, singular
              "NNPS", //Proper noun, plural
              "RB", //Adverb
              "RBR", //Adverb, comparative
              "RBS", //Adverb, superlative
              "VB", //Verb, base form
              "VBD", //Verb, past tense
              "VBG", //Verb, gerund or present participle
              "VBN", //Verb, past participle
              "VBP", //Verb, non-3rd person singular present
              "VBZ" //Verb, 3rd person singular present
              ));

        functionTagSet = new HashSet<String>(Arrays.asList(
              //              "$", //dollar $ -$ --$ A$ C$ HK$ M$ NZ$ S$ U.S.$ US$
              "``", //opening quotation mark ` ``
              "''", //closing quotation mark ' ''
              //              "(", //opening parenthesis ( [ {
              //              ")", //closing parenthesis ) ] }
              ",", //comma ,
              //              "-- ", //dash --
              ".", //sentence terminator . ! ?
              ":", //colon or ellipsis : ; ...
              "-RRB-", // Right braces/brackets
              "-LRB-", // Left braces/brackets
              "CC", //Coordinating conjunction
              "CD", //Cardinal number
              "DT", //Determiner
              "EX", //Existential there
              "IN", //Preposition or subordinating conjunction
              "LS", //List item marker
              "MD", //Modal
              "PDT", //Predeterminer
              "POS", //Possessive ending
              "PRP", //Personal pronoun
              "PRP$", //Possessive pronoun
              "RP", //Particle
              "SYM", //Symbol
              "TO", //to
              "UH", //Interjection
              "WDT", //Wh-determiner
              "WP", //Wh-pronoun
              "WP$", //Possessive wh-pronoun
              "WRB" //Wh-adverb
              ));

        fullTagSet = new HashSet<String>(Arrays.asList(
              //              "$", //dollar $ -$ --$ A$ C$ HK$ M$ NZ$ S$ U.S.$ US$
              "``", //opening quotation mark ` ``
              "''", //closing quotation mark ' ''
              //              "(", //opening parenthesis ( [ {
              //              ")", //closing parenthesis ) ] }
              ",", //comma ,
              //              "-- ", //dash --
              ".", //sentence terminator . ! ?
              ":", //colon or ellipsis : ; ...
              "-RRB-", // Right braces/brackets
              "-LRB-", // Left braces/brackets
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
              "NNP", //Proper noun, singular
              "NNPS", //Proper noun, plural
              "PDT", //Predeterminer
              "POS", //Possessive ending
              "PRP", //Personal pronoun
              "PRP$", //Possessive pronoun
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

        idxToReducedTag = idxToFullTag = new HashMap<Integer, String>();
        setIdxMap(fullTagSet, idxToFullTag);
        idxToContentTag = new HashMap<Integer, String>();
        idxToFunctionTag = new HashMap<Integer, String>();
        int idx = 0;
        for (String tag : contentTagSet) {
            idxToContentTag.put(idx, tag);
            idxToFullTag.put(idx, tag);
            idx += 1;
        }

        for (String tag : functionTagSet) {
            idxToFunctionTag.put(idx, tag);
            idxToFullTag.put(idx, tag);
            idx += 1;
        }

        return fullTagSet;
    }

    /**
     * If the tag exists
     * @param tag
     * @return
     */
    public String getReducedTag(String tag) {
        String rtag = null;
        if (fullTagSet.contains(tag)) {
            rtag = fullTagToReducedTag.get(tag);
        }

        return rtag;
    }
}
