import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;

class YoutubeItem {
	// video ID uploader age category length views rate ratings comments related
	// IDs
	String id;
	String uploader;
	String age;
	String category;
	String length;
	String views;
	String rate;
	String ratings;
	String comments;
	ArrayList<String> relatedIDs;

	public YoutubeItem(String id, String uploader, String age, String category, String length, String views,
			String rate, String ratings, String comments, ArrayList<String> relatedIDs) {
		this.id = id;
		this.uploader = uploader;
		this.age = age;
		this.category = category;
		this.length = length;
		this.views = views;
		this.rate = rate;
		this.ratings = ratings;
		this.comments = comments;
		this.relatedIDs = relatedIDs;
	}

}


public class TransformToCSV {

	public static void main(String[] args) throws Exception {

		//makeImportFiles();
		//importToNeo4j();

	}

	private static void importToNeo4j() throws Exception {
		System.out.println("CSV files read; Building the Neo4j database");
		String command = "C:/Program Files/neo4j-community-3.0.7"
				+ "/bin/neo4j-import --into C:/Users/anandrcsc/Documents/Neo4j/Youtube.db"
				+ " --nodes:video " + "videos.txt" + " --nodes:category " + "categories.txt" + " --nodes:uploader "
				+ "publishers.txt" + " --relationships " + "relationships.txt";

		System.out.println(command);

		Process p = Runtime.getRuntime().exec(command);
		p.waitFor();
		int exitVal = p.exitValue();

		System.out.println("proc.exitValue(): " + exitVal);

		if (exitVal == 0)
			System.out.println("program is finished properly!");
		else {
			String line;
			System.out.println("ERROR: Neo4j messed up");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
			in.close();
		}

	}

	private static void makeImportFiles() throws Exception {
		ArrayList<YoutubeItem> items = new ArrayList<YoutubeItem>();

		FileInputStream fis = new FileInputStream("0.txt");

		// Construct BufferedReader from InputStreamReader
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));

		String line = null;
		while ((line = br.readLine()) != null) {
			String[] splittedData = line.split("\t");
			if (splittedData.length >= 9) {

				ArrayList<String> relatedIds = new ArrayList<String>();

				for (int i = 9; i < splittedData.length; i++) {
					relatedIds.add(splittedData[i].trim());
				}

				YoutubeItem item = new YoutubeItem(splittedData[0].trim(), splittedData[1].trim(),
						splittedData[2].trim(), splittedData[3].trim(), splittedData[4].trim(), splittedData[5].trim(),
						splittedData[6].trim(), splittedData[7].trim(), splittedData[8].trim(), relatedIds);

				items.add(item);

			}

		}

		br.close();

		// video ID uploader age category length views rate ratings comments
		// related IDs

		HashSet<String> categoriesSet = new HashSet<String>();
		HashSet<String> uploadersSet = new HashSet<String>();
		HashSet<String> seenIDs = new HashSet<String>();

		// write video nodes
		File foutvideo = new File("videos.txt");
		FileOutputStream fosvideo = new FileOutputStream(foutvideo);

		BufferedWriter bwvideo = new BufferedWriter(new OutputStreamWriter(fosvideo));
		bwvideo.write("videoId:ID,uploader,age,category,length,views,rate,ratings,comments\n");
		for (YoutubeItem item : items) {
			bwvideo.write(item.id + "," + item.uploader + "," + item.age + "," + item.category + "," + item.length + ","
					+ item.views + "," + item.rate + "," + item.ratings);
			categoriesSet.add(item.category);
			uploadersSet.add(item.uploader);
			seenIDs.add(item.id);
			seenIDs.add(item.category);
			seenIDs.add(item.uploader);
			bwvideo.newLine();
		}

		bwvideo.close();

		// write category nodes
		File foutcategory = new File("categories.txt");
		FileOutputStream foscategory = new FileOutputStream(foutcategory);

		BufferedWriter bwcategory = new BufferedWriter(new OutputStreamWriter(foscategory));
		bwcategory.write("category:ID\n");
		for (String category : categoriesSet) {
			bwcategory.write(category);
			bwcategory.newLine();
		}

		bwcategory.close();

		// write publisher nodes
		File foutpublisher = new File("publishers.txt");
		FileOutputStream fospublisher = new FileOutputStream(foutpublisher);

		BufferedWriter bwpublisher = new BufferedWriter(new OutputStreamWriter(fospublisher));
		bwpublisher.write("uploader:ID\n");

		for (String uploader : uploadersSet) {
			bwpublisher.write(uploader);
			bwpublisher.newLine();
		}

		bwpublisher.close();

		// write relationships
		File foutrelationships = new File("relationships.txt");
		FileOutputStream fosrelationships = new FileOutputStream(foutrelationships);

		BufferedWriter bwrelationships = new BufferedWriter(new OutputStreamWriter(fosrelationships));
		bwrelationships.write(":START_ID,:END_ID,:TYPE\n");

		for (YoutubeItem item : items) {
			for (String relatedVideo : item.relatedIDs) {
				if (seenIDs.contains(relatedVideo) && !item.id.equals(relatedVideo)) {
					bwrelationships.write(item.id + "," + relatedVideo + ",RELATED_VIDEO");
					bwrelationships.newLine();
				}
			}
		}

		for (YoutubeItem item : items) {
			if (seenIDs.contains(item.category)) {
				bwrelationships.write(item.id + "," + item.category + ",CATEGORY");
				bwrelationships.newLine();
			}

		}
		for (YoutubeItem item : items) {
			if (seenIDs.contains(item.uploader)) {
				bwrelationships.write(item.id + "," + item.uploader + ",UPLOADER");
				bwrelationships.newLine();
			}

		}

		bwrelationships.close();

	}

}

