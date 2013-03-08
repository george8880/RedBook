//Fixes whitespace errors in name

import java.io.*;
import java.util.*;

import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

public class NameFixer {
	
	static ArrayList<String> nameList = new ArrayList<String>();
	static List<String> fixed = new ArrayList<String>();
	//static Map<String, String> pastWork = new HashMap<String, String>();
	
	public static void main(String[] args) throws IOException {
		getNameList("NameList1942.txt");                                                   //CHANGE
		//loadPast("pastWork.txt");
		ICsvListReader listReader = new CsvListReader(new FileReader("idMap_1942_50.csv"), //CHANGE
				CsvPreference.STANDARD_PREFERENCE);
		CsvListWriter writer = new CsvListWriter(new FileWriter("idMap_fixed_1942_50.csv"), //CHANGE
				CsvPreference.STANDARD_PREFERENCE);
    	Scanner input = new Scanner(System.in);
		
		listReader.getHeader(true); // skip the header (can't be used with CsvListReader)
        final CellProcessor[] processors = getProcessors();
        
        List<Object> customerList;
        while ((customerList = listReader.read(processors)) != null) {
        	String id = (String) customerList.get(0);
        	String name = (String)customerList.get(1);
        	//String deceased = (name.indexOf('?') != -1 || name.indexOf('*') != -1)?"Yes":"No";
        	name = prepareName(name);
        	String fixedName;
        	int minD = computeDistance(name, removeSpaces(nameList.get(0)));
        	
        	//If entry also handled in some other runtime, no need to do again
        	/*if (pastWork.containsKey((String) customerList.get(1))) {
        		String fix = pastWork.get((String) customerList.get(1));
        		
        		if (fix.equals("g"))
        			fixedName = (String) customerList.get(1);
        		else        		
        			fixedName = pastWork.get((String) customerList.get(1));
        	} //JOHN HANCOCK NOTMAN*/
        	  
        	if (true) {
	        	int index = 0;
	        	for (int i = 1; i < nameList.size(); i++) {
	        		int dist = computeDistance (name, removeSpaces(nameList.get(i)));
	        		if (dist < minD) {
	        			minD = dist;
	        			index = i;
	        		}
	        	}
	        	if (minD >= 3) {
	        		System.out.println((String) customerList.get(1));
	        		String answer = input.nextLine();
	        		if (answer.equals("g"))
	        			fixedName=(String) customerList.get(1);
	        		else
	        			fixedName = answer;
	        	}
	        	else
	        		fixedName = nameList.get(index);
        	}
	        	
        	fixed.add(id);
        	
        	//checks if deceased
        	if (!fixedName.equals("") && (fixedName.charAt(0) < 'A' || fixedName.charAt(0) > 'Z')) {
        		fixed.add(fixedName.substring(1).trim());
        		fixed.add("Deceased");
        	}
        	else
        		fixed.add(fixedName);
        	
        	//fixed.add(deceased);
        	writer.write(fixed);
        	fixed = new ArrayList<String>();
        }
		listReader.close();
		writer.close();
	}
	
	//removes 
	public static String prepareName (String s) {
		String temp = "";
		
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) >= 'A' && s.charAt(i) <= 'Z')
				temp += s.charAt(i);
		}
		
		return temp;
	}
	
	public static void getNameList (String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		//BufferedWriter bw = new BufferedWriter(new FileWriter("1940NameList.txt"));
		String s = br.readLine();
		
		while (s != null) {
			if (!s.equals("1942") && !s.equals("") && (s.indexOf(')') == -1 || s.indexOf('(') != -1) &&
					s.substring(0,1).equals(s.substring(0,1).toUpperCase())) {
				if (s.indexOf('(') != -1)
					s = s.substring(0, s.indexOf('('));
				if (s.length() >= 8) {
					nameList.add(s);
					//bw.write(s);
					//bw.newLine();
				}
			}
			s = br.readLine();
		}
		
		//bw.close();
		br.close();
	}
	
	//removes spaces and punctuation, and returns to upper case
	public static String removeSpaces (String s) {
		String temp = s.toUpperCase();
		String result = "";
		
		for (int i = 0; i < temp.length(); i++) {
			if (temp.charAt(i) >= 'A' && temp.charAt(i) <= 'Z')
				result += temp.charAt(i);
		}
		
		return result.toUpperCase();
	}
	
	private static CellProcessor[] getProcessors() {
        
        final CellProcessor[] processors = new CellProcessor[] { 
                new NotNull(), // id
                new NotNull(), // name
        };
        
        return processors;
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
		
		//loads past work in case program ends in the middle
		//can resume
		/*
		private static void loadPast (String filename) throws IOException {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String s = br.readLine();
			
			while (s != null) {
				String fixed = br.readLine();
				pastWork.put(s, fixed);
				s = br.readLine();
			}
			
			br.close();
		} */
}
