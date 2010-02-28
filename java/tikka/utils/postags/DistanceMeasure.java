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
 * Distance measure class to populate cost matrix in Evaluator
 * 
 * @author tsmoon
 */
public abstract class DistanceMeasure {

    protected int[] modelTags, goldTags, cooccurrenceMatrix, modelTagCounts, goldTagCounts;
    protected int M, N;

    /**
     * Default constructor. Requires full access to the Evaluator that it is called from
     * 
     * @param evaluator Calling Evaluator class object.
     */
    public DistanceMeasure(Evaluator evaluator) {
        this.modelTags = evaluator.modelTags;
        this.goldTags = evaluator.goldTags;
        this.cooccurrenceMatrix = evaluator.cooccurrenceMatrix;
        this.modelTagCounts = evaluator.modelTagCounts;
        this.goldTagCounts = evaluator.goldTagCounts;
        this.M = evaluator.M;
        this.N = evaluator.N;
    }

    /**
     * Calculate distance between two tags i and j.
     * 
     * @param i tag in model
     * @param j tag in gold
     * @return distance between two tags
     */
    public abstract double cost(int i, int j);
}
