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
public class PennTags extends TagSet {

    @Override
    public HashSet<String> setTags() {
        return setPennTags();
    }

    @Override
    public HashSet<String> reduceTag() {
        return reducedTagSet;
        
    }
}
