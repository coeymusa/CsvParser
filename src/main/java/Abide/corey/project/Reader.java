package Abide.corey.project;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import Abide.corey.project.model.Surgery;
import Abide.corey.project.model.Transaction;

public class Reader {

	public List<String[]> readCSV(String filePath) throws IOException{
		List<String[]> csvLine= new ArrayList<String[]>();
		
		String[] row;
		
		CsvParserSettings settings = new CsvParserSettings();
		settings.getFormat().setLineSeparator("\n");
		FileReader reader = new FileReader(filePath);
		CsvParser parser = new CsvParser(settings);
		
		parser.beginParsing(reader);
		
		while ((row = parser.parseNext()) != null) {
			csvLine.add(row);
		}

		parser.stopParsing();
		return csvLine;
	}

	public static List<Transaction> populateTransaction(List<String[]> list){

		List<Transaction> transactionList = new ArrayList<Transaction>();
		Pattern pattern = Pattern.compile(".*\\d+.*");
		Matcher matcher = pattern.matcher("");

		if (list.get(0)[5].matches(".*\\d+.*") == false){
			list.remove(0);
		}

		for(int rowIndex = 1; rowIndex < list.size(); rowIndex++){
			Transaction transaction = new Transaction();

			transaction.setSurgeryId(list.get(rowIndex)[2]);
			transaction.setBnfCode(list.get(rowIndex)[3]);
			transaction.setBnfName(list.get(rowIndex)[4]);
			transaction.setQuantity(Integer.valueOf(list.get(rowIndex)[5]));
			transaction.setPrice(Double.valueOf(list.get(rowIndex)[6]));

			transactionList.add(transaction);

		}

		return transactionList;

	}

	public static List<Surgery> populateSurgery(List<String[]> list){
		List<Surgery> surgeryList = new ArrayList<Surgery>();

		for(int rowIndex = 0; rowIndex < list.size(); rowIndex++){
			Surgery surgery = new Surgery();

			surgery.setId(list.get(rowIndex)[1]);
			surgery.setName(list.get(rowIndex)[2]);
			surgery.setStreet(list.get(rowIndex)[4]);
			surgery.setCounty(list.get(rowIndex)[6]);
			surgery.setPostcode(list.get(rowIndex)[7]);

			surgeryList.add(surgery);
		}
		return surgeryList;
	}
}
