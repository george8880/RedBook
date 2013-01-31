//Uses PDFBox and SuperCVS libraries

import java.io.*;
import java.util.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class Extractor {
	
	static int totalPages;
	static Group g = new Group();
	static Scanner input = new Scanner(System.in);
	
	//Starting ID number for this session
	static final int ID_START = 2000;
	
	public static void main(String[] args) throws IOException {
		//loads the pdf document and records total number of pages 
		File file = new File("1940_25_polished.pdf");
		PDDocument doc = new PDDocument();
		doc = PDDocument.load(file);
		totalPages = doc.getNumberOfPages();
		
		//used to extract text from pdf document
		PDFTextStripper st = new PDFTextStripper();
		
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
		
		for (int i = 1; i <= totalPages; i++) {
			//only look at one page at a time
			st.setStartPage(i);
			st.setEndPage(i);
			
			//List of workable sentences in the page
			MyList<String> page = preparePage(i, st, doc);
			
			while (!page.isEmpty()) {
				String sentence = page.removeHead();
				
				String temp = stripSpaces(sentence);
				if (temp.indexOf("ANNIVER") == -1 && temp.indexOf("HARVARDC") == -1 &&
						!sentence.trim().equals("YEARS IN COMRGE;")) {
					
				isField = isField(sentence);
				
				//Name could pop up anywhere, after info field or essay, so always check
				if (isName(sentence)) {
					//store essay in last person's essay field and clear store
					if (currentPerson != null && !store.equals("")) {
						currentPerson.essay = store;
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
				
				//Page offsets for 1940_50
				/*
				if (i >= 341)
					p += 18;
				if (i >= 537)
					p += 5;
				currentPerson.addPage(p);*/
				int p = i;
				if (i >= 102)
					p += 459;
				if (i >= 542)
					p += 4;
				if (i >= 736)
					p += 4;
				currentPerson.addPage(p);
				
				
			}	
			}
		}
		
		doc.close();
		
		System.out.println("DONE");
		
		g.outputInfo("infoFieldsPages_1940_25.csv");
		g.outputMap("idMapPages_1940_25.csv");
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
		result.removeHead();
		result.removeTail();
		if (pageNum == 1)
			result.removeHead();
		
		return result;
	}
	
	//Checks if sentence is the name of a person
		//If 90% of the characters are uppercase
		private static boolean isName (String line) {
			String checker = line.toUpperCase();
			int sameChars = 0;
			//to make sure line isn't just pure numbers
			int numNumbers = 0;
			
			if (checker.trim().indexOf("ANNIVERSARY REPORT") != -1 || checker.trim().indexOf("ADDRESS UNKNOWN.") != -1)
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
	
	//A sentence is the start of a new info field if it contains
	//one colon
	private static boolean isField (String line) {
		return (line.indexOf(':') != -1);
	}
	
	//Checks if a sentence ends in a period
	private static boolean endsInPeriod (String line) {
		String sentence = line.trim();
		int l = sentence.length();
		return (sentence.charAt(l - 1) == '.');
	}
}
