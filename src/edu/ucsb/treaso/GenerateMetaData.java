package edu.ucsb.treaso;

import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

//import com.sun.org.apache.xml.internal.serializer.ToStream;


public class GenerateMetaData extends DefaultHandler {
	List<Post> postList;
	String postXmlFileName;
	String tmpValue;
	Post postTemp;
	Map<Integer,Post> postMap;
	Map<String,TagPost> tagPostMap;

	public GenerateMetaData(String postXmlFileName) {
		this.postXmlFileName = postXmlFileName;
		postMap = new HashMap<Integer,Post>();
		tagPostMap  = new HashMap<String,TagPost>();
		postList = new ArrayList<Post>();
		parseDocument();
		//  printDatas();
	}
	private void parseDocument() {
		// parse
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			parser.parse(postXmlFileName, this);
		} catch (ParserConfigurationException e) {
			System.out.println("ParserConfig error");
		} catch (SAXException e) {
			System.out.println("SAXException : xml not well formed");
		} catch (IOException e) {
			System.out.println("IO error");
		}
	}
	private void printDatas() {
		// System.out.println(bookL.size());
		for (Post tmpB : postList) {
			System.out.println(tmpB.toString());
		}
	}
	@Override
	public void startElement(String s, String s1, String elementName, Attributes attributes) throws SAXException {
		// if current element is book , create new book
		// clear tmpValue on start of element

		int temppostid;
		if (elementName.equalsIgnoreCase("row")) {
			//System.out.println("Inside if");
			if(Integer.parseInt(attributes.getValue("PostTypeId")) ==1) {
				postTemp = new Post();
				temppostid = Integer.parseInt(attributes.getValue("Id"));
				postTemp.setPostId(temppostid);
				postTemp.setCreationDate((attributes.getValue("CreationDate").substring(0,10)));
				//postTemp.setTags(attributes.getValue("Tags"));
				String tags = attributes.getValue("Tags");
				String[] tagList = tags.split("><|<|>");

				for(String item: tagList){
					if(!item.equalsIgnoreCase("") && !item.equalsIgnoreCase(" "))
						//System.out.print(" "+item);
						if(tagPostMap.containsKey(item))
						{
							TagPost temptagPost = tagPostMap.get(item);
							temptagPost.addPostId(temppostid);
							temptagPost.incrementScore();
							tagPostMap.put(item, temptagPost);
						}
						else{
							TagPost temptagPost = new TagPost();
							temptagPost.addPostId(temppostid);
							temptagPost.incrementScore();
							tagPostMap.put(item, temptagPost);
						}
				}
				System.out.println();
				postTemp.setTagsList(new ArrayList<String>(Arrays.asList(tagList)));	
				postTemp.setViewCount(Integer.parseInt(attributes.getValue("ViewCount")));
				postList.add(postTemp);
				//System.out.println(" "+postTemp.getPostId()+" "+postTemp.getCreationDate()+" "+postTemp.getTags());
				if(postMap.containsKey(postTemp))
				{
					System.out.println(" Code unreachable ************** ");
				}
				else{
					postMap.put(temppostid,postTemp);
				}
			}
			else{
				int parent = Integer.parseInt(attributes.getValue("ParentId"));
				Post temppost = postMap.get(parent);
				temppost.addNewChild(Integer.parseInt(attributes.getValue("Id")));
				postMap.put(parent, temppost);
			}
		}


	}


	public void printPostMap()
	{
		Iterator<Integer> itr = postMap.keySet().iterator();
		while(itr.hasNext())
		{
			int tempparent = itr.next();
			Post temppost = postMap.get(tempparent);
			System.out.println("Parent "+tempparent);
			System.out.println("Attributes "+temppost);
			System.out.println();
		}
	}

	public void printTagPostMap()
	{
		Iterator<String> itr = tagPostMap.keySet().iterator();
		while(itr.hasNext())
		{
			String temptag = itr.next();
			TagPost tempTagPost = tagPostMap.get(temptag);
			System.out.println("Tag "+temptag);
			System.out.println("Value "+tempTagPost);
		}
	}

	public void serialize_stuff() {
		try {
			FileOutputStream fileOut = new FileOutputStream("tagPostMap.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(tagPostMap);
			FileOutputStream fileOut1 = new FileOutputStream("QPostMap.ser");
			ObjectOutputStream out1 = new ObjectOutputStream(fileOut1);
			out1.writeObject(postMap);
			out.close();
			fileOut.close();   
			out1.close();
			fileOut1.close();

		}catch(IOException i)
		{
			i.printStackTrace();
		}
	}

	public void deserialize_stuff() throws ClassNotFoundException {
		try {
			FileInputStream fileOut = new FileInputStream("tagPostMap.ser");
			ObjectInputStream out = new ObjectInputStream(fileOut);
			Map<Integer,TagPost> tagpostmap =  (HashMap<Integer,TagPost>)out.readObject();
			System.out.println(" "+tagpostmap);
			FileInputStream fileOut1 = new FileInputStream("QPostMap.ser");
			ObjectInputStream out1 = new ObjectInputStream(fileOut1);
			Map<Integer,Post> postmap =(Map<Integer,Post>) out1.readObject();
			System.out.println();
			System.out.println(" "+postmap);
			out.close();
			fileOut.close();   
			out1.close();
			fileOut1.close();

		}catch(IOException i)
		{
			i.printStackTrace();              }
	}    

	public static void main(String[] args) throws ClassNotFoundException {
		GenerateMetaData obj =  new GenerateMetaData("sample.xml");
		// obj.serialize_stuff();
		obj.deserialize_stuff();
		//obj.printPostMap();
		//obj.printTagPostMap();
	}
}

class TagPost implements Serializable{

	ArrayList<Integer> postIds;
	Integer viewCount;

	public TagPost()
	{
		postIds = new ArrayList<Integer>();
		viewCount = 0;
	}

	public ArrayList<Integer> getPostIds() {
		return postIds;
	}

	public void setPostIds(ArrayList<Integer> postIds) {
		this.postIds = postIds;
	}

	public void addPostId(Integer postId)
	{
		postIds.add(postId);
	}

	public Integer getViewCount() {
		return viewCount;
	}

	public void setViewCount(Integer viewCount) {
		this.viewCount = viewCount;
	}

	public void incrementScore()
	{
		viewCount++;
	}

	public String toString()
	{
		return new String(getViewCount()+" "+getPostIds());
	}

}


class Post implements Serializable{
	String creationDate;
	ArrayList<Integer> childPosts ;
	Set<String> tagList;
	String tags;
	Integer viewCount;
	Integer postId;
	public Post()
	{
		childPosts = new ArrayList<Integer>();
		tagList = new HashSet<String>();
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public ArrayList<Integer> getChildPosts()
	{
		return childPosts;
	}

	public void addNewChild(Integer child)
	{
		childPosts.add(child);
	}
	public String getTags() {
		return tags;
	}

	public Set<String> getTagList()
	{
		return tagList;
	}

	public void setTagsList(ArrayList<String> tags) {
		for(String item : tags){
			if(!item.equalsIgnoreCase("") && !item.equalsIgnoreCase(" "))
				tagList.add(item);
		}

	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public Integer getViewCount() {
		return viewCount;
	}

	public void setViewCount(Integer viewCount) {
		this.viewCount = viewCount;
	}

	public Integer getPostId() {
		return postId;
	}

	public void setPostId(Integer postId) {
		this.postId = postId;
	}

	public String toString()
	{

		return new String(" "+getPostId()+" "+getViewCount()+" "+getCreationDate()+" "+getTagList()+" childIds "+getChildPosts());
	}


}