package edu.ucsb.treaso;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Iterator;

public class TopicMining {

	HashMap<Integer,HashMap<Integer,Double>> topicDocumentProportion;
	
	TopicMining(File filetodeserialize)
	{
		try
	      {
	         FileInputStream fileIn =
	                          new FileInputStream(filetodeserialize);
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         topicDocumentProportion = (HashMap<Integer,HashMap<Integer,Double>>) in.readObject();
	         in.close();
	         fileIn.close();
	      }catch(IOException i)
	      {
	         i.printStackTrace();
	         return;
	      }catch(ClassNotFoundException c)
	      {
	         System.out.println("TopicDocumentProportionMap class not found");
	         c.printStackTrace();
	         return;
	      }
		 catch(Exception e){
				System.out.println(" Exception " );
				e.printStackTrace();
		 }
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TopicMining obj = new TopicMining(new File("topicDocumentProportion_php.ser"));
		obj.getTopicPopularity();
	}
	
	public void getTopicPopularity()
	{
		Iterator<Integer> itr = topicDocumentProportion.keySet().iterator();
		HashMap<Integer,Double> topicShare = new HashMap<Integer,Double>();
		while(itr.hasNext())
		{
			int tempTopic = itr.next();
			HashMap<Integer,Double> tempmap = topicDocumentProportion.get(tempTopic);
			Iterator<Double> itr1 = tempmap.values().iterator();
			Double sum = 0.0;
			while(itr1.hasNext())
			{
				sum += itr1.next();
			}
			sum /= tempmap.size();
			topicShare.put(tempTopic,sum);
		}
		
		Iterator<Integer> itr2 = topicShare.keySet().iterator();
		while(itr2.hasNext()){
			int temptopic = itr2.next();
			System.out.println("Topic "+temptopic+" :: "+topicShare.get(temptopic));
		}
	}
	

}
