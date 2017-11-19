 tool to compute some statistics from large datasets - focusing on performance. Exploring ideas of generating useful, readable data from multiple sources. That enable easy expansion, and good readability.

Able to:

How many practices are in given location ? 
l "D:\T201202ADD REXT.csv" "London"

What was the average actual cost of all given prescription name? 
a "D:\T201109PDP IEXT.csv" "Peppermint Oil"

Which 5 post codes have the highest actual spend, and how much did each spend in total?
p "D:\T201202ADD REXT.csv" "D:\T201109PDP IEXT.csv" 5

For each region of England: 
What was the average price per prescription of Flucloxacillin (excluding CoFluampicil)? 
r "D:\T201202ADD REXT.csv" "D:\T201109PDP IEXT.csv" Flucloxacillin

For a given surgery Id how many perscriptions did they give, and how did this compare to the PCT average?
i "D:\corey.project\corey.project\src\main\resources\T201109PDP IEXT.csv" A86003


To compile run:
1.mvn clean install
2.cd target
3.run one of the above commands

Enter "help" for more information 