import csv


def main():
	with open("E:/Big Data project/Part 2 and 3/videos.txt","r") as infile, open("E:/Big Data project/Part 2 and 3/mcsv.csv","w") as outfile:
		 in_txt = csv.reader(infile, delimiter = '\t')
		 out_csv = csv.writer(outfile)
		 out_csv.writerows(in_txt)

	   

main()


