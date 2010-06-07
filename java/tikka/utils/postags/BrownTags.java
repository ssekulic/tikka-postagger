///////////////////////////////////////////////////////////////////////////////
//  Copyright (C) 2010 Taesun Moon, The University of Texas at Austin
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 3 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
///////////////////////////////////////////////////////////////////////////////
package tikka.utils.postags;

import java.util.Arrays;
import java.util.HashSet;

/**
 * EnglishTagMap for handling full Brown corpus tagset. Does not reduce any of the tags.
 * 
 * @author tsmoon
 */
public class BrownTags extends TagMap {

    protected final HashSet<String> brownContentTagSet = new HashSet<String>(Arrays.asList(
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
    protected final HashSet<String> brownFunctionTagSet = new HashSet<String>(Arrays.asList(
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
    protected final HashSet<String> brownFullTagSet = new HashSet<String>(Arrays.asList(
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

    public BrownTags(int _modelTagSize) {
        super(_modelTagSize);
        contentTagSet = brownContentTagSet;
        functionTagSet = brownFunctionTagSet;
        fullTagSet = brownFullTagSet;
        initialize(_modelTagSize);
    }

    @Override
    public String getReducedTag(String tag) {
        String[] tags = tag.split("[+\\-]");
        return super.getReducedTag(tags[0]);
    }

    @Override
    protected HashSet<String> reduceTag() {
        throw new UnsupportedOperationException();
    }
}
