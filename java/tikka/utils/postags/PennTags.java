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
public class PennTags extends EnglishTagMap {

    public PennTags(int modelTagSize) {
        super(modelTagSize);
    }

    @Override
    protected HashSet<String> setTags() {
        setPennTags();
        return fullTagSet;
    }
}
