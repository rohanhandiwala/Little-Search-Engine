package lse;

import java.io.*;
import java.util.*;

/**
 * This class builds an index of keywords. Each keyword maps to a set of pages in
 * which it occurs, with frequency of occurrence in each page.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in 
	 * DESCENDING order of frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash set of all noise words.
	 */
	HashSet<String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashSet<String>(100,2.0f);
	}
	
	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeywordsFromDocument(String docFile) 
	throws FileNotFoundException {
		Scanner sc = new Scanner(new File(docFile));
        HashMap<String,Occurrence> map = new HashMap<String,Occurrence>();
        while(sc.hasNext()){
                String word = sc.next();
                word = getKeyword(word);
                if(word == null) {
                	return null;
                }
                if(word != null){
                        if(map.containsKey(word)){
                               
                                map.get(word).frequency = map.get(word).frequency+1;
                        }else{
                               
                                Occurrence occur = new Occurrence(docFile,1);
                                map.put(word, occur);
                        }
                }
               
        }
        return map;
}
		
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeywords(HashMap<String,Occurrence> kws) {
		  for(String i : kws.keySet()){
              Occurrence occurrence = kws.get(i);
              if(!keywordsIndex.containsKey(i)){
                      ArrayList<Occurrence> occur = new ArrayList<Occurrence>();
                      occur.add(occurrence);
                      keywordsIndex.put(i, occur);
              }else{
                      
                      keywordsIndex.get(i).add(occurrence);
                      
                      insertLastOccurrence(keywordsIndex.get(i));
              }
      }
}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * trailing punctuation, consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyword(String word) {
		word = word.toLowerCase();
		int str = word.length()-1;
		while((str == '.' || str == '!' || str == '?' || str == ',') && word.length() > 1) {
			word = word.substring(0, str)+ word.substring(str+1, word.length());
			str--;
		}
	for (int i = 0; i < word.length(); i++ ) {
		if(!Character.isAlphabetic(word.charAt(i))) {
			return null;
		}
	if(noiseWords.contains(word))
		return null;
		}
		return word;
		}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion is done by
	 * first finding the correct spot using binary search, then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		 if (occs.size() == 1) {
             return null;
		 }
     
     int end = occs.get(occs.size()-1).frequency;
     ArrayList<Integer> midIndex = new ArrayList<Integer>();
     Occurrence temp = occs.get(occs.size() -1);
     
     int low = 0;
     
     int mid;
     
     int high = occs.size()-1;
      
     while( low <= high ){
    	 
    	 mid = ( low + high ) / 2;
    	 midIndex.add(mid);

    	 if( end > occs.get(mid).frequency ){   
    		 
    		 high = mid - 1;
    		 
    	 } else if(low < occs.get(mid).frequency){
    		 
         low = mid + 1;
         
    	 } else {
    		 
         break;
    	 }
    	 
     }

     if(midIndex.get(midIndex.size()-1) == 0){
    	 
     if(temp.frequency < occs.get(0).frequency){
    	 
             occs.add(1, temp);
             
             occs.remove(occs.size() - 1);
             
             return midIndex;
     	}
     }

     occs.add(midIndex.get(midIndex.size() - 1), temp);

     occs.remove(occs.size() - 1);
     
     return midIndex;
}

	
	
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.add(word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeywordsFromDocument(docFile);
			mergeKeywords(kws);
		}
		sc.close();
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of document frequencies. (Note that a
	 * matching document will only appear once in the result.) Ties in frequency values are broken
	 * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
	 * also with the same frequency f1, then doc1 will take precedence over doc2 in the result. 
	 * The result set is limited to 5 entries. If there are no matches at all, result is null.
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matches, returns null.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		ArrayList<String> top5 = new ArrayList<String>();
        ArrayList<Occurrence> str1 = new ArrayList<Occurrence>();
        ArrayList<Occurrence> str2 = new ArrayList<Occurrence>();

        kw1 = kw1.toLowerCase();
        kw2 = kw2.toLowerCase();
       

        if (keywordsIndex.get(kw1) != null) {
                str1 = keywordsIndex.get(kw1);
        }
        if (keywordsIndex.get(kw2) != null) {
                str2 = keywordsIndex.get(kw2);
        }

        
        if (str1.isEmpty() && str2.isEmpty()) {
                return null;
        } 
        else {
                int counter = 0;
                int i = 0;
                int j = 0;
                
                while (counter < 5 && (i < str1.size() || j < str2.size())) {
                       
                        if(str1.isEmpty()){
                                if(!top5.contains(str2.get(j).document)){
                                        top5.add(str2.get(j).document);
                                        counter++;
                                }
                                j++;
                        }
                        
                        else if(str2.isEmpty()){
                                if(!top5.contains(str1.get(i).document)){
                                        top5.add(str1.get(i).document);
                                        counter++;
                                }
                                i++;
                        }
                        else if(i>=str1.size()&&str1.size()>0){
                                if(!top5.contains(str2.get(j).document)){
                                        top5.add(str2.get(j).document);
                                        counter++;
                                }
                                j++;
                        }

                        else if(j>=str2.size()&&str2.size()>0){
                                if(!top5.contains(str1.get(i).document)){
                                        top5.add(str1.get(i).document);
                                        counter++;
                                }
                                i++;
                        }
                        else if(str1.get(i).frequency > str2.get(j).frequency){
                                if(!top5.contains(str1.get(i).document)){
                                        top5.add(str1.get(i).document);
                                        counter++;
                                }
                                i++;

                        }
                        else if(str1.get(i).frequency < str2.get(j).frequency){
                                if(!top5.contains(str2.get(j).document)){
                                        top5.add(str2.get(j).document);
                                        counter++;
                                }
                                j++;
                        }
                       
                        else if(str1.get(i).frequency == str2.get(j).frequency){
                                if(!top5.contains(str1.get(i).document)){
                                        top5.add(str1.get(i).document);
                                        counter++;
                                }
                                i++;
                        }
                }
        }
        return top5;
}


	
	}