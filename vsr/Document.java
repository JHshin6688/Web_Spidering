package ir.vsr;

import java.io.*;
import java.util.*;

import ir.utilities.*;

/**
 * Docment is an abstract class that provides for tokenization
 * of a document with stop-word removal and an iterator-like interface
 * similar to  StringTokenizer.
 * Also provides a method for converting a document into a
 * vector-space bag-of-words in the form of a HashMap of
 * tokens and their occurrence counts.
 *
 * @author Ray Mooney
 */

public abstract class Document {
  /**
   * The file where a list of stopwords, 1 per line, are stored
   */
  protected static final String stopWordsFile = "/u/mooney/ir-code/ir/utilities/stopwords.txt";
  /**
   * The number of stopwords in this file
   */
  protected static final int numStopWords = 514;
  /**
   * The hashtable where stopwords are indexed
   */
  protected static HashSet<String> stopWords = null;
  /**
   * The Porter stemmer
   */
  protected static Porter stemmer = new Porter();

  /**
   * The next token in the document
   */
  //'nextToken1' is the valid token right next to 'nextToken2'
  protected String nextToken1 = null;
  protected String nextToken2 = null;

  /**
   * The number of tokens currently read from document
   */
  protected int numTokens = 0;
  /**
   * Whether to stem tokens with the Porter stemmer
   */
  protected boolean stem = false;

  /**
   * Creates a new Document making sure that the stopwords
   * are loaded, indexed, and ready for use.  Subclasses
   * that create concrete instances MUST call prepareNextToken
   * before finishing to ensure that the first token is precomputed
   * and available.
   */
  public Document(boolean stem) {
    this.stem = stem;
    if (stopWords == null)
      loadStopWords();
  }

  /**
   * Returns the next token in the document or null if there are none
   */
  public String nextToken() {
    String token = nextToken1;
    if (token == null) return null;
    prepareNextToken();
    numTokens++;
    return token;
  }

  /**
   * The nextToken slot is always precomputed and stored by this method.
   * Performs stop-word removal of candidate tokens.
   */
  protected void prepareNextToken() {
    // Loop until a non-stopword token is found
    do {
      nextToken1 = getNextCandidateToken();
      if (nextToken1 == null) return; // reached end of document
      // Normalize token string case to lower case.
      nextToken1 = nextToken1.toLowerCase();
      // Do not include a token found in the stopword list as
      // indexed in the stopwords hashtable.
      // Also do not include tokens that are not all Unicode letters
      if (stopWords.contains(nextToken1) || !allLetters(nextToken1))
        nextToken1 = null;
      else if (stem) {
        nextToken1 = stemmer.stripAffixes(nextToken1);
        if (stopWords.contains(nextToken1))
          nextToken1 = null;
      }
    }
    while (nextToken1 == null);
  }

  /**
   * Check if this token consists of all Unicode letters to eliminate
   * other bizarre tokens
   */
  protected boolean allLetters(String token) {
    for (int i = 0; i < token.length(); i++) {
      if (!Character.isLetter(token.charAt(i)))
        return false;
    }
    return true;
  }

  /**
   * Return the next possible token in the document. Each subclass must implement
   * this method to produce candidate tokens for subsequent stop-word filtering.
   */
  protected abstract String getNextCandidateToken();

  /**
   * Load the stopwords from file to the hashtable where they are indexed.
   */
  protected static void loadStopWords() {
    // Initialize hashtable to proper size given known number of
    // stopwords in the file and a default 75% load factor with
    // 10 extra slots for spare room.
    int HashMapSize = (int) (numStopWords / 0.75 + 10);
    stopWords = new HashSet<String>(HashMapSize);
    String line;
    try {
      // Open stopword file for reading
      BufferedReader in = new BufferedReader(new FileReader(stopWordsFile));
      // Read in stopwords, one per line, until file is empty
      while ((line = in.readLine()) != null) {
        // Index word into the hashtable with
        // the default empty string as a "dummy" value.
        stopWords.add(line);
      }
      in.close();
    }
    catch (IOException e) {
      System.out.println("\nCould not load stopwords file: " + stopWordsFile);
      System.exit(1);
    }
  }

  /**
   * Returns a hashmap version of the term-vector (bag of words) for this
   * document, where each token is a key whose value is the number of times
   * it occurs in the document as stored in a Weight.
   *
   * @see Weight
   */
  public HashMapVector hashMapVector() {
    if (numTokens != 0)
      return null;
    HashMapVector vector = new HashMapVector();
    // Process each token in the document and add it to the vector

    while (hasMoreTokens()) {
      String token = nextToken();
      vector.increment(token);
    }
    return vector;
  }

  /**
   * Returns the total number of tokens in the document or -1 if
   * there are still more tokens to be read and the total count is not yet available.
   */
  public int numberOfTokens() {
    if (nextToken1 == null)
      return numTokens;
    else
      return -1;
  }

  /**
   * Returns true iff the document contains more tokens
   */
  public boolean hasMoreTokens() {
    if (nextToken1 == null)
      return false;
    else
      return true;
  }

  //Functions used for querying two-word phrases

  /**
   * Returns a hashmap version of the term-vector (bag of words) for this
   * document, where each token is a key whose value is the number of times
   * it occurs in the document as stored in a Weight.
   *
   * @see Weight
   */
  public HashMapVector hashMapVector2() {
    //Here, both nextToken1 and nextToken2 is used.
    if (numTokens != 0)
      return null;

    HashMapVector vector = new HashMapVector();
    prepareNextToken2();
    
    if(nextToken2 != null) nextToken2 = nextToken2.toLowerCase();
    if(nextToken1 != null) nextToken1 = nextToken1.toLowerCase();

    // Process each token in the document and add it to the vector
    while (hasMoreTokens2()) {
      String token = nextToken2();
      vector.increment(token);
    }
    return vector;
  }

  /**
   * Returns true iff the document contains more tokens
   */
  public boolean hasMoreTokens2() {
    if (nextToken1 == null || nextToken2 == null)
      return false;
    else
      return true;
  }

  /**
   * Returns the next token in the document or null if there are none
   */
  public String nextToken2() {
    String token = nextToken2.concat(" ").concat(nextToken1);
    if (token == null) return null;
    prepareNextToken2();
    numTokens++;
    return token.toLowerCase();
  }

  /**
   * The nextToken slot is always precomputed and stored by this method.
   * Performs stop-word removal of candidate tokens.
   */
  protected void prepareNextToken2() {
    // Loop until a non-stopword token is found
    nextToken2 = nextToken1;
    if(nextToken2 != null) nextToken2 = nextToken2.toLowerCase();
    
    do {
      nextToken1 = getNextCandidateToken();
      
      if (nextToken1 == null) return; // reached end of document
      // Normalize token string case to lower case.
      nextToken1 = nextToken1.toLowerCase();
      // Do not include a token found in the stopword list as
      // indexed in the stopwords hashtable.
      // Also do not include tokens that are not all Unicode letters
      if (stopWords.contains(nextToken1) || !allLetters(nextToken1))
        nextToken1 = null;
      else if (stem) {
        nextToken1 = stemmer.stripAffixes(nextToken1);
        if (stopWords.contains(nextToken1))
          nextToken1 = null;
      }
    }
    while (nextToken1 == null);
  }

  /**
   * Compute and print out (one line per term) the term-vector (bag of words)
   * for this document
   */
  public void printVector() {
    hashMapVector().print();
  }
}