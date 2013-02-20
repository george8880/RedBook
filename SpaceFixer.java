//Used on initial pdf. Fixes whitespaces in headers

import java.io.*;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;


public class SpaceFixer {

	static String[] fields = {"homeaddress", "officeaddress", "occupation", "married",
		"child", "grandchildren", "officesheld,honorandawards", "seasonaladdress", 
		"publicationsandfinearts", "lastknownaddress", "businessaddress", "spouse", "children",
		"publications", "homeandofficeaddress", "great-grandchild", 
		
		"preparedat", "officesheld", "memberof", "yearsincollege", "harvardsons", "harvardbrothers",
		"born", "died", "mailingaddress", "degrees"};
	static String[] fix = {"home address", "office address", "occupation", "married",
		"child", "grandchildren", "offices held, honor and awards", "seasonal address", 
		"publications and fine arts", "last known address", "business address", "spouse", "children",
		"publications", "home and office address", "great-grandchild", 
		
		"prepared at", "offices held", "member of", "years in college", "harvard sons", "harvard brothers",
		"born", "died", "mailing address", "degrees"};
	
	public static void main(String[] args) throws IOException {
		File file = new File("1941_50.pdf");
		PDDocument doc = PDDocument.load(file);
		int totalPages = doc.getNumberOfPages();
		
		//used to extract text from pdf document
		PDFTextStripper st = new PDFTextStripper();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("WSfixed_1941_50.txt"));
		
		for (int i = 1; i <= totalPages; i++) {
			//only look at one page at a time
			st.setStartPage(i);
			st.setEndPage(i);
			MyList<String> workText = preparePage(i, st, doc);
			
			while(!workText.isEmpty()) {
				String line = workText.removeHead();
				String test = "";
				int colonIndex = line.indexOf(':');
				int semiIndex = line.indexOf(';');
				int index = -1;
				//contains : or ;
				//if contains both, take closer one
				if (colonIndex != -1 && semiIndex != -1) {
					int min = Math.min(colonIndex, semiIndex);
					test = line.substring(0, min);
					index = min;
				}
				else if (colonIndex != -1) {
					test = line.substring(0, colonIndex);
					index = colonIndex;
				}
				else if (semiIndex != -1) {
					test = line.substring(0, semiIndex);
					index = semiIndex;
				}
				test = prepareTest(test);
				
				//If we found a potential field
				if (!test.equals("")) {
					String fix = closestField(test);
					if (!fix.equals(""))
						line = fix + ":" + line.substring(index + 1);
				}
				
				//gets rid of certain lines that aren't supposed to be there
				String temp = stripSpaces(line);
				if (temp.indexOf("ANNIVER") == -1 &&
						temp.indexOf("HARVARDC") == -1) {
					bw.write(line);
					bw.newLine();
				}
			}
		}
		doc.close();
	}
	
	//returns closest field it is
	//If doesn't fulfil min requirement, return ""
	public static String closestField (String s) {
		Scanner input = new Scanner(System.in);
		int min = computeDistance(s, fields[0]);
		int index = 0;
		
		for (int i = 1; i < fields.length; i++) {
			int d = computeDistance(s, fields[i]);
			if (d < min) {
				min = d;
				index = i;
			}
		}
		
		//if editing distance is less than 50% of field length
		if (min * 2 <= fields[index].length())
			return fix[index];
		else
			return "";
	}
	
	//gets rid of all spaces and turns all to lower case
	public static String prepareTest (String s) {
		String result = "";
		
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c != ' ')
				result += c;
		}
		return result.toLowerCase();	
	}
	
	//gets rid of all spaces and dots
			public static String stripSpaces (String s) {
				String result = "";
				
				for (int i = 0; i < s.length(); i++) {
					char c = s.charAt(i);
					if (c != ' ' && c != '.')
						result += c;
				}
				return result;	
			}
	
	//from rosetta stone
	public static int computeDistance(String s1, String s2) {
	    s1 = s1.toLowerCase();
	    s2 = s2.toLowerCase();
	 
	    int[] costs = new int[s2.length() + 1];
	    for (int i = 0; i <= s1.length(); i++) {
	      int lastValue = i;
	      for (int j = 0; j <= s2.length(); j++) {
	        if (i == 0)
	          costs[j] = j;
	        else {
	          if (j > 0) {
	            int newValue = costs[j - 1];
	            if (s1.charAt(i - 1) != s2.charAt(j - 1))
	              newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
	            costs[j - 1] = lastValue;
	            lastValue = newValue;
	          }
	        }
	      }
	      if (i > 0)
	        costs[s2.length()] = lastValue;
	    }
	    return costs[s2.length()];
	  }
	
	private static MyList<String> preparePage (int pageNum, PDFTextStripper st, 
			PDDocument doc) throws IOException {
		//Store all sentences in a MyList first
		MyList<String> result = new MyList<String>();
		String s = st.getText(doc);
		while (s.indexOf("\n") != -1) {
			int indexOfNewLine = s.indexOf("\n");
			result.enqueue(s.substring(0, indexOfNewLine - 1));
			s = s.substring(indexOfNewLine + 1);
		} //don't need to add last line because it is just an empty line
		//Remove extraneous lines (first page, first two lines and last line (page)
		//All other lines, remove first line and last line		
		if (pageNum == 1)
			result.removeHead();
		
		if (isTitle(result.head.v))
			result.removeHead();
		if (isPageNum(result.tail.v))
			result.removeTail();
		
		return result;
	}

	//tests if a line is a title at the top of the page
	//HARVARDCLASSOF1941 or 25THANNIVERSARYREPORT
	public static boolean isTitle(String s) {
		String test = Extractor.stripSpaces(s).toUpperCase().trim();
		int d1 = SpaceFixer.computeDistance(test, "HARVARDCLASSOF1941");
		int d2 = SpaceFixer.computeDistance(test, "25THANNIVERSARYREPORT");
		
		if (d1 <= 7 || d2 <= 7)
			return true;
		
		return false;
	}
	
	//tests if a line is the bottom page number
	//i.e. [ # ]
	public static boolean isPageNum(String s) {
		String test = Extractor.stripSpaces(s).toUpperCase().trim();
		if (test.length() <= 7 && test.indexOf('[') != -1 && test.indexOf(']') != -1) {
			System.out.println(test);
			return true;
		}
		return false;
	}
}
