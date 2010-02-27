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
public class BrownTags extends TagMap {

    public BrownTags(int modelTagSize) {
        super(modelTagSize);
    }

    @Override
    protected HashSet<String> setTags() {
        setBrownTags();
        return fullTagSet;
    }

    @Override
    public String getTag(String tag) {
        String[] tags = tag.split("[+\\-]");
        return super.getTag(tags[0]);
    }
}
