//Uses PDFBox and SuperCVS libraries

import java.io.*;
import java.util.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class TextExtractor {
	
	static String[] fix = {"home address", "office address", "home and office address", 
		"business address", "seasonal address", "mailing address", "last known address", 
		"born", "died", "prepared at", "occupation", 
		"offices held", "member of", "years in college", "harvard sons", "harvard brothers",
		"married", "spouse", "child", "children", "grandchildren", "great-grandchild", 
		"offices held, honor and awards", "publications", "publications and fine arts"
		 };
	
	static int totalPages;
	static Group g = new Group();
	static Scanner input = new Scanner(System.in);
	
	//Starting ID number for this session
	static final int ID_START = 2000;
	
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("WSfixed_1940_25.txt"));
		
		//list of boolean values to keep track of where the text reader is
		boolean isEssay = false;
		boolean isField = false;
		boolean lastSentenceIsName = false;
		boolean lastSentenceEndsInPeriod = false;
		/*Notes: Can only transition from fields to essay if isEssay = false
		 *       and lastSentenceEndsInPeriod = true or lastSentenceIsName = true
		 *       and isNewField = false;
		 *       
		 *       Can only get out of isEssay = true if line is a name
		 *       
		 *       If isEssay = true, lastSentenceEndsInPeriod does not affect anything
		 */
		Person currentPerson = null;
		int id = ID_START; //id increments by 1 every new person
		//stores sentences that are together, i.e. one info field, one essay etc
		String store = "";
		
		for (int i = 1; i <= 1; i++) {
			String sentence = br.readLine();
			while (sentence != null) {
				isField = isField(sentence);
				
				//Name could pop up anywhere, after info field or essay, so always check
				if (isName(sentence)) {
					//store essay/info in last person's field and clear store
					if (currentPerson != null && !store.equals("")) {
						//handles one line info fields
						if (isEssay)
							currentPerson.essay = store;
						//If last person didn't get to isEssay mode,
						//that means s/he did not have an essay, so store store
						//in info field
						else
							currentPerson.addField(store);
						store = "";
					}		
					
					currentPerson = new Person(sentence.trim());
					g.addPerson(currentPerson, id);
					id++; //increment id for next new person
					
					isEssay = false;
					isField = false;
					lastSentenceEndsInPeriod = false;
					lastSentenceIsName = true;
				}
				//info field can only come when it is not in the middle of an essay
				else if (!isEssay && isField) {
					//store previous, completed info field and clear store for new info field
					if (currentPerson != null && !store.equals("")) {
						currentPerson.addField(store);
						store = "";
					}
					
					store += sentence;
					
					if (endsInPeriod(sentence))
						lastSentenceEndsInPeriod = true;
					else
						lastSentenceEndsInPeriod = false;
					lastSentenceIsName = false;
				}
				//If not in essay field, and current line is not the start of an info field
				//and the previous sentence ends with a period, this is the start of the essay
				else if (!isEssay && !isField && 
						(lastSentenceEndsInPeriod || lastSentenceIsName)) {
					//store previous, completed info field and clear store for essay
					if (currentPerson != null && !store.equals("")) {
						currentPerson.addField(store);
						store = "";
					}
					
					store += sentence;
					
					isEssay = true;
					lastSentenceIsName = false;
				}
				//otherwise, in the middle of some info field or essay
				else {
					store += sentence;
					
					//Last sentence ending in period only relevant in info fields
					if (!isEssay) {
						if (endsInPeriod(sentence))
							lastSentenceEndsInPeriod = true;
						else
							lastSentenceEndsInPeriod = false;
						lastSentenceIsName = false;
					}
				}
				System.out.println(currentPerson.name);
				//currentPerson.addPage(i);
				sentence = br.readLine();
			}	
		}
		
		System.out.println("DONE");
		br.close();
		g.outputInfo("infoFields_1940_25.csv");
		g.outputMap("idMap_1940_25.csv");
	}
	
	/*
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
		result.removeHead();
		result.removeTail();
		if (pageNum == 1)
			result.removeHead();
		
		return result;
	} */
	
	//Checks if sentence is the name of a person
	//If 90% of the characters are uppercase
	private static boolean isName (String line) {
		String checker = line.toUpperCase();
		int sameChars = 0;
		//to make sure line isn't just pure numbers
		int numNumbers = 0;
		
		if (stripSpaces(checker).indexOf("ANNIVER") != -1 || checker.trim().indexOf("ADDRESS UNKNOWN.") != -1)
			return false;
		
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == checker.charAt(i))
				sameChars++;
			if (checker.charAt(i) >= '0' && checker.charAt(i) <= '9')
				numNumbers++;
		}
		
		//secondary check to make sure name is more than a certain
		//number of characters without spaces
		if ((double) sameChars/line.length() > 0.90 && numNumbers <= 1)
			if (stripSpaces(line).length() >= 8)
				return true;
			else
				return false;
		else
			return false;
	}
	
	//A sentence is the start of a new info field if it contains
	//one colon
	private static boolean isField (String line) {
		if (line.indexOf(':') != -1) {
			for (int i = 0; i < fix.length; i++)
				if (line.indexOf(fix[i]) != -1)
					return true;
			return false;
		}
		return false;
	}
	
	//Checks if a sentence ends in a period
	private static boolean endsInPeriod (String line) {
		String sentence = line.trim();
		int l = sentence.length();
		return (sentence.charAt(l - 1) == '.');
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
}
