package dependencyNegation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.iupui.pancyst.Constants;
import edu.iupui.pancyst.Utilities;

import edu.stanfordDependencyParser.StanfordDependencyParser;
import edu.iupui.NegEx.CallKit;
import edu.iupui.pancyst.DBReader;
import edu.stanford.nlp.trees.TypedDependency;

/***
 * Dependency Negation Analyzer class.
 * Contains methods to check presence or absence of 
 * terms/tokens in dependencies.
 */
public class DependencyNegationAnalyzer{

	private static boolean isPrepWithout = false;
	private static boolean isPrepOther = false;
	private static boolean isConjunction = false;
	private static boolean isNSubject = false;
	private static boolean isSuggest = false;
	private static boolean isNegationRoot = false;

	// Check if Negation is the root term.
	public static boolean isNegationRoot() {
		return isNegationRoot;
	}
	
	// Set if Negation is the root term.
	public static void setNegationRoot(boolean isNegationRoot) {
		DependencyNegationAnalyzer.isNegationRoot = isNegationRoot;
	}

	// Check if Dependency contains Preposition_Without
	public static boolean isPrepWithout() {
		return isPrepWithout;
	}

	// Set if Dependency contains Preposition_Without
	public static void setPrepWithout(boolean isWithout) {
		isPrepWithout = isWithout;
	}

	// Check if Dependency contains NSubject
	public static boolean isNSubject() {
		return isNSubject;
	}

	// Set if Dependency contains NSubject
	public static void setNSubject(boolean isNSubject) {
		DependencyNegationAnalyzer.isNSubject = isNSubject;
	}

	// Check if Dependency contains "Suggest"
	public static boolean isSuggest() {
		return isSuggest;
	}

	// Set if Dependency contains "Suggest"
	public static void setSuggest(boolean isSuggest) {
		DependencyNegationAnalyzer.isSuggest = isSuggest;
	}

	// Check if Dependency contains Preposition_Other
	public static boolean isPrepOther() {
		return isPrepOther;
	}

	// Set if Dependency contains Preposition_Other
	public static void setPrepOther(boolean isOther) {
		isPrepOther = isOther;
	}

	// Check if Dependency contains Conjunction
	public static boolean isConjunction() {
		return isConjunction;
	}

	// Set if Dependency contains Conjunction
	public static void setConjunction(boolean isConj) {
		isConjunction = isConj;
	}

	// Check if Dependency contains NSubj
	public static boolean isNominalSubject() {
		return isNSubject;
	}

	// Set if Dependency contains NSubj
	public static void setNominalSubject(boolean isNSubj) {
		isNSubject = isNSubj;
	}

	/**
	 * Main invoking method.
	 * @param args
	 */
	public static void main(String[] args)
	{
		String fileName = "~/test.txt";

		DependencyNegationAnalyzer dPa = new DependencyNegationAnalyzer();

		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(fileName));			
			String inputString = "";
			String outputString = "";
			while(null != (inputString = fileReader.readLine()))
			{
				System.out.println(inputString);
				outputString = dPa.AnalyzeDependencyNegation(inputString);
				System.out.println(outputString);	
			}

			fileReader.close();
		}		
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/***
	 * Main method to Analyze Dependency Negation.
	 * @param inputString
	 * @return Returns "Affirmed" or "Negated" for the sentence.
	 */
	public String AnalyzeDependencyNegation(String inputString) 
	{
		String outputString = "";
		String conceptTerm = inputString.split("\t")[0];		
		String sentence = inputString.split("\t")[1];
		
		List<String> specialCharacter = new ArrayList<String>();
		Pattern regexForSpecialCharacters = Pattern.compile("[^\\w\\s]");
		Matcher matcher = regexForSpecialCharacters.matcher(sentence);
		HashMap<String, String> negationDictionary = new HashMap<String, String>();
		
		while(matcher.find())
		{
			if(matcher.end() != sentence.length())
			{
				specialCharacter.add(sentence.substring(matcher.start(), matcher.end()));
			}
		}

		specialCharacter = RemoveDuplicates(specialCharacter);
		for(int specialCharIndex = 0; specialCharIndex < specialCharacter.size(); specialCharIndex++)
		{
			sentence = sentence.replace(specialCharacter.get(specialCharIndex), " " + specialCharacter.get(specialCharIndex) + " ");
		}

		sentence = sentence.replace("  ", " ");
		inputString = conceptTerm + "\t" + sentence;

		try {

			CallKit ck = new CallKit();
			String result = ck.CallKitMethod(inputString);
			String negationToken = "";

			if(result.equals("Negated"))
			{
				negationToken = ck.getNegationToken();
				String[] negationTokens = negationToken.split(" ");
				List<TypedDependency> sdpOutput = StanfordDependencyParser.Parser(sentence);
				String[][] sdpForSentence = new String[sdpOutput.size()][3];
				String sdpTemp = "";
				String[] dependencyParts = null;

				setPrepWithout(false);
				setPrepOther(false);				
				setConjunction(false);
				setSuggest(false);
				setNegationRoot(false);

				for (int sdpOutputIndex=0; sdpOutputIndex < sdpOutput.size(); sdpOutputIndex++)
				{	
					sdpTemp = sdpOutput.get(sdpOutputIndex).toString().replaceAll("," ," ");
					sdpTemp = sdpTemp.replaceAll("\\(", " ");
					sdpTemp = sdpTemp.replaceAll("\\)", " ");
					sdpTemp = sdpTemp.replaceAll("  ", " ");
					System.out.println(sdpTemp);
					dependencyParts = sdpTemp.split(" ");
					sdpForSentence[sdpOutputIndex] = dependencyParts;

					if(sdpTemp.split(" ")[0].equals("prep_without"))
					{
						setPrepWithout(true);
					}

					if(sdpTemp.split(" ")[0].equals("prep_in") || 
							sdpTemp.split(" ")[0].equals("prep_with") || 
							sdpTemp.split(" ")[0].equals("prep_within"))
					{
						setPrepOther(true);						
					}

					if(sdpTemp.split(" ")[0].equals("conj_and"))
					{
						setConjunction(true);
					}

					if(sdpTemp.split(" ")[0].equals("nsubj"))
					{
						setNominalSubject(true);
					}

					if(sdpTemp.split(" ")[1].split("-")[0].equals("suggest") || 
							sdpTemp.split(" ")[2].split("-")[0].equals("suggest") ||
							sdpTemp.split(" ")[1].split("-")[0].equals("suggests") || 
							sdpTemp.split(" ")[2].split("-")[0].equals("suggests"))
					{
						setSuggest(true);
					}	

					if(sdpTemp.split(" ")[0].split("-")[0].equals("root") && 
							negationToken.toLowerCase().contains(sdpTemp.split(" ")[2].split("-")[0].toLowerCase()))
					{
						setNegationRoot(true);
					}
				}
								
				// If conjunction is found, split sentence by "and" and work with two parts separately.
				if(isConjunction())
				{
					String[] sentenceParts = sentence.split("and");

					for(int sentencePartIndex = 0; sentencePartIndex < sentenceParts.length; sentencePartIndex++)
					{
						sentenceParts[sentencePartIndex] = (sentenceParts[sentencePartIndex] + ".").trim().replace("..", ".");
						result = ck.CallKitMethod(conceptTerm + "\t" + sentenceParts[sentencePartIndex]);

						if(result.equals("Negated"))
						{
							negationToken = ck.getNegationToken();
							negationTokens = negationToken.split(" ");
							System.out.println("sentenceParts :" + sentenceParts[sentencePartIndex]);
							sdpOutput = StanfordDependencyParser.Parser(sentenceParts[sentencePartIndex]);
							sdpForSentence = new String[sdpOutput.size()][3];

							setPrepWithout(false);
							setPrepOther(false);
							setNominalSubject(false);
							setSuggest(false);
							setNegationRoot(false);

							for (int sdpOutputIndex=0; sdpOutputIndex < sdpOutput.size(); sdpOutputIndex++)
							{
								sdpTemp = sdpOutput.get(sdpOutputIndex).toString().replaceAll("," ," ");
								sdpTemp = sdpTemp.replaceAll("\\(", " ");
								sdpTemp = sdpTemp.replaceAll("\\)", " ");
								sdpTemp = sdpTemp.replaceAll("  ", " ");
								dependencyParts = sdpTemp.split(" ");
								sdpForSentence[sdpOutputIndex] = dependencyParts;

								if(sdpTemp.split(" ")[0].equals("prep_without"))
								{
									setPrepWithout(true);
								}

								if(sdpTemp.split(" ")[0].equals("prep_in") || 
										sdpTemp.split(" ")[0].equals("prep_with") || 
										sdpTemp.split(" ")[0].equals("prep_within"))
								{
									setPrepOther(true);						
								}

								if(sdpTemp.split(" ")[0].equals("nsubj"))
								{
									setNominalSubject(true);
								}		

								if(sdpTemp.split(" ")[1].equals("suggest") || 
										sdpTemp.split(" ")[2].equals("suggest") ||
										sdpTemp.split(" ")[1].equals("suggests") || 
										sdpTemp.split(" ")[2].equals("suggests"))
								{
									setSuggest(true);
								}	

								if(sdpTemp.split(" ")[0].split("-")[0].equals("root") && 
										negationToken.toLowerCase().contains(sdpTemp.split(" ")[2].split("-")[0].toLowerCase()))
								{
									setNegationRoot(true);
								}
							}

							outputString = ProcessSentence(sdpForSentence, conceptTerm, negationTokens);

							if(outputString.contains("Negation"))
							{
								break;
							}
						}
						else
						{
							outputString = "Affirmed";
						}
					}					
				}
				else
				{
					outputString = ProcessSentence(sdpForSentence, conceptTerm, negationTokens);
				}
			}
			else
			{
				outputString = "Affirmed";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return outputString;
	}

	/***
	 * Sentence with concept term and negation tokens to check dependency.
	 * @param sdpForSentence
	 * @param conceptTerm
	 * @param negationTokens
	 * @return Production Chain of the Concept term and Negation tokens.
	 */
	private static String ProcessSentence(String[][] sdpForSentence, String conceptTerm, String[] negationTokens)
	{
		Boolean isConceptPresentInPrepOther = false; 
		boolean conceptPresent = false;
		String productionChain = "";
		String outputString = "";

		// If "Without" is present in the dependency.
		if(isPrepWithout())
		{
			StanfordDependencyParser.GetProductionChainForPrepWithoutAndRoot(sdpForSentence, Constants.PrepWithout);
			setPrepOther(false);
		}
		else
			if(isPrepOther())
			{
				isConceptPresentInPrepOther = StanfordDependencyParser.GetProductionChainForPrepOther(sdpForSentence, conceptTerm);						

				if(!isConceptPresentInPrepOther)
				{
					StanfordDependencyParser.GenerateProductionChain(sdpForSentence, negationTokens);
				}
			}
			else
			{
				StanfordDependencyParser.GenerateProductionChain(sdpForSentence, negationTokens);
			}

		// If Negation is the Root in the Dependency.
		if(isNegationRoot())
		{
			StanfordDependencyParser.GetProductionChainForPrepWithoutAndRoot(sdpForSentence, Constants.RootWord);
		}

		// If dependency contains NSubj tag.
		if(isNominalSubject())
		{
			StanfordDependencyParser.GenerateProductionChainForNSubj(sdpForSentence);
		}

		// If dependency contains "Suggest".
		if(isSuggest())
		{
			StanfordDependencyParser.GenerateProductionChainForSuggest(sdpForSentence, negationTokens);
		}

		// If dependency contains "without".
		if(isPrepWithout() || !isConceptPresentInPrepOther)
		{
			isConceptPresentInPrepOther = false;
			List<String> productionChainCollection =  StanfordDependencyParser.getProductionChainCollection();

			for(int productionChainCollectionIndex = 0; 
					productionChainCollectionIndex < productionChainCollection.size(); 
					productionChainCollectionIndex++)
			{
				productionChain = productionChainCollection.get(productionChainCollectionIndex);
				System.out.println("Prod Chain : " + productionChain);

				if(IsConceptPresent(productionChain, conceptTerm))
				{
					conceptPresent = true;
					break;
				}
			}	

			if(conceptPresent)
			{
				outputString = "Negation Confirmed by SDP";
			}
			else
			{
				outputString = "Affirmed by SDP";
			}

			productionChain = "";
		}
		else 
			if(isConceptPresentInPrepOther)
			{
				outputString = "Affirmed by SDP";;
			}
			else
			{
				outputString = "Negation Confirmed by SDP";
			}

		return outputString;
	}

	/***
	 * Method to check if the Concept is present in the Production Chain.
	 * @param productionChain
	 * @param conceptTerm
	 * @return True if concept term is present, else False.
	 */
	private static boolean IsConceptPresent(String productionChain, String conceptTerm) {

		String[] conceptTermParts = conceptTerm.split(" ");
		boolean conceptFound = false;
		String productionChainParts = productionChain.replaceAll("[\\)\\(]", " ");
		productionChainParts = productionChainParts.replaceAll("  ", " ");
		String[] allLevelParts = productionChainParts.trim().split(" ");
		String conceptTermPart = "";

		for(int conceptTermPartsIndex = 0; conceptTermPartsIndex < conceptTermParts.length; conceptTermPartsIndex++)
		{
			conceptTermPart = conceptTermParts[conceptTermPartsIndex];
			for(int allLevelPartsIndex = 0; allLevelPartsIndex < allLevelParts.length; allLevelPartsIndex++)
			{	  
				if(allLevelParts[allLevelPartsIndex].trim().equals(conceptTermPart))
				{
					conceptFound = true;
					break;
				}
			}

			if(conceptFound)
			{
				break;
			}
		}

		return conceptFound;
	}	

	/***
	 * Remove duplicate terms from list of String.
	 * @param itemList
	 * @return List containing unique terms.
	 */
	public List<String> RemoveDuplicates(List<String> itemList) {
		String tempItem = "";

		for(int itemIndex = 0; itemIndex < itemList.size(); itemIndex++)
		{		
			tempItem = itemList.get(itemIndex);
			for(int tempIndex = itemIndex + 1; tempIndex < itemList.size(); tempIndex++)
			{
				if(0 == itemList.get(tempIndex).compareToIgnoreCase(tempItem))
				{
					itemList.remove(tempIndex);
					itemIndex = -1;
					break;
				}
			}			
		}

		return itemList;
	}
}
