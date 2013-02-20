//Matches name with id in existing database

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;


public class NameMatcher {
	
	static final String currMapFile = "idMap_fixed_1941_50.csv";
	static Map<String, Integer> mainMap = new HashMap<String, Integer>();
	
	public static void main(String[] args) throws IOException {
		loadOrig("idMap_1941_25_1.csv");
		
		CsvListReader reader = new CsvListReader(new FileReader(currMapFile),  
				CsvPreference.STANDARD_PREFERENCE);
		CsvListWriter writer = new CsvListWriter(new FileWriter("idMap_1941_50_matched.csv"), 
				CsvPreference.STANDARD_PREFERENCE);
		List<String> values = reader.read();
		
		while(values != null) {
			int id = Integer.parseInt(values.get(0));
			String name;
			
			if (values.get(1) != null)
				name = values.get(1).toUpperCase();
			else
				name = "";
			
			Set<String> listOfNames = mainMap.keySet();
			//fewer than 3 corrections
			int foundMatchID = -1;
			for (String s : listOfNames)
				if (SpaceFixer.computeDistance(name, s) <= 3) {
					foundMatchID = mainMap.get(s);
					break;
				}
			
			if (foundMatchID != -1)
				id = foundMatchID;
			
			List<String> temp = new ArrayList<String>();
			temp.add(id + "");
			temp.add(name);
			writer.write(temp);
			
			values = reader.read();
		}
		writer.close();
		reader.close();
	}
	
	//load all id-named accumulated thus far
	public static void loadOrig (String filename) throws IOException {
		CsvListReader reader = new CsvListReader(new FileReader(filename),  
				CsvPreference.STANDARD_PREFERENCE);
		List<String> values = reader.read();
		
		while(values != null) {
			int id = Integer.parseInt(values.get(0));
			String name;
			if (values.get(1) != null)
				name = values.get(1).toUpperCase();
			else
				name = "FAIL";
			
			mainMap.put(name, id);
			values = reader.read();
		}
		
		reader.close();
	}
}
