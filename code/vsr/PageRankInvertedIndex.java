package ir.vsr;

import ir.vsr.InvertedIndex;

import java.io.*;
import java.util.*;
import java.lang.*;

import ir.utilities.*;
import ir.classifiers.*;

public class PageRankInvertedIndex extends InvertedIndex {

    public double weight = 0.0;
    //HashMap to save the filename and its pagerank value
    public HashMap<String, Double> pageRankMap;

    public PageRankInvertedIndex(File dirFile, short docType, boolean stem, boolean feedback, Double weight){
        super(dirFile, docType, stem, feedback);
        this.weight = weight;
        pageRankMap = new HashMap<String, Double>();
        loadPageRank("./indexed/page_ranks.txt");
    }

// load the pagerank values from the file and
// add the filename-pagerank entry to pageRankMap
  protected void loadPageRank(String filePath){
      try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
          String line;
          while ((line = br.readLine()) != null) {
              String[] parts = line.split(" ");
              if (parts.length == 2) {
                  String fileName = parts[0];
                  double pageRank = Double.parseDouble(parts[1]);
                  pageRankMap.put(fileName, pageRank);
              }
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
  }

// Add the functionality to skip the page_ranks.txt file for indexing
protected void indexDocuments() {
  if (!tokenHash.isEmpty() || !docRefs.isEmpty()) {
    // Currently can only index one set of documents when an index is created
    throw new IllegalStateException("Cannot indexDocuments more than once in the same InvertedIndex");
  }
  // Get an iterator for the documents
  DocumentIterator docIter = new DocumentIterator(dirFile, docType, stem);
  System.out.println("Indexing documents in " + dirFile);

  while (docIter.hasMoreDocuments()) {
    FileDocument doc = docIter.nextDocument();
    //Skip indexing for page_ranks.txt file
    if(doc.file.getName().equals("page_ranks.txt")) continue;

    // Create a document vector for this document
    System.out.print(doc.file.getName() + ",");
    HashMapVector vector = doc.hashMapVector();
    indexDocument(doc, vector);
  }
  // Now that all documents have been processed, we can calculate the IDF weights for
  // all tokens and the resulting lengths of all weighted document vectors.
  computeIDFandDocumentLengths();
  System.out.println("\nIndexed " + docRefs.size() + " documents with " + size() + " unique terms.");
}

  protected Retrieval getRetrieval(double queryLength, DocumentReference docRef, double score) {
    // Normalize score for the lengths of the two document vectors
    score = score / (queryLength * docRef.length);
    String doc_name = docRef.toString();
    // Add the pagerank of document scaled by weight to the score
    score += pageRankMap.get(doc_name) * weight;

    // Add a Retrieval for this document to the result array
    return new Retrieval(docRef, score);
  }

public static void main(String[] args) {
    // Parse the arguments into a directory name and optional flag

    String dirName = args[args.length - 1];
    short docType = DocumentIterator.TYPE_TEXT;
    boolean stem = false, feedback = false;
    double weight = 0.0;

    for (int i = 0; i < args.length - 1; i++) {
      String flag = args[i];
      if (flag.equals("-html"))
        // Create HTMLFileDocuments to filter HTML tags
        docType = DocumentIterator.TYPE_HTML;
      else if (flag.equals("-stem"))
        // Stem tokens with Porter stemmer
        stem = true;
      else if (flag.equals("-feedback"))
        // Use relevance feedback
        feedback = true;
    
      else if (flag.equals("-weight")) {
        weight = Double.parseDouble(args[++i]);
      }

      else {
        throw new IllegalArgumentException("Unknown flag: "+ flag);
      }
    }

    // Create an inverted index for the files in the given directory.
    PageRankInvertedIndex index = new PageRankInvertedIndex(new File(dirName), docType, stem, feedback, weight);
    // index.print();
    // Interactively process queries to this index.
    index.processQueries();
  }

}