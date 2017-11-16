package Abide.corey.project;

import java.io.IOException;
import java.util.List;

import Abide.corey.project.model.Surgery;
import Abide.corey.project.model.Transaction;

/**
 * Hello world!
 *
 */
public class Tool 
{
	public static void main( String[] args ) {
		if (args.length ==  1 && args[0].toLowerCase().equals("help")){
			System.out.println(
					"Commands: \n"
							+"'l' : total number of practices in specified location \n"
							+"'a' : average cost of all peppermint oil perscriptions \n"
							+"'p' : top 5 postcodes that have highest actual spend, and how much did they vary to the national mean \n"
							+"'r' : for each region of England, the average price per perspection of Fluxcloxacillin and how much it varied from national mean \n"
							+"'?' : mystery question");
			
		} else if (args.length <= 2){
			System.out.println("Please pass in the location of the file as a string, extra param and the operation, pass 'help' to find out possible commands");
		} else{			
			
			try{
			Reader reader = new Reader();
			
			long startTime = System.nanoTime();
			List<Surgery> surgeries = reader.populateSurgery(reader.readCSV("src/main/resources/testCSV.csv"));
			List<Transaction> transcations = reader.populateTransaction(reader.readCSV("src/main/resources/testCSV3.csv"));

				switch (args[2]){
				case "l":
					System.out.println("Total number of practices in " + args[1] + ":" + Utility.practicesCountCSV("src/main/resources/testCSV.csv",args[1]) );
					//System.out.println("Total number of practices in " + args[1] + ":" + Utility.countPractices(surgeries,args[1]));
					break;
				case "a":
					//System.out.println(Utility.averagePerscriptionCost(transcations,args[1], Utility.singleWordCheck(args[1])));
					System.out.println(Utility.perscriptionCostreadCSV("src/main/resources/testCSV3.csv",args[1]));	
					break;
				case "p":
					//System.out.println("Top 5 post codes with the highest actual spend: \n" + Utility.costPerPostcode(transcations,surgeries));   
					System.out.println("Top 5 post codes with the highest actual spend: \n" + Utility.costPerPostcodeCSV("src/main/resources/testCSV.csv","src/main/resources/testCSV3.csv",5));   
					break;
				case "r":
					System.out.println("Average price of Fluxocacillin vs national mean per region : \n" + Utility.forEachRegionCSV("src/main/resources/testCSV.csv","src/main/resources/testCSV3.csv", "0501012G0"));   		
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);
					System.out.println(duration);	
					break;
//				case "?":
					//break;
		
				}
			} catch (IOException fnfe){
				System.out.println("Unable to find CSV file, please check it exists and the program has permission to read from it");
				System.exit(1);
			}
		}
	}
}
