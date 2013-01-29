//Stores the information of every writer
//Maps each specific writer to a unique ID

import java.io.*;
import java.util.*;

import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

public class Group {
	static String[] header = {"ID", "Name", "Essay", "Pages", "home_address", "office_address", 
		"home_and_office_address", "business_address", "seasonal_address", "last_known_address", "occupation", 
		"married", "spouse", "child", "children", "grandchildren", "great-grandchild", 
		"offices_held,_honor_and_awards", "publications", "publications_and_fine_arts"
		 };
	
	private Map<Integer, Person> idMap; //maps id to name
	
	//Used as default, when there is no other map to build on
	//i.e. building on another book from another year
	public Group() {
		idMap = new HashMap<Integer, Person>();
	}
	
	//Used when there is an existing map to build upon
	public Group(File file) throws IOException{
		CsvListReader reader = new CsvListReader(new FileReader(file),  
				CsvPreference.STANDARD_PREFERENCE);
		List<String> values = reader.read();
		
		while(values != null) {
			int id = Integer.parseInt(values.get(0));
			String name = values.get(1);
			
			idMap.put(id, new Person(name));
			values = reader.read();
		}
		
		reader.close();
	}
	
	public void addPerson (Person p, Integer i) {
		idMap.put(i, p);
	}
	
	//checks if name is already in map from before
	//i.e. person wrote in two books, so id exists from before
	public boolean isAlreadyListed (String s) {
		Collection<Person> people = idMap.values();
		
		for (Person p : people)
			if (p.name.equals(s))
				return true;
		return false;
	}
	
	//writes current map to file so it can be built upon in 
	//a future run of another book
	//FORMAT: ID, NAME
	public void outputMap (String file) throws IOException {
		CsvListWriter writer = new CsvListWriter(new FileWriter(file), 
				CsvPreference.STANDARD_PREFERENCE);
		Set<Integer> keys = idMap.keySet();
		
		for (Integer i : keys) {
			List<String> v = new ArrayList<String>();
			v.add(i + "");
			v.add(idMap.get(i).name);
			writer.write(v);
		}
		
		writer.close();
	}
	
	//writes information fields of every Person in the Group to file
	//FORMAT: ID, NAME, ESSAY, Page#s, INFO FIELD, INFO FIELD... however many there are
	public void outputInfo (String file) throws IOException {
		CsvListWriter writer = new CsvListWriter(new FileWriter(file), 
				CsvPreference.STANDARD_PREFERENCE);
		Set<Integer> keys = idMap.keySet();
		
		writer.writeHeader(header);
		
		for (Integer i : keys) {
			List<String> v = new ArrayList<String>();
			v.add(i + "");
			v.addAll(idMap.get(i).toList());
			writer.write(v);			
		}
		
		writer.close();
	}
}
