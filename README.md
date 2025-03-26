# Web_Spidering

## Overview
This project extends a basic web spidering and indexing system to incorporate PageRank-based relevance scoring for web search. It builds on the foundational components provided in the ir.webutils package, which includes tools for crawling web pages, saving them to disk, and indexing them for information retrieval

The main objective is to develop a more intelligent web spider and indexer that accounts for the link structure of web pages, enabling more effective and relevant search capabilities through the incorporation of PageRank scores.

## Key Objectives
### 1. Develop a PageRank-Aware Web Spider
Implement PageRankSpider, a subclass of the provided Spider class.

While spidering, collect a link graph from the pages that are actually indexed (i.e., saved to disk).

Use ir.webutils.Graph and Node classes to manage the link structure.

##### Compute PageRank values using:

&nbsp;&nbsp;&nbsp;&nbsp;**-Damping factor (alpha)**: 0.15

&nbsp;&nbsp;&nbsp;&nbsp;**-Number of iterations**: 50

&nbsp;&nbsp;&nbsp;&nbsp;Output PageRank scores to a file named page_ranks.txt in the crawl directory:


### 2. Modify the Inverted Index to Utilize PageRank
Create PageRankInvertedIndex, a subclass of InvertedIndex.

During scoring, read PageRank values from page_ranks.txt.

Incorporate PageRank into document scores using a weight parameter passed via the -weight command-line argument.


### 3. Build Specialized Crawlers
Develop PageRankSiteSpider, a PageRank-based crawler restricted to a specific site or directory.

Use it to crawl a custom network of student-generated class pages.

### Copyleft
This code supplies "miniature" pedagogical Java implementations of
information retrieval, spidering, and other IR and text-processing
software.  It is being released for educational and research purposes only under
the GNU General Public License (see http://www.gnu.org/copyleft/gpl.html).

It was developed for an introductory course on "Intelligent Information
Retrieval and Web Search".  See:
