/*
 * Copyright 2012: Thomson Reuters Global Resources. All Rights Reserved.
 *
 * Proprietary and Confidential information of TRGR. Disclosure, Use or
 * Reproduction without the written authorization of TRGR is prohibited
 */

package com.research.course.debate.util ;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import com.trgr.rd.newsplus.hashing.HashInterface;

/**
 *
 *
 */
public class CompareUtils {
	public enum Order {
		ASCENDING, DESCENDING
	}

	private CompareUtils() {
	}

	public static int integerCompare(int a, int b) {
		return (a < b ? -1 : (a == b ? 0 : 1));
	}

	public static int longCompare(long a, long b) {
		return (a < b ? -1 : (a == b ? 0 : 1));
	}

	/**
	 * Sorts the given map by its values in the given order. Returns a list of
	 * the sorted map entries.
	 * <p>
	 * Ties are broken by comparing the keys in ascending order.
	 * </p>
	 * 
	 * @param map
	 *            the map to sort
	 * @param order
	 *            the order which to sort
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * 
	 * @return a sorted list of the map entries
	 * 
	 * @throws IllegalArgumentException
	 *             is any arguments are <code>null</code>
	 */
	public static <K extends Comparable<K>, V extends Comparable<V>> List<Map.Entry<K, V>> sortMapEntriesByValue(
			final Map<K, V> map, final Order order) {
		ArgumentVerifierUtil.checkForNull(order, "order");
		ArgumentVerifierUtil.checkForNull(map, "map");
		List<Map.Entry<K, V>> list = new ArrayList<Map.Entry<K, V>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				V first = o1.getValue();
				V second = o2.getValue();
				int result;
				if (order == Order.DESCENDING) {
					result = second.compareTo(first);
				} else {
					result = first.compareTo(second);
				}

				if (result == 0) {
					return o1.getKey().compareTo(o2.getKey());
				}

				return result;

			}
		});
		return list;
	}

	public static <K extends Comparable<K>, V extends Comparable<V>> Map<K, V> sortByValue(
			Map<K, V> map, Order order) {
		ArgumentVerifierUtil.checkForNull(map, "map");
		ArgumentVerifierUtil.checkForNull(order, "order");
		List<Map.Entry<K, V>> entries = sortMapEntriesByValue(map, order);
		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : entries) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public static <K extends Comparable<K>, V extends Comparable<V>> Map<K, V> sortByValueAsc(
			Map<K, V> map) {
		return sortByValue(map, Order.ASCENDING);
	}

	public static <K extends Comparable<K>, V extends Comparable<V>> Map<K, V> sortByValueDesc(
			Map<K, V> map) {
		return sortByValue(map, Order.DESCENDING);
	}

	@SuppressWarnings("UnnecessaryUnboxing")
	public static float computeCosSimilarity(Map<String, Float> feat1,
			Map<String, Float> feat2) {
		float outValue = 0;
		float dotSum = 0;
		float modv1 = 0;
		float modv2 = 0;

		Set<String> unionSet = new HashSet<String>(feat1.keySet());
		unionSet.addAll(feat2.keySet());

		for (String featureName : unionSet) {
			Float feat1value = feat1.get(featureName);
			Float feat2value = feat2.get(featureName);

			if (feat1value != null) {
				modv1 += feat1value.floatValue() * feat1value.floatValue();
			}
			if (feat2value != null) {
				modv2 += feat2value.floatValue() * feat2value.floatValue();
			}

			if (feat1value != null && feat2value != null) {
				dotSum += feat1value.floatValue() * feat2value.floatValue();
			}
		}

		if (modv1 > 0 && modv2 > 0) {
			outValue = (float) (dotSum / (Math.sqrt(modv1) * Math.sqrt(modv2)));
		}

		return outValue;
	}

	public static float computeCosSimilarity( Map<String, Float> feat1,
			Map<String, Float> feat2, int topPosn ) {
		float outValue = 0;
		float dotSum = 0;
		float modv1 = 0;
		float modv2 = 0;

		int index = 1 ;
		for (String featureName : feat1.keySet() )
		{
			Float feat1value = feat1.get(featureName);
			Float feat2value = feat2.get(featureName);

			if (feat1value != null) 
			{
				modv1 += feat1value.floatValue() * feat1value.floatValue();
			}
			if (feat2value != null)
			{
				modv2 += feat2value.floatValue() * feat2value.floatValue();
			}

			if (feat1value != null && feat2value != null)
			{
				dotSum += feat1value.floatValue() * feat2value.floatValue();
			}
			if ( index == topPosn )
			{
				break;
			}
			index++;
		}

		if (modv1 > 0 && modv2 > 0) {
			outValue = (float) (dotSum / (Math.sqrt(modv1) * Math.sqrt(modv2)));
		}

		return outValue;
	}
/*
	public static float computeTextFeature(Collection<String> sourceFeatures,
			Collection<String> candidateFeatures, HashInterface dataHash) {
		float similarityScore = 0.0f;

		if (sourceFeatures == null || candidateFeatures == null) {
			return similarityScore;
		}

		Map<String, Float> idfMap = new HashMap<String, Float>();
		Map<String, Float> sourceMap;
		Map<String, Float> candidateMap;

		float sourceTermFrequency = 1 / ((float) sourceFeatures.size());
		float candidateTermFrequency = 1 / ((float) candidateFeatures.size());

		Set<String> unionSet = new HashSet<String>();
		unionSet.addAll(sourceFeatures);
		unionSet.addAll(candidateFeatures);

		for (String feature : unionSet) {
			if (feature.trim().length() > 1) {
				if (dataHash.getDocCount() > 0) {
					float weight = dataHash.getIDF(feature.trim());
					if (!Float.isNaN(weight)) {
						idfMap.put(feature.trim(), weight);
					}
				} else {
					idfMap.put(feature.trim(),
							(float) (Math.log(1000000 / 5) / Math.log(2.0)));
				}
			}
		}

		sourceMap = computeTextFeatureScores(idfMap, sourceFeatures,
				sourceTermFrequency);
		candidateMap = computeTextFeatureScores(idfMap, candidateFeatures,
				candidateTermFrequency);

		similarityScore = CompareUtils.computeCosSimilarity(sourceMap,
				candidateMap);

		return similarityScore;
	}
*/
	public static Map<String, Float> computeTextFeatureScores(
			Map<String, Float> idfMap, Collection<String> seg, float tf) {
		Map<String, Float> resultMap = new HashMap<String, Float>();
		for (String aSeg : seg) {
			String onesc = aSeg.trim();
			Float weight = idfMap.get(onesc);
			if (weight != null) {
				Float scv = resultMap.get(onesc);
				if (scv == null) {
					resultMap.put(onesc, tf * weight);
				} else {
					resultMap.put(onesc, (tf * weight + scv));
				}
			}
		}
		return resultMap;
	}
}