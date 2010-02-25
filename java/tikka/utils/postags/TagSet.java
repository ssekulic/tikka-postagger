///////////////////////////////////////////////////////////////////////////////
// To change this template, choose Tools | Templates
// and open the template in the editor.
///////////////////////////////////////////////////////////////////////////////
package tikka.utils.postags;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class for handling mappings from and to gold tag sets
 * 
 * @author tsmoon
 */
public abstract class TagSet extends HashMap<String, String> {

    protected HashSet<String> tagSet;
    protected HashSet<String> reducedTagSet;

    public TagSet() {
        setTags();
    }

    protected HashSet<String> setBrownTags() {
        tagSet = new HashSet<String>(Arrays.asList(
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
        return tagSet;
    }

    protected HashSet<String> setPennTags() {
        tagSet = new HashSet<String>(Arrays.asList(
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
        return tagSet;
    }

    public abstract HashSet<String> setTags();

    public abstract HashSet<String> reduceTag();
}
