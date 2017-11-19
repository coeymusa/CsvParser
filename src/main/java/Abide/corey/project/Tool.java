package Abide.corey.project;

import java.io.IOException;

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
							+"'l' : total number of practices in specified location\n "
							+ "	-path to file -region\n"
							+"'a' : average cost of all peppermint oil perscriptions\n "
							+ "	-path to file -product name\n"
							+"'p' : top 5 postcodes that have highest actual spend, and how much did they vary to the national mean\n"
							+ "	-path to surgery file -path to transaction file -amount of top postcodes to be displayed\n"
							+"'r' : for each region of England, the average price per perspection of Fluxcloxacillin and how much it varied from national mean\n"
							+ "	-path to surgery file -path to transaction file -product name\n"
							+"'i' : Find the amount of perscriptions given in a surgery and compare to the average amount of perscriptions given within their PCT\n"
							+"	-path to transaction file -surgeryId\n"
							+ "NOTE: Please do not enter -, - indicates seperate paramater\n"
							+"Example arguement : r /src/main/resources/test1.csv /src/main/resouirces/test2.csv Peppermint Oil");

		} else if (args.length == 0){
			System.out.println("Please enter runtime paramaters, pass 'help' to find out possible commands");
		} else{			

			try{
				switch (args[0]){
				case "l":
					Utility utilityCount = new Utility();
					System.out.println("Total number of practices in " + args[2] + ":" + utilityCount.regionSurgeryCount(args[1],args[2]) );
					break;
				case "a":
					Utility utilityPerscriptionCost = new Utility();
					System.out.println(utilityPerscriptionCost.perscriptionAverageCost(args[1],args[2]));	
					break;
				case "p":
					Utility utilityPostcode = new Utility();
					System.out.println("Top 5 post codes with the highest actual spend: \n" + utilityPostcode.costPerPostcodeCSV(args[1],args[2],args[3]));   
					break;
				case "r":
					Utility utilityRegion = new Utility();
					System.out.println("Average price of Fluxocacillin vs national mean per region : \n" + utilityRegion.forEachRegionCSV(args[1],args[2], args[3]));   		
					break;
				case "i":
					Utility utilityPCT = new Utility();
					System.out.println(utilityPCT.perscriptionsGiven(args[1],args[2]));   		
					break;
				
				}
			} catch (IOException fnfe){
				System.out.println("Unable to find CSV file, please check it exists and the program has permission to read from it");
				System.exit(1);
			}
		}
	}
}
