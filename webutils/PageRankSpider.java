package ir.webutils;

import java.util.*;

import java.net.*;
import java.io.*;
import ir.utilities.*;
import ir.webutils.Graph;
import ir.webutils.HTMLPage;
import ir.webutils.Link;
import ir.webutils.Node;
import ir.webutils.PathDisallowedException;
import ir.webutils.LinkExtractor;
import ir.webutils.Spider;

public class PageRankSpider extends Spider {
    protected Graph graph;
    
    //Mapping for the link name and the file name
    Map<String, String> pageMap = new HashMap<String, String>();

    public void go(String[] args) {
        graph = new Graph();
        processArgs(args);
        doCrawl(graph);

        //Delete every node that doesn't have an outgoing edge in the graph structure
        graph.resetIterator();
        Node node = graph.nextNode();
        
        while(node != null){
          Iterator<Node> iterator = node.getEdgesOut().iterator();
          while (iterator.hasNext()) {
              Node out = iterator.next();
              if (graph.getExistingNode(out.toString()) == null) {
                  iterator.remove();
              }
          }
          node = graph.nextNode();
        }

        System.out.println("Graph structure: ");
        graph.print();
        pagerank(graph);
    }

    public void doCrawl(Graph graph) {
        if (linksToVisit.size() == 0) {
          System.err.println("Exiting: No pages to visit.");
          System.exit(0);
        }
        visited = new HashSet<Link>();
        String filename;

        while (linksToVisit.size() > 0 && count < maxCount) {
          // Pause if in slow mode
          if (slow) {
            synchronized (this) {
              try {
                wait(1000);
              }
              catch (InterruptedException e) {
              }
            }
          }
          // Take the top link off the queue
          Link link = linksToVisit.remove(0);
          link.cleanURL(); // Standardize and clean the URL for the link
          System.out.println("Trying: " + link);
          // Skip if already visited this page
          if (!visited.add(link)) {
            System.out.println("Already visited");
            continue;
          }
          if (!linkToHTMLPage(link)) {
            System.out.println("Not HTML Page");
            continue;
          }
          HTMLPage currentPage = null;
          // Use the page retriever to get the page
          try {
            currentPage = retriever.getHTMLPage(link);
          }
          catch (PathDisallowedException e) {
            System.out.println(e);
            continue;
          }
          if (currentPage.empty()) {
            System.out.println("No Page Found");
            continue;
          }
          if (currentPage.indexAllowed()) {
            count++;
            System.out.println("Indexing" + "(" + count + "): " + link);
            
            //save the link-filename entry to pageMap
            filename = indexPage(currentPage);
            pageMap.put(link.toString(), filename);
          }
          if (count <= maxCount) {
            List<Link> newLinks = getNewLinks(currentPage);
            //clean url before adding it to the graph
            link.cleanURL();
            String xName = link.toString();
            Node node1 = graph.getNode(xName);
            
            for (Link newlink : newLinks){
                //clean url before adding it to the graph
                newlink.cleanURL();
                
                //pass the links that are not .html
                if(!linkToHTMLPage(newlink)) continue;

                String yName = newlink.getURL().toString();
                //Eliminate self-loop
                if(xName.equals(yName)) continue;

                //Add the node only when it don't exist in the edgesOut list
                Node node2 = new Node(yName);
                boolean exists = false;
                for (Node node : node1.getEdgesOut()) {
                    if (node.toString().equals(yName)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) node1.addEdge(node2);   
              }
            linksToVisit.addAll(newLinks);
          }
        }
      }

    //Calculating pagerank values for nodes in graph
    public void pagerank(Graph graph){
        int count = 0;
        Node[] nodes = graph.nodeArray();
        int num_nodes = nodes.length;
        double random_jump = 0.15 / num_nodes;
        
        //Add Incoming Edges to nodes in graph
        graph.resetIterator();
        Node node = graph.nextNode();
        while(node != null){
          for (Node node_out : node.getEdgesOut()){
            //Add the node in the edgesIn
            if(graph.getExistingNode(node_out.toString()) != null){
              graph.getNode(node_out.toString()).addEdgeFrom(node);
            }
          }
          node = graph.nextNode();
        }

        //Initialize pagerank values
        graph.resetIterator();
        node = graph.nextNode();
        while(node != null){
            node.modify_pr(1.0 / num_nodes);
            node = graph.nextNode();
        }

        //Iterate pagerank algorithm       
        while(count < 50){
            System.out.println("Iteration " + count);

            graph.resetIterator();            
            Node helper = graph.nextNode();
            while(helper != null){
              double num_out = (double) helper.getEdgesOut().size();
              if(num_out != 0.0) helper.modify_pr_out(helper.getPageRank() / num_out);
              else helper.modify_pr_out(0.0);
              helper = graph.nextNode();
            }

            graph.resetIterator();
            helper = graph.nextNode();
            while(helper != null){
                helper.modify_pr(0.0);
                for(Node in_node : helper.getEdgesIn()){
                    helper.add_pr(0.85 * graph.getNode(in_node.toString()).getpr_out());
                    //helper.add_pr(0.85 * in_node.getpr_out());
                }
                helper.add_pr(random_jump);
                helper = graph.nextNode();
            }

            //Unnormalized
            graph.resetIterator();
            helper = graph.nextNode();
            double pagerank_total = 0.0;
            while(helper != null){
                System.out.println("Unnormalized P = " + String.format("%.5f", helper.getPageRank()) + " | " + helper.toString());
                pagerank_total += helper.getPageRank();
                helper = graph.nextNode();
            }

            //Normarlized
            graph.resetIterator();
            helper = graph.nextNode();
            while(helper != null){
                helper.div_pr(pagerank_total);
                System.out.println("Normalized P = " + String.format("%.5f", helper.getPageRank()) + " | " + helper.toString());
                helper = graph.nextNode();
            }       
            count += 1;
        }

        //Final PR values
        System.out.println("");
        System.out.println("PageRank:");
        graph.resetIterator();
        Node helper = graph.nextNode();
        while(helper != null){
            System.out.println(String.format("PR(%s): %.5f", helper.toString(), helper.getPageRank()));
            helper = graph.nextNode();
        }
        
        //Write page_ranks.txt in indexed/ directory
        File pagerank = new File(saveDir, "page_ranks.txt");
        graph.resetIterator();
        helper = graph.nextNode();

        try {
          FileWriter writer = new FileWriter(pagerank);
          while(helper != null){
            writer.write(pageMap.get(helper.toString()) + " " +  helper.getPageRank() + "\n");
            helper = graph.nextNode();
          }
          writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        new PageRankSpider().go(args);
    }
}
