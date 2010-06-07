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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Taesun Moon <tsunmoon@gmail.com>
 */
public abstract class TagMap extends HashMap<String, Integer> implements
      Serializable {

    static private final long serialVersionUID = 100L;
    protected HashSet<String> contentTagSet, functionTagSet, fullTagSet;
    protected HashSet<String> reducedTagSet;
    protected HashMap<Integer, String> idxToContentTag, idxToFunctionTag, idxToFullTag, idxToReducedTag;
    protected HashMap<String, String> fullTagToReducedTag;
    protected HashMap<Integer, Integer> oneToOneTagMap;
    protected HashMap<Integer, Integer> manyToOneTagMap;
    protected HashMap<Integer, Integer> goldToModelTagMap;
    protected TagSetEnum.TagSet tagSet;
    protected TagSetEnum.ReductionLevel level;

    public TagMap(int _modelTagSize) {
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

    public String getTagSetName() {
        return tagSet.toString();
    }

    public TagSetEnum.ReductionLevel getReductionLevel() {
        return level;
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

    public String getOneToOneTagString(int stateid) {
        return idxToFullTag.get(oneToOneTagMap.get(stateid));
    }

    public String getManyToOneTagString(int stateid) {
        return idxToFullTag.get(manyToOneTagMap.get(stateid));
    }

    public String getGoldReducedTagString(int goldid) {
        return idxToFullTag.get(goldid);
    }

    public int getContentTagSize() {
        return contentTagSet.size();
    }

    public int getFunctionTagSize() {
        return functionTagSet.size();
    }

    public boolean isContentTag(String tag) {
        return contentTagSet.contains(tag);
    }

    public boolean isFunctionTag(String tag) {
        return functionTagSet.contains(tag);
    }

    public boolean isContentTag(int tagid) {
        return idxToContentTag.containsKey(tagid);
    }

    public boolean isFunctionTag(int tagid) {
        return idxToFunctionTag.containsKey(tagid);
    }
}
