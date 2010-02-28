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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class for evaluating model tags based on gold tags
 *
 * @author tsmoon
 */
public class Evaluator {

    protected int M, N;
    protected int[] modelTags, goldTags, cooccurrenceMatrix, modelTagCounts, goldTagCounts;
    protected double[] costMatrix;
    protected double oneToOneAccuracy, manyToOneAccuracy;
    protected TagMap tagMap;
    protected HashSet<Integer> rows, cols;
    protected HashMap<Integer, Integer> oneToOneTagMap;
    protected HashMap<Integer, Integer> manyToOneTagMap;
    protected HashMap<Integer, Integer> goldToModelTagMap;
    protected DistanceMeasureEnum.Measure measure;
    protected DistanceMeasure distanceMeasure;

    public Evaluator(TagMap tagMap, DistanceMeasureEnum.Measure measure) {
        this.tagMap = tagMap;
        this.oneToOneTagMap = tagMap.oneToOneTagMap;
        this.manyToOneTagMap = tagMap.manyToOneTagMap;
        this.measure = measure;
    }

    public void evaluateTags(int[] modelTags, int[] goldTags) {
        this.modelTags = modelTags;
        this.goldTags = goldTags;
        matchTags();
        measureAccuracy();
    }

    public void matchTags() {
        M = tagMap.oneToOneTagMap.size();
        N = tagMap.reducedTagSet.size();
        cooccurrenceMatrix = new int[M * N];
        costMatrix = new double[M * N];
        modelTagCounts = new int[M];
        goldTagCounts = new int[N];

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

        switch (measure) {
            case JACCARD:
                distanceMeasure = new JaccardMeasure(this);
                break;
            case JENSEN_SHANNON:
                distanceMeasure = new JensenShannonMeasure(this);
                break;
        }

        for (int i = 0; i < M; ++i) {
            for (int j = 0; j < N; ++j) {
                costMatrix[i * N + j] = distanceMeasure.cost(i, j);
            }
        }

        rows = new HashSet<Integer>();
        cols = new HashSet<Integer>();
        setTickers();

        double[] tmpCostMatrix = costMatrix.clone();
        buildOneToOneMap(tmpCostMatrix);

        setTickers();
        buildManyToOneMap(costMatrix);
    }

    protected void setTickers() {
        for (int i = 0; i < M; ++i) {
            rows.add(i);
        }
        for (int i = 0; i < N; ++i) {
            cols.add(i);
        }
    }

    protected void buildOneToOneMap(double[] costMatrix) {
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

        if (min == 1) {
            ArrayList<Integer> tl = new ArrayList<Integer>(rows);
            Collections.shuffle(tl);
            imin = tl.get(0);

            tl = new ArrayList<Integer>(cols);
            Collections.shuffle(tl);
            jmin = tl.get(0);
        }

        rows.remove(imin);
        cols.remove(jmin);

        oneToOneTagMap.put(imin, jmin);

        if (!rows.isEmpty() && !cols.isEmpty()) {
            buildOneToOneMap(costMatrix);
        }
    }

    protected void buildManyToOneMap(double[] costMatrix) {
        for (int i : rows) {
            double min = 1;
            int jmin = 0;
            for (int j : cols) {
                if (costMatrix[i * N + j] < min) {
                    jmin = j;
                    min = costMatrix[i * N + j];
                }
            }
            manyToOneTagMap.put(i, jmin);
        }
    }

    protected void measureAccuracy() {
        int total = modelTags.length;
        int correct = 0;
        for (int i = 0; i < total; ++i) {
            int j = modelTags[i];
            if (oneToOneTagMap.get(j) == goldTags[i]) {
                correct++;
            }
        }
        oneToOneAccuracy = correct / (double) total;

        correct = 0;
        for (int i = 0; i < total; ++i) {
            int j = modelTags[i];
            if (manyToOneTagMap.get(j) == goldTags[i]) {
                correct++;
            }
        }
        manyToOneAccuracy = correct / (double) total;
    }

    /**
     * @return the oneToOneAccuracy
     */
    public double getOneToOneAccuracy() {
        return oneToOneAccuracy;
    }

    /**
     * @return the manyToOneAccuracy
     */
    public double getManyToOneAccuracy() {
        return manyToOneAccuracy;
    }
}
