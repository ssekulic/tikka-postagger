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
package tikka.utils.normalizer;

import java.util.regex.Pattern;
import tikka.utils.postags.EnglishTagMap;

/**
 *
 * @author tsmoon
 */
public class WordNormalizerToLower extends WordNormalizer {

    public WordNormalizerToLower(EnglishTagMap tagMap) {
        super(tagMap);
    }

    @Override
    public String[] normalize(String[] strings) {
        this.strings = new String[strings.length];

        try {
            reducedTag = tagMap.getReducedTag(strings[1]);

//            if (!reducedTag.isEmpty()) {
            word = strings[0].toLowerCase();
//            } else {
//                word = "";
//            }
//            pattern = Pattern.compile("^\\w.*$");
//            matcher = pattern.matcher(reducedTag);
//            if (!matcher.find()) {
//                reducedTag = "";
//            }
//            this.strings[1] = reducedTag;
//            this.strings[1] = reducedTag;
        } catch (ArrayIndexOutOfBoundsException e) {
            reducedTag = null;
            word = "";
        }

//        if (!reducedTag.isEmpty()) {
//        } else {
//        }
//
//        if (reducedTag == null || !reducedTag.isEmpty()) {
//            word = strings[0].toLowerCase();
//            pattern = Pattern.compile("^\\W*$");
//            matcher = pattern.matcher(word);
//            if (!matcher.find()) {
//                pattern = Pattern.compile("(^\\W*(\\w.*\\w)\\W*$|(^\\w+$)|.*)");
//                matcher = pattern.matcher(word);
//                word = matcher.replaceAll("$2$3");
//            } else {
//                word = "";
//            }
//        } else {
//            word = "";
//        }
//        this.strings[0] = word;

        return this.strings;
    }
}
