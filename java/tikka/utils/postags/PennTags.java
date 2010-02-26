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
public class PennTags extends TagMap {

    @Override
    protected HashSet<String> setTags() {
        setPennTags();
        setIdxMap(fullTagSet);
        return fullTagSet;
    }

    @Override
    protected HashSet<String> reduceTag() {
        return reducedTagSet;
    }
}
