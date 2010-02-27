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

import java.util.HashMap;
import java.util.HashSet;
import tikka.utils.postags.TagMap;

/**
 *
 * @author tsmoon
 */
public class Evaluator {

    protected TagMap tagMap;
    protected HashMap<Integer, Integer> modelToGoldTagMap;
    protected HashMap<Integer, Integer> goldToModelTagMap;

    public Evaluator(TagMap tagMap) {
        this.tagMap = tagMap;
        this.modelToGoldTagMap = tagMap.modelToGoldTagMap;
        this.goldToModelTagMap = tagMap.goldToModelTagMap;
    }

    public void matchTags(int[] modelTags, int[] goldTags) {
        int M = tagMap.modelToGoldTagMap.size();
        int N = tagMap.reducedTagSet.size();
        int[] cooccurrenceMatrix = new int[M * N];
        double[] costMatrix = new double[M * N];
        int[] modelTagCounts = new int[M];
        int[] goldTagCounts = new int[N];

        for (int i = 0; i < M * N; ++i) {
            cooccurrenceMatrix[i] = 0;
            costMatrix[i] = 0;
        }
        for (int i = 0; i < M; ++i) {
            modelTagCounts[i] = 0;
        }
        for (int i = 0; i < N; ++i) {
            goldTagCounts[i] = 0;
        }

        for (int i = 0; i < modelTags.length; ++i) {
            int j = modelTags[i];
            int k = goldTags[i];
            modelTagCounts[j]++;
            goldTagCounts[k]++;
            cooccurrenceMatrix[j * N + k]++;
        }

        for (int i = 0; i < M; ++i) {
            for (int j = 0; j < N; ++j) {
                costMatrix[i * N + j] = 1 - cooccurrenceMatrix[i * N + j]
                      / (0. + modelTagCounts[i] + goldTagCounts[j] - cooccurrenceMatrix[i * N + j]);
            }
        }

        HashSet<Integer> rows = new HashSet<Integer>(), cols = new HashSet<Integer>();
        for (int i = 0; i < M; ++i) {
            rows.add(i);
        }
        for (int i = 0; i < N; ++i) {
            cols.add(i);
        }
        buildMap(costMatrix, rows, cols, M, N);
        measureAccuracy(modelTags, goldTags);
    }

    protected void buildMap(double[] costMatrix, HashSet<Integer> rows,
          HashSet<Integer> cols, int M, int N) {
        double min = 1;
        int imin = 0, jmin = 0;
        for (int i : rows) {
            for (int j : cols) {
                if (costMatrix[i * N + j] < min) {
                    imin = i;
                    jmin = j;
                    min = costMatrix[i * N + j];
                }
            }
        }

        modelToGoldTagMap.put(imin, jmin);
        goldToModelTagMap.put(jmin, imin);

        rows.remove(imin);
        cols.remove(jmin);
        if (!rows.isEmpty() && !cols.isEmpty()) {
            buildMap(costMatrix, rows, cols, M, N);
        }
    }

    protected double measureAccuracy(int[] modelTags, int[] goldTags) {
        int total = modelTags.length;
        int correct = 0;
        for (int i = 0; i < total; ++i) {
            int j = modelTags[i];
            if (modelToGoldTagMap.get(j) == goldTags[i]) {
                correct++;
            }
        }
        return correct / (double) total;
    }
}
