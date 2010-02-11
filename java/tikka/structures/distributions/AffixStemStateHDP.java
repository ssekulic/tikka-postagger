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
//  You should have received stem copy of the GNU Lesser General Public
//  License along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
///////////////////////////////////////////////////////////////////////////////
package tikka.structures.distributions;

import tikka.exceptions.EmptyCountException;
import tikka.exceptions.KeyRemovedException;
import tikka.exceptions.EmptyTwoDimLexiconException;
import tikka.structures.lexicons.Lexicon;

/**
 * Distribution for affixes conditioned on stems and affixes. The base distribution
 * for the affixes is not the usual base distribution but another conditional
 * DP.
 * 
 * @author tsmoon
 */
public class AffixStemStateHDP extends AffixStemStateDP {

    /**
     * Distribution for maintaining prob of affix given state.
     */
    protected AffixStateDP affixStateDP;

    /**
     *
     * @param baseDistribution
     * @param lexicon
     * @param hyper
     */
    public AffixStemStateHDP(
          HierarchicalDirichletBaseDistribution baseDistribution,
          Lexicon lexicon, double hyper, int states) {
        super(null, null, hyper, states);
        affixStateDP = new AffixStateDP(baseDistribution, lexicon, hyper);
    }

    /**
     * Decrement counts for an affix given stem and class.
     *
     * @param cls
     * @param affix
     * @param stem
     * @return
     */
    @Override
    public int dec(int cls, int stem, int affix) {
        int val = 0;
        try {
            baseDistribution.dec(lexicon.getString(affix));
        } catch (EmptyCountException e) {
            e.printMessage(lexicon.getString(affix), affix);
            System.exit(1);
        }

        try {
            val = clsAffixStemCounts.dec(cls, stem, affix);
        } catch (EmptyCountException e) {
            e.printMessage(lexicon.getString(affix), affix);
            System.exit(1);
        } catch (EmptyTwoDimLexiconException e) {
            clsAffixStemCounts.get(cls).remove(stem);
        }

        try {
            lexicon.dec(affix);
        } catch (KeyRemovedException e) {
        }

        return val;
    }

    /**
     * Increment counts for affix given stem and class.
     *
     * @param cls
     * @param affix
     * @param stem
     * @return
     */
    @Override
    public int inc(int cls, int stem, int affix) {
        lexicon.inc(affix);
        baseDistribution.inc(lexicon.getString(affix));
        return clsAffixStemCounts.inc(cls, stem, affix);
    }
}
