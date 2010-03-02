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
package tikka.utils.postags;

/**
 * Jensen Shannon divergence measure for populating cost matrix in Evaluator
 *
 * @author tsmoon
 */
public class JensenShannonMeasure extends DistanceMeasure {

    public JensenShannonMeasure(int[] cooccurrenceMatrix, int[] modelTagCounts,
          int[] goldTagCounts, int N) {
        super(cooccurrenceMatrix, modelTagCounts, goldTagCounts, N);
    }

    /**
     * Not yet implemented
     * 
     * @param i
     * @param j
     * @return
     */
    @Override
    public double cost(int i, int j) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
