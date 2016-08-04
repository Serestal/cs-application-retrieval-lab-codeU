package com.flatironschool.javacs;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import redis.clients.jedis.Jedis;


/**
 * Represents the results of a search query.
 *
 */
public class WikiSearch {
	
	// map from URLs that contain the term(s) to relevance score
	private Map<String, Integer> map;

	/**
	 * Constructor.
	 * 
	 * @param map
	 */
	public WikiSearch(Map<String, Integer> map) {
		this.map = map;
	}
	
	/**
	 * Looks up the relevance of a given URL.
	 * 
	 * @param url
	 * @return
	 */
	public Integer getRelevance(String url) {
		Integer relevance = map.get(url);
		return relevance==null ? 0: relevance;
	}
	
	/**
	 * Prints the contents in order of term frequency.
	 * 
	 * @param map
	 */
	private  void print() {
		List<Entry<String, Integer>> entries = sort();
		for (Entry<String, Integer> entry: entries) {
			System.out.println(entry);
		}
	}
	
	/**
	 * Computes the union of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch or(WikiSearch that) {
        // FILL THIS IN!
        HashMap<String, Integer> results = new HashMap<String, Integer>();
		for (String key : map.keySet()) {
			results.put(key, map.get(key));
		}
		Map<String, Integer> that_map = that.getMap();
		for (String key : that_map.keySet()) {
			if (results.get(key) != null) {
				results.put(key, results.get(key) + that_map.get(key));
			} else {
				results.put(key, that_map.get(key));
			}
		}
		return new WikiSearch(results);
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch and(WikiSearch that) {
        // FILL THIS IN!
		HashMap<String, Integer> results = new HashMap<String, Integer>();
		Map<String, Integer> that_map = that.getMap();

		for (String key : map.keySet()) {
			if (that_map.get(key) != null) {
				results.put(key, map.get(key) + that_map.get(key));
			}
		}

		return new WikiSearch(results);
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch minus(WikiSearch that) {
        // FILL THIS IN!
		HashMap<String, Integer> results = new HashMap<String, Integer>();
		Map<String, Integer> that_map = that.getMap();
		
		for (String key : map.keySet()) {
			if (that_map.get(key) == null) {
				results.put(key, map.get(key));
			}
		}

		return new WikiSearch(results);
	}
	
	public Map<String, Integer> getMap() {
		return map;
	}
	/**
	 * Computes the relevance of a search with multiple terms.
	 * 
	 * @param rel1: relevance score for the first search
	 * @param rel2: relevance score for the second search
	 * @return
	 */
	protected int totalRelevance(Integer rel1, Integer rel2) {
		// simple starting place: relevance is the sum of the term frequencies.
		return rel1 + rel2;
	}

	/**
	 * Sort the results by relevance.
	 * 
	 * @return List of entries with URL and relevance.
	 */
	public List<Entry<String, Integer>> sort() {
        // FILL THIS IN!
        List<Entry<String, Integer>> results = new LinkedList<Entry<String, Integer>>();
        for (Entry<String, Integer> entry : map.entrySet()) {
        	results.add(entry);
        }
		results.sort(entry_comparator);
		return results;
	}

	Comparator<Entry<String, Integer>> entry_comparator = new Comparator<Entry<String, Integer>>() {
		public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
			return e1.getValue() - e2.getValue();
		}
	};

	/**
	 * Performs a search and makes a WikiSearch object.
	 * 
	 * @param term
	 * @param index
	 * @return
	 */
	public static WikiSearch search(String term, JedisIndex index) {
		Map<String, Integer> map = index.getCounts(term);
		return new WikiSearch(map);
	}

	public static void main(String[] args) throws IOException {
		
		// make a JedisIndex
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis); 
		
		// search for the first term
		String term1 = "java";
		System.out.println("Query: " + term1);
		WikiSearch search1 = search(term1, index);
		search1.print();
		
		// search for the second term
		String term2 = "programming";
		System.out.println("Query: " + term2);
		WikiSearch search2 = search(term2, index);
		search2.print();
		
		// compute the intersection of the searches
		System.out.println("Query: " + term1 + " AND " + term2);
		WikiSearch intersection = search1.and(search2);
		intersection.print();
	}
}
