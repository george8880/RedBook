import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;


public class DeathMarker {
	public static void main(String[] args) throws IOException {
		ICsvListReader listReader = new CsvListReader(new FileReader("idMap_1942_25.csv"), //CHANGE
				CsvPreference.STANDARD_PREFERENCE);
		CsvListWriter writer = new CsvListWriter(new FileWriter("idMap_marked_1942_25.csv"), //CHANGE
				CsvPreference.STANDARD_PREFERENCE);
		
		final CellProcessor[] processors = getProcessors();
		List<Object> customerList;
	    while ((customerList = listReader.read(processors)) != null) {
	    	ArrayList<String> marked = new ArrayList<String>();
	      	String id = (String) customerList.get(0);
	      	String name = (String)customerList.get(1);
	      	
	      	//checks if first letter in name has mark
	      	char c = name.charAt(0);
	      	if (c == '/') {
	      		marked.add(id);
	      		marked.add("");
	      	}
	      	else
		      	if (c < 'A' || c > 'Z') {
		      		marked.add(id);
		      		marked.add(name.substring(1).trim());
		      		marked.add("Deceased");
		      	}
		      	else {
		      		marked.add(id);
		      		marked.add(name);
		      	}
	      	
	      	writer.write(marked);
        	marked = new ArrayList<String>();
	    }
	    listReader.close();
	    writer.close();
	}

	private static CellProcessor[] getProcessors() {
        
        final CellProcessor[] processors = new CellProcessor[] { 
                new NotNull(), // customerNo (must be unique)
                new NotNull(), // firstName
        };
        
        return processors;
	}
}
