package dependencyNegation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.io.StringReader;
import java.lang.invoke.ConstantCallSite;

import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.ling.CoreLabel;  
import edu.stanford.nlp.ling.HasWord;  
import edu.stanford.nlp.ling.Sentence;  
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

/***
 * Methods to deal with sentences with dependency parsing.
 * Identifying the central term and connecting with all dependent terms.
 */
public class StanfordDependencyParser {

	private static String[][] sdpForSentence = {};
	
	// Collection of Production Chains.
	private static List<String> ProductionChainCollection = new ArrayList<String>();

	// Get Production Chain for Sentence.
	public static List<String> getProductionChainCollection() {
		return ProductionChainCollection;
	}

	// Set Production Chain for Sentence.
	public static void setProductionChain(String productionChain) {
		ProductionChainCollection.add(productionChain);
	}

	// Get Dependency for Sentence.
	public static String[][] getSdpForSentence() {
		return sdpForSentence;
	}

	// Set Dependency for Sentence.
	public static void setSdpForSentence(String[][] sdpForSentence) {
		StanfordDependencyParser.sdpForSentence = sdpForSentence;
	}

	/***
	 * Method accepts sentence and returns Dependency.
	 * @param sentence
	 * @return Dependency of the sentence.
	 */
	public static List<TypedDependency> Parser(String sentence) {
		// This option shows parsing a list of correctly tokenized words
		String[] sent = sentence.split(" ");
		LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");

		List<CoreLabel> rawWords = Sentence.toCoreLabelList(sent);
		Tree parse = lp.apply(rawWords);

		// This option shows loading and using an explicit tokenizer
		String sent2 = sentence;
		TokenizerFactory<CoreLabel> tokenizerFactory = 
				PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
		List<CoreLabel> rawWords2 = 
				tokenizerFactory.getTokenizer(new StringReader(sent2)).tokenize();
		parse = lp.apply(rawWords2);

		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();

		TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");

		return tdl;
	}

	/***
	 * Generate Production chain showing relationship between Negation token and 
	 * words in the sentence.
	 * @param sdpForSentence
	 * @param negationTokens
	 */
	public static void GenerateProductionChain(String[][] sdpForSentence1,
			String[] negationTokens) {

		sdpForSentence = sdpForSentence1;
		ResetProductionChainCollection();
		String startProductionChain = "";
		String productionChain = "";
		String tempProductionChain = "";
		startProductionChain = "(" + DisplayTokens(negationTokens) + ")";

		// For all the Negation terms found in the Sentence, construct a production chain.
		for(int negationTokenIndex = 0;negationTokenIndex < negationTokens.length; negationTokenIndex++)
		{
			// Fetch the Tokens from the First Level.
			String[] firstLevelTokens = GetFirstLevelTokens(negationTokens[negationTokenIndex]);			
			productionChain = startProductionChain + " (" + DisplayTokens(firstLevelTokens) + ") ";

			for(int firstLevelTokensIndex = 0;firstLevelTokensIndex < firstLevelTokens.length; firstLevelTokensIndex++)
			{	
				// Fetch the Tokens from the Second Level.
				String[] secondLevelTokens = GetSecondLevelTokens(firstLevelTokens[firstLevelTokensIndex]);
				productionChain += "(" + DisplayTokens(secondLevelTokens) + ") ";
				tempProductionChain = productionChain;
				for(int secondLevelTokensIndex = 0;secondLevelTokensIndex < secondLevelTokens.length; secondLevelTokensIndex++)
				{
					// Fetch the Tokens from the Third Level.
					String[] thirdLevelTokens = GetThirdLevelTokens(secondLevelTokens[secondLevelTokensIndex]);					

					if("" != thirdLevelTokens[0])
					{
						productionChain = tempProductionChain + "(" + DisplayTokens(thirdLevelTokens) + ") ";
						setProductionChain(productionChain);
					}
					else
					{
						productionChain = tempProductionChain;
						setProductionChain(productionChain);
					}
				}				
			}
		}
	}

	/***
	 * Reset Production Chain.
	 */
	private static void ResetProductionChainCollection() {
		ProductionChainCollection = new ArrayList<String>();		
	}

	/***
	 * Display all tokens.
	 * @param tokens
	 * @return Concatenation of all tokens.
	 */
	private static String DisplayTokens(String[] tokens) {

		String tempToken = "";

		for(int tokenIndex = 0; tokenIndex < tokens.length; tokenIndex++)
		{
			tempToken += tokens[tokenIndex] + " ";
		}

		return tempToken.trim();
	}

	/***
	 * Collect all the First level tokens from the Negation Term from the 
	 * Dependency.
	 * @param negationToken
	 * @return First level tokens.
	 */
	public static String[] GetFirstLevelTokens(String negationToken) {

		String firstLevelTokens = "";
		String productionChainElement;
		for(int sdpForSentenceIndex = 0; sdpForSentenceIndex < sdpForSentence.length; sdpForSentenceIndex++)
		{
			productionChainElement = sdpForSentence[sdpForSentenceIndex][2].split("-")[0];
			if(negationToken.equalsIgnoreCase(productionChainElement))
			{				
				firstLevelTokens += sdpForSentence[sdpForSentenceIndex][1].split("-")[0] + " ";
				RemoveChainItem(sdpForSentenceIndex);
				sdpForSentenceIndex = 0;
			}
		}

		return firstLevelTokens.split(" ");
	}

	/***
	 * Collect all the Second level tokens from the First level tokens Term from the 
	 * Dependency.
	 * @param negationToken
	 * @return Second level tokens.
	 */
	public static String[] GetSecondLevelTokens(String firstLevelToken) {

		String secondLevelTokens = "";
		String productionChainElement;
		for(int sdpForSentenceIndex = 0; sdpForSentenceIndex < sdpForSentence.length; sdpForSentenceIndex++)
		{
			productionChainElement = sdpForSentence[sdpForSentenceIndex][1].split("-")[0];
			if(firstLevelToken.equalsIgnoreCase(productionChainElement))
			{				
				secondLevelTokens += sdpForSentence[sdpForSentenceIndex][2].split("-")[0] + " ";
				RemoveChainItem(sdpForSentenceIndex);
				sdpForSentenceIndex = 0;
			}
		}

		return secondLevelTokens.split(" ");
	}

	/***
	 * Collect all the Third level tokens from the Second level tokens Term from the 
	 * Dependency.
	 * @param negationToken
	 * @return Third level tokens.
	 */
	public static String[] GetThirdLevelTokens(String secondLevelToken) {

		String thirdLevelTokens = "";
		String productionChainElement;
		for(int sdpForSentenceIndex = 0; sdpForSentenceIndex < sdpForSentence.length; sdpForSentenceIndex++)
		{
			productionChainElement = sdpForSentence[sdpForSentenceIndex][1].split("-")[0];
			if(secondLevelToken.equalsIgnoreCase(productionChainElement))
			{				
				thirdLevelTokens += sdpForSentence[sdpForSentenceIndex][2].split("-")[0] + " ";
				RemoveChainItem(sdpForSentenceIndex);
				sdpForSentenceIndex = 0;
			}
		}

		return thirdLevelTokens.split(" ");
	}

	/***
	 * Once a dependency has been reached, remove the dependency from the dependency list.
	 * @param sdpForSentenceIndex Index of the Dependency.
	 */
	private static void RemoveChainItem(int sdpForSentenceIndex) {

		String[][] tempSdpForSentence = new String[sdpForSentence.length - 1][3];

		for(int tempSdpForSentenceIndex = 0, index = 0; tempSdpForSentenceIndex < sdpForSentence.length; tempSdpForSentenceIndex++ )
		{
			if(sdpForSentenceIndex != tempSdpForSentenceIndex)
			{
				tempSdpForSentence[index][0] = sdpForSentence[tempSdpForSentenceIndex][0];
				tempSdpForSentence[index][1] = sdpForSentence[tempSdpForSentenceIndex][1];
				tempSdpForSentence[index++][2] = sdpForSentence[tempSdpForSentenceIndex][2];				
			}
		}

		sdpForSentence = tempSdpForSentence;		
	}

	/***
	 * Temporary method to display all Dependencies.
	 */
	private static void DisplaySDPForSentence() {
		for (int sdpOutputIndex=0; sdpOutputIndex < sdpForSentence.length; sdpOutputIndex++)
		{
			System.out.println(sdpForSentence[sdpOutputIndex][0] + " " + sdpForSentence[sdpOutputIndex][1] + " " +sdpForSentence[sdpOutputIndex][2]);
		}	

		System.out.println(" ");
	}
}
