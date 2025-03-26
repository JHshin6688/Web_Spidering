package ir.webutils;

import java.util.*;
import java.io.*;

/**
 * Node in the the Graph data structure.
 *
 * @see Graph
 *
 * @author Ray Mooney
 */
public class Node {

  /**
   * Name of the node.
   */
  String name;

  //PageRank Value (New variables)
  double pagerank;
  double pr_out;

  /**
   * Lists of incoming and outgoing edges.
   */
  List<Node> edgesOut = new ArrayList<Node>();
  List<Node> edgesIn = new ArrayList<Node>();

  /**
   * Constructs a node with that name.
   */
  public Node(String name) {
    this.name = name;
    //added
    this.pagerank = 0.0;
  }

  /**
   * Adds an outgoing edge
   */
  public void addEdge(Node node) {
    edgesOut.add(node);
    //node.addEdgeFrom(this);
  }

  /**
   * Adds an incoming edge
   */
  // Added 'public'
  public void addEdgeFrom(Node node) {
    edgesIn.add(node);
  }

  /**
   * Returns the name of the node
   */
  public String toString() {
    return name;
  }

  /**
   * Gives the list of outgoing edges
   */
  public List<Node> getEdgesOut() {
    return edgesOut;
  }

  /**
   * Gives the list of incoming edges
   */
  public List<Node> getEdgesIn() {
    return edgesIn;
  }

  // added methods
  public double getPageRank(){
    return pagerank;
  }

  public double getpr_out(){
    return pr_out;
  }

  public void modify_pr(double value){
    pagerank = value;
  }

  public void modify_pr_out(double value){
    pr_out = value;
  }

  public void add_pr(double value){
    pagerank += value;
  }

  public void div_pr(double value){
    pagerank /= value;
  }
}