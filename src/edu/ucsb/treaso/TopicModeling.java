package edu.ucsb.treaso;

import cc.mallet.util.*;
import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;

import java.util.*;
import java.util.regex.*;
import java.io.*;

public class TopicModeling {

	public ArrayList<String> listFilesForFolder(final File folder) {
		ArrayList<String> listofFiles = new ArrayList<String>();
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry);
	        } else {
	            //System.out.println(fileEntry.getName());
	        	listofFiles.add(fileEntry.getName());
	        }
	    }
	    return listofFiles;
	}
	
	public static void main(String[] args) throws Exception {

		TopicModeling obj = new TopicModeling();
		// Begin by importing documents from text to feature sequences
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		// Pipes: lowercase, tokenize, remove stopwords, map to features
		pipeList.add(new File2CharSequence());
		pipeList.add( new CharSequenceLowercase() );
		pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
		pipeList.add( new TokenSequenceRemoveStopwords(new File("stoplists/en.txt"), "UTF-8", false, false, false) );
		pipeList.add(new TokenSequencePortersAlgorithm());
	//	new TokenSequenceRemoveStopwords(stoplistFile, encoding, includeDefault, caseSensitive, markDeletions)
		pipeList.add( new TokenSequence2FeatureSequence() );

		InstanceList instances = new InstanceList (new SerialPipes(pipeList));

		//replace below with folder containing files generated from posts.xml
		String prefix = "/home/sivabalan/search/data/textfiles/";
		File folder = new File(prefix);
		//listFilesForFolder(folder);
		ArrayList<String> listofFiles = obj.listFilesForFolder(folder);
		String[] fileList = new String[listofFiles.size()];
		int tempcount = 0;
		for(String str: listofFiles)
			fileList[tempcount++] = prefix+str;
		
		//for(String str: listofFiles){
		//	System.out.println(" "+str);
		//Reader fileReader = new InputStreamReader(new FileInputStream(new File("/home/sivabalan/search/data/textfiles/"+str)), "UTF-8");
     	//	Reader fileReader = new InputStreamReader(new FileInputStream(new File("/home/sivabalan/search/data/textfiles/"+"44.txt")), "UTF-8");
		//instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)(.*[\\S]*)$",Pattern.DOTALL),
			//								   2, 1, 1)); // data, label, name fields
		instances.addThruPipe( new FileListIterator(fileList, null,Pattern.compile("(.*)") , false));
	    //	fileReader.close();
		//}
		//new CsvIterator(input, lineRegex, dataGroup, targetGroup, uriGroup)
		
		// Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
		//  Note that the first parameter is passed as the sum over topics, while
		//  the second is 
		int numTopics = 20;
		ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);
		//new ParallelTopicModel(numberOfTopics, alphaSum, beta)

		model.addInstances(instances);

		// Use two parallel samplers, which each look at one half the corpus and combine
		//  statistics after every iteration.
		model.setNumThreads(2);

		// Run the model for 50 iterations and stop (this is for testing only, 
		//  for real applications, use 1000 to 2000 iterations)
		model.setNumIterations(50);
		System.out.println("Estimate  -------------------------- ");
		model.estimate();
		System.out.println(" ************************************ ");
		HashMap<Integer,HashMap<Integer,Double>> topicDocumentMap = model.getDocumentTopics();
		Iterator<Integer> itr = topicDocumentMap.keySet().iterator();
		
		//HashMap<Integer,HashMap<String,Double>> topicDocumentMap = model.getDocumentTopicsString();
		//Iterator<Integer> itr = topicDocumentMap.keySet().iterator();
		
		int count = 0;
		while(itr.hasNext())
		{
			int temptopic = itr.next();
			System.out.println("Topic "+temptopic);
			HashMap<Integer,Double> documentProportionMap = topicDocumentMap.get(temptopic);
			Iterator<Integer> itr2 = documentProportionMap.keySet().iterator();
			while(itr2.hasNext())
			{
				int tempdoc = itr2.next();
				System.out.println("    "+tempdoc+" :: "+documentProportionMap.get(tempdoc));
			}
			System.out.println("");
			count++;
		}
		/*
		try
	      {
	         FileOutputStream fileOut =
	         new FileOutputStream("topicDocumentProportion.ser");
	         ObjectOutputStream out =
	                            new ObjectOutputStream(fileOut);
	         out.writeObject(topicDocumentMap);
	         out.close();
	          fileOut.close();
	      }catch(IOException i)
	      {
	          i.printStackTrace();
	      }
		*/
		
		System.out.println(" ************************************ ");
		// Show the words and topics in the first instance

		// The data alphabet maps word IDs to strings
		/*Alphabet dataAlphabet = instances.getDataAlphabet();
		System.out.println(" ------------------------------------------------ ");
	    ArrayList<TopicAssignment> items = model.getData();
		for (TopicAssignment item : items)
			System.out.println("TTopic "+item.topicDistribution+" - "+item.topicSequence+" - "+item.toString()+" - "+item.instance.getData());
		System.out.println(" ************************ ");
		FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
		LabelSequence topics = model.getData().get(0).topicSequence;
		
		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		for (int position = 0; position < tokens.getLength(); position++) {
			out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
		}
		System.out.println(out);
		//born-44 indian-95 film-6 actress-77 appeared-44 hindi-73 films-65 bollywood-39 telugu-5 english-language-48 movies-48 graduating-9
		
		
		// Estimate the topic distribution of the first instance, 
		//  given the current Gibbs state.
		double[] topicDistribution = model.getTopicProbabilities(0);
	//	model.getData().get(1).topicDistribution.
		// Get an array of sorted sets of word ID/count pairs
		ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
		
	//	System.out.println(" Show top 5 words in topics ********************** ");
		/*
		 * 0	0.054	team (1) premier (1) kabhi (1) noted (1) lead (1) 
		 * 1	0.000	
		 * 2	0.031	indian (3) image (1) 
		 * 3	0.000	
		 */
		// Show top 5 words in topics with proportions for the first document
	/*	for (int topic = 0; topic < numTopics; topic++) {
			Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
			
			out = new Formatter(new StringBuilder(), Locale.US);
			out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
			int rank = 0;
			while (iterator.hasNext() && rank < 5) {
				IDSorter idCountPair = iterator.next();
				out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
				rank++;
			}
			System.out.println(out);
		}
	    System.out.println("*********************************** ");
	    
		// Create a new instance with high probability of topic 0
		StringBuilder topicZeroText = new StringBuilder();
		Iterator<IDSorter> iterator = topicSortedWords.get(0).iterator();

		int rank = 0;
		while (iterator.hasNext() && rank < 5) {
			IDSorter idCountPair = iterator.next();
			topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
			rank++;
		}

		// Create a new instance named "test instance" with empty target and source fields.
		InstanceList testing = new InstanceList(instances.getPipe());
		testing.addThruPipe(new Instance(topicZeroText.toString(), null, "test instance", null));

		TopicInferencer inferencer = model.getInferencer();
		double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
		System.out.println("0\t" + testProbabilities[0]);
		*/
	}

}
