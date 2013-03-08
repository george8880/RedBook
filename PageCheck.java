import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

//Checks and outputs which pages are missing/repeated

public class PageCheck {
	public static void main(String[] args) throws IOException {
		File file = new File("even.pdf");
		PDDocument doc = PDDocument.load(file);
		int totalPages = doc.getNumberOfPages();
		
		//used to extract text from pdf document
		PDFTextStripper st = new PDFTextStripper();
		
		int leeway = 0;
		
		for (int i = 1; i <= totalPages; i++) {
			st.setStartPage(i);
			st.setEndPage(i);
			String s = st.getText(doc);
			while (s.indexOf("\n") != -1) {
				int indexOfNewLine = s.indexOf("\n");
				int indexOfNextNewLine = s.indexOf("\n", indexOfNewLine + 1);
				String line;
				
				if (indexOfNextNewLine == -1)
					line = s.substring(indexOfNewLine + 1);
				else
					line = s.substring(indexOfNewLine + 1, indexOfNextNewLine);
				
				if (SpaceFixer.isPageNum(line)) {
					line = Extractor.stripSpaces(line).toUpperCase().trim();
					int left = line.indexOf('[');
					int right = line.indexOf(']');
					
					try {
						int num = Integer.parseInt(line.substring(left + 1, right));
						
						if (num < 2*i + leeway) {
							System.out.println("Repeat: " + num + " at pdf page:" + i);
							leeway -= 2;
						}
						else if (num > 2*i + leeway) {
							System.out.println("Missing: " + (2*i + leeway) + " at pdf page:" + i);
							leeway += 2;
						}
					} catch (NumberFormatException e) {
						System.out.println("WTF at pdf page: " + i);
					}
				}
				s = s.substring(indexOfNewLine + 1);
			}
		}
	}
}
