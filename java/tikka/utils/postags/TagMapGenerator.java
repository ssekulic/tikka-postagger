///////////////////////////////////////////////////////////////////////////////
// To change this template, choose Tools | Templates
// and open the template in the editor.
///////////////////////////////////////////////////////////////////////////////

package tikka.utils.postags;

/**
 *
 * @author tsmoon
 */
public class TagMapGenerator {

    public static TagMap generate(TagSetEnum.TagSet tagSet, int level, int modelTagSize) {
        TagMap tagMap = null;
        switch(tagSet) {
            case BROWN:
                switch(level) {
                    case 0:
                        tagMap = new BrownTags(modelTagSize);
                        break;
                    case 1:
                        tagMap = new BrownTagsCE(modelTagSize);
                        break;
                }
                break;
            case PTB:
                switch(level) {
                    case 0:
                        tagMap = new PennTags(modelTagSize);
                        break;
                    case 1:
                        tagMap = new PennTagsCE(modelTagSize);
                        break;
                }
                break;
            case TIGER:
                throw new UnsupportedOperationException("Implement this fool!");
//                break;
        }
        return tagMap;
    }
}
