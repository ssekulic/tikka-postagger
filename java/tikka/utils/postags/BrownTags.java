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

    @Override
    protected HashSet<String> setTags() {
        setBrownTags();
        setIdxMap(fullTagSet);
        return fullTagSet;
    }

    @Override
    protected HashSet<String> reduceTag() {
//        reducedTagSet = new HashSet<String>(Arrays.asList("AB", "AP", "BE", "CC","CD","CS"));
//        put("ABL", "AB");
//        put("ABN", "AB");
//        put("ABX", "AB");
//        put("AP", "AP");
//        put("BE", "BE");
//        put("BED", "BE");
//        put("BEDZ", "BE");
//        put("BEG", "BE");
//        put("BEM", "BE");
//        put("BEN", "BE");
//        put("BER", "BE");
//        put("BEZ", "BE");
//        put("CC", "CC");
//        put("CD","CD");
//        put("CS","CS");
//        put("DO","DO");
        return reducedTagSet;
    }
}
