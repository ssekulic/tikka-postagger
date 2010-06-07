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
import java.util.HashSet;

/**
 *
 * @author tsmoon
 */
public class PennTags extends TagMap {

    protected final HashSet<String> pennContentTagSet = new HashSet<String>(Arrays.asList(
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
    protected final HashSet<String> pennFunctionTagSet = new HashSet<String>(Arrays.asList(
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
    protected final HashSet<String> pennFullTagSet = new HashSet<String>(Arrays.asList(
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

    public PennTags(int _modelTagSize) {
        super(_modelTagSize);
        contentTagSet = pennContentTagSet;
        functionTagSet = pennFunctionTagSet;
        fullTagSet = pennFullTagSet;
        initialize(_modelTagSize);
    }

    @Override
    protected HashSet<String> reduceTag() {
        throw new UnsupportedOperationException();
    }
}
