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

//    protected int M, N;
    protected int[] modelTags;//, modelTagCounts;
    //protected int[] cooccurrenceMatrix;
    protected int[] fullGoldTags;//, goldTagCounts;
    protected int[] reducedGoldTags;//, reducedGoldTagCounts;
//    protected double[] costMatrix;
    protected double fullOneToOneAccuracy, fullManyToOneAccuracy;
    protected double reducedOneToOneAccuracy, reducedManyToOneAccuracy;
    protected TagMap fullTagMap;
    protected TagMap reducedTagMap;
//    protected HashSet<Integer> rows, cols;
    protected HashMap<Integer, Integer> fullOneToOneTagMap;
    protected HashMap<Integer, Integer> fullManyToOneTagMap;
    protected HashMap<Integer, Integer> reducedOneToOneTagMap;
    protected HashMap<Integer, Integer> reducedManyToOneTagMap;
    protected HashMap<Integer, Integer> goldToModelTagMap;
    protected DistanceMeasureEnum.Measure measure;
    protected DistanceMeasure distanceMeasure;

    /**
     * 
     * @param tagMap
     * @param measure
     */
    public Evaluator(TagMap tagMap, DistanceMeasureEnum.Measure measure) {
        fullTagMap = tagMap;
        reducedTagMap = TagMapGenerator.generate(fullTagMap.tagSet, 1, fullTagMap.oneToOneTagMap.size());

        fullOneToOneTagMap = fullTagMap.oneToOneTagMap;
        fullManyToOneTagMap = fullTagMap.manyToOneTagMap;

        reducedOneToOneTagMap = reducedTagMap.oneToOneTagMap;
        reducedManyToOneTagMap = reducedTagMap.manyToOneTagMap;

        this.measure = measure;
    }

    /**
     * 
     * @param modelTags
     * @param goldTags
     */
    public void evaluateTags(int[] modelTags, int[] goldTags) {
        this.modelTags = modelTags;
        fullGoldTags = goldTags;

        reducedGoldTags = new int[fullGoldTags.length];
        for (int i = 0; i < fullGoldTags.length; ++i) {
            int fullid = fullGoldTags[i];
            String fulltag = fullTagMap.idxToFullTag.get(fullid);
            reducedGoldTags[i] = reducedTagMap.get(reducedTagMap.getReducedTag(fulltag));
        }

        matchTags(modelTags, fullGoldTags, fullTagMap, fullOneToOneTagMap, fullManyToOneTagMap);
        fullOneToOneAccuracy = measureAccuracy(modelTags, fullGoldTags, fullOneToOneTagMap);
        fullManyToOneAccuracy = measureAccuracy(modelTags, fullGoldTags, fullManyToOneTagMap);
        matchTags(modelTags, reducedGoldTags, reducedTagMap, reducedOneToOneTagMap, reducedManyToOneTagMap);
        reducedOneToOneAccuracy = measureAccuracy(modelTags, reducedGoldTags, reducedOneToOneTagMap);
        reducedManyToOneAccuracy = measureAccuracy(modelTags, reducedGoldTags, reducedManyToOneTagMap);
    }

    /**
     * 
     * @param modelTags
     * @param goldTags
     * @param tagMap
     * @param oneToOneTagMap
     * @param manyToOneTagMap
     */
    public void matchTags(int[] modelTags, int[] goldTags, TagMap tagMap,
          HashMap<Integer, Integer> oneToOneTagMap,
          HashMap<Integer, Integer> manyToOneTagMap) {
        int M = tagMap.oneToOneTagMap.size();
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

        switch (measure) {
            case JACCARD:
                distanceMeasure = new JaccardMeasure(cooccurrenceMatrix, modelTagCounts, goldTagCounts, N);
                break;
            case JENSEN_SHANNON:
                distanceMeasure = new JensenShannonMeasure(cooccurrenceMatrix, modelTagCounts, goldTagCounts, N);
                break;
        }

        for (int i = 0; i < M; ++i) {
            for (int j = 0; j < N; ++j) {
                costMatrix[i * N + j] = distanceMeasure.cost(i, j);
            }
        }

        HashSet<Integer> rows = new HashSet<Integer>();
        HashSet<Integer> cols = new HashSet<Integer>();
        setTickers(rows, cols, M, N);

        double[] tmpCostMatrix = costMatrix.clone();
        buildOneToOneMap(tmpCostMatrix, rows, cols, N, oneToOneTagMap);

        setTickers(rows, cols, M, N);
        buildManyToOneMap(costMatrix, rows, cols, N, manyToOneTagMap);
    }

    /**
     * 
     * @param rows
     * @param cols
     * @param M
     * @param N
     */
    protected void setTickers(HashSet<Integer> rows,
          HashSet<Integer> cols, int M, int N) {
        for (int i = 0; i < M; ++i) {
            rows.add(i);
        }
        for (int i = 0; i < N; ++i) {
            cols.add(i);
        }
    }

    /**
     * 
     * @param costMatrix
     * @param rows
     * @param cols
     * @param N
     * @param oneToOneTagMap
     */
    protected void buildOneToOneMap(double[] costMatrix, HashSet<Integer> rows,
          HashSet<Integer> cols, int N, HashMap<Integer, Integer> oneToOneTagMap) {
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
            buildOneToOneMap(costMatrix, rows, cols, N, oneToOneTagMap);
        }
    }

    /**
     * 
     * @param costMatrix
     * @param rows
     * @param cols
     * @param N
     * @param manyToOneTagMap
     */
    protected void buildManyToOneMap(double[] costMatrix,
          HashSet<Integer> rows, HashSet<Integer> cols, int N,
          HashMap<Integer, Integer> manyToOneTagMap) {
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

//    protected void measureAccuracy() {
//        int total = modelTags.length;
//        int correct = 0;
//        for (int i = 0; i < total; ++i) {
//            int j = modelTags[i];
//            if (fullOneToOneTagMap.get(j) == fullGoldTags[i]) {
//                correct++;
//            }
//        }
//        fullOneToOneAccuracy = correct / (double) total;
//
//        correct = 0;
//        for (int i = 0; i < total; ++i) {
//            int j = modelTags[i];
//            if (fullManyToOneTagMap.get(j) == fullGoldTags[i]) {
//                correct++;
//            }
//        }
//        fullManyToOneAccuracy = correct / (double) total;
//    }

    /**
     * 
     * @param modelTags
     * @param goldTags
     * @param tagMap
     * @return
     */
    protected double measureAccuracy(int[] modelTags, int[] goldTags,
          HashMap<Integer, Integer> tagMap) {
        int total = modelTags.length;
        int correct = 0;
        for (int i = 0; i < total; ++i) {
            int j = modelTags[i];
            if (tagMap.get(j) == goldTags[i]) {
                correct++;
            }
        }
        return correct / (double) total;
    }

    /**
     * @return the fullOneToOneAccuracy
     */
    public double getFullOneToOneAccuracy() {
        return fullOneToOneAccuracy;
    }

    /**
     * @return the fullManyToOneAccuracy
     */
    public double getFullManyToOneAccuracy() {
        return fullManyToOneAccuracy;
    }

    /**
     * @return the fullOneToOneAccuracy
     */
    public double getReducedOneToOneAccuracy() {
        return reducedOneToOneAccuracy;
    }

    /**
     * @return the fullManyToOneAccuracy
     */
    public double getReducedManyToOneAccuracy() {
        return reducedManyToOneAccuracy;
    }
}
