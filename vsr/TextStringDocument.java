package ir.vsr;

import java.io.*;
import java.util.*;

/**
 * A simple document represented by a String
 *
 * @author Ray Mooney
 */
public class TextStringDocument extends Document {

  /**
   * StringTokenizer delim for tokenizing only alphabetic strings.
   */
  public static final String tokenizerDelim = " \t\n\r\f\'\"\\1234567890!@#$%^&*()_+-={}|[]:;<,>.?/`~";

  /**
   * The tokenizer for this document.
   */
  protected StringTokenizer tokenizer = null;
  public static String query_string = null;

  /**
   * Create a simple Document for this string
   */
  // I modified this part in order to make sure that the both query options (one-word / two-word) are available.
  public TextStringDocument(String string, boolean stem, boolean phrase) {
    super(stem);
    this.tokenizer = new StringTokenizer(string, tokenizerDelim);

    if(phrase){
      prepareNextToken();
      query_string = nextToken1;
      prepareNextToken();
      query_string += " " + nextToken1;
      nextToken1 = query_string;
    }

    else{
      prepareNextToken();
    }
  }

  /**
   * Get the next token from this string
   */
  protected String getNextCandidateToken() {
    if (tokenizer == null || !tokenizer.hasMoreTokens())
      return null;
    return tokenizer.nextToken();
  }

  /**
   * For testing, print the bag-of-words vector for the given string
   */
  public static void main(String[] args) throws IOException {
    String input = args[0];
    Document doc = new TextStringDocument(input, false, false);
    doc.printVector();
    System.out.println("\nNumber of Tokens: " + doc.numberOfTokens());
  }

}
