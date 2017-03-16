# deepen
Automatically exported from code.google.com/p/deepen
This method uses a combination of Negex and Dependency Parsing technique that developes an improvement to the Negex algorithm. 
This work has been published under the following link.
http://www.sciencedirect.com/science/article/pii/S153204641500043X

To use the DEEPEN you only need the two following lines of code:
DependencyNegationAnalyzer g       = new DependencyNegationAnalyzer();
g.AnalyzeDependencyNegation(Concept+"\t"+Sentence)

Input: a sentence with  indicated clinical condition (concept) from the sentence.
Output: String that identifies the negation status of the concept in the sentence,
the code only double checks the concepts that are considered negated by the NegEx algorithm so there are 3 outputs
Affirmed: meaning the concept is not negated as found by NegEx
Affirmed confirmed by SDP: NegEx found the concept as negated but DEEPEN considers it affirmed 
Negation confirmed by SDP: NegEx found the concept as negated and DEEPEN  confirms that.
