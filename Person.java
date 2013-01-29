//Person object to store information about each writer
import java.util.ArrayList;
import java.util.List;

public class Person {
	
	 static String[] fields = {"ID", "Name", "Essay", "Pages", "home address", "office address", "home and office address", 
		"business address", "seasonal address", "last known address", "occupation", 
		"married", "spouse", "child", "children", "grandchildren", "great-grandchild", 
		"offices held, honor and awards", "publications", "publications and fine arts"
		 };
	 
	 final static String NO_FIELD_INFO_MSG = "N/A";
	
	String name;
	String essay;
	String[] infoFields;
	ArrayList<Integer> pagesSpanned;
	
	public Person(String s) {
		name = s;
		infoFields = new String[fields.length];
		pagesSpanned = new ArrayList<Integer>();
	}
	
	public void addField (String s) {
		String field = s.substring(0, s.indexOf(':'));
		for (int i = 4; i < fields.length; i++) {
			if (field.indexOf(fields[i]) != -1) {
				infoFields[i] = s;
				return;
			}
		}
		//throw new RuntimeException("Field NOT ADDED WTFFFF");
	}
	
	
	public void addPage (int i) {
		if (!pagesSpanned.contains(i))
			pagesSpanned.add(new Integer(i));
	} 
	
	//FORMAT: NAME, ESSAY, PAGES (X or X-Y), INFO FIELD, INFO FIELD...
	//however many info field the Person has
	public List<String> toList() {
		List<String> result = new ArrayList<String>();
		
		result.add(name);
		
		//If an essay exists, add essay; else, store no essay
		if (essay == null)
			result.add("No Essay");
		else	
			result.add(essay);
		
		//not counting pages
		if (pagesSpanned.size() == 0)
			result.add("gg");
		else if (pagesSpanned.size() == 1)
			result.add("Pg. " + pagesSpanned.get(0) + "");
		else {
			result.add("Pg. " + pagesSpanned.get(0) + "-" + 
		        pagesSpanned.get(pagesSpanned.size()-1));
		}
		
		for (int i = 4; i < infoFields.length; i++)
			if (infoFields[i] == null || infoFields[i].equals(""))
				result.add(NO_FIELD_INFO_MSG);
			else
				result.add(infoFields[i]);
		
		return result;
	}
}
