package queryassets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Scanner;

public class JsonReader {

private static String readAll(Reader rd) throws IOException {
    StringBuilder sb = new StringBuilder();
    int cp;
    while ((cp = rd.read()) != -1) {
      sb.append((char) cp);
    }
    return sb.toString();
  }

public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
    InputStream is = new URL(url).openStream();
    try {
      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
      String jsonText = readAll(rd);
      JSONObject json = new JSONObject(jsonText);
      return json;
    } finally {
      is.close();
    }
  }
  
public static void findCriticalAssets(JSONObject json) throws JSONException {
	  org.json.JSONArray jsonArray = json.getJSONArray("assets");
	  int criticalAssetCount = 0;
	    System.out.println("Asset IDs for Critical Assets: ");
	    for(int i = 0; i < jsonArray.length(); i++) {
	    		JSONObject currentAsset = jsonArray.getJSONObject(i);
	    		if(currentAsset.getInt("status") == 3) {
	    			criticalAssetCount++;
	    			System.out.println(currentAsset.get("assetId"));	
	    		}
	    }
	    System.out.println("Total Critical Asset Count: " + criticalAssetCount);
  }
  
public static void listOptions() {
	  System.out.println("search");
	  System.out.println("critical");
	  System.out.println("tree");
	  System.out.println("quit");
  }

public static void searchPrintOptions(JSONObject json) throws JSONException {
	System.out.println("What would you like to search by?");
	org.json.JSONArray jsonArray = json.getJSONArray("assets");
	JSONObject asset = jsonArray.getJSONObject(0);
	Iterator<String> keys = asset.keys();

	while(keys.hasNext()) {
	    System.out.println(keys.next());
	    
	}
  }
  
public static void search(String searchType, String searchTerm, JSONObject json) throws JSONException {
	  org.json.JSONArray jsonArray = json.getJSONArray("assets");
	  boolean foundFlag = false;
	  int assetCount = 0;
	    for(int i = 0; i < jsonArray.length(); i++) {
	    	JSONObject currentAsset = jsonArray.getJSONObject(i);
	    		if(currentAsset.get(searchType).toString().equals(searchTerm)) {
	    			Gson gson = new GsonBuilder().setPrettyPrinting().create();
	    			String toPrint = gson.toJson(currentAsset);
	    			System.out.println(toPrint);
	    			System.out.println("");
	    			foundFlag = true;
	    			assetCount++;
	    		}
	    }
	    System.out.println(assetCount + " Results returned");
	  if(foundFlag == false) {
		  System.out.println("Asset not found");
	  }
  }

public static boolean exists(String searchType, String searchTerm, JSONObject json) throws JSONException {
	//similar to search function but returns whether or not an asset exists for a given value
	  org.json.JSONArray jsonArray = json.getJSONArray("assets");
	    for(int i = 0; i < jsonArray.length(); i++) {
	    	JSONObject currentAsset = jsonArray.getJSONObject(i);
	    		if(currentAsset.get(searchType).toString().equals(searchTerm)) {	
	    			return true;
	    		}
	    }
	return false;	
}

public static void printTree(HashMap<Integer, ArrayList<Integer>> h, int assetId, int level) {
	for(int i = 0; i < level; i++) {
		System.out.print("-");
	}
	System.out.println(assetId);
	if(h.get(assetId) == null) {
		return;
	}
	else {
		for(int i = 0; i < h.get(assetId).size(); i++) {
			printTree(h, h.get(assetId).get(i), level+1);
		}

	}
}		

public static HashMap<Integer, ArrayList<Integer>> createTree(JSONObject json) throws JSONException {
	org.json.JSONArray jsonArray = json.getJSONArray("assets");	
	HashMap<Integer, ArrayList<Integer>> hmap = new HashMap<Integer, ArrayList<Integer>>();
	ArrayList<Integer> tempList;
	int tempVar = 0;
	for(int i = 0; i < jsonArray.length(); i++) {
		JSONObject currentAsset = jsonArray.getJSONObject(i);
		//If asset has no parent and is not already in the hashmap, put it in
		if(currentAsset.get("parentId").toString().equals("null")) {
			if(!hmap.containsKey(currentAsset.getInt("assetId"))) {
				hmap.put(currentAsset.getInt("assetId"), new ArrayList<Integer>());
			}
		} 
		else {
			//if asset's parent has an entry but nothing in the list yet
			if(hmap.get(currentAsset.getInt("parentId")) == null) {
				tempList = new ArrayList<Integer>();
				tempVar = currentAsset.getInt("assetId");
				tempList.add(tempVar);
				hmap.put((Integer)currentAsset.getInt("parentId"), tempList);
			}
			//if asset's parent exists in the list and the child list is already populated
			else {
				tempList = hmap.get(currentAsset.getInt("parentId"));
				tempVar = currentAsset.getInt("assetId");
				tempList.add(tempVar);
				hmap.put((Integer)currentAsset.getInt("parentId"), tempList);
			}
		}
	}
	return hmap;
}
  
public static void main(String[] args) throws IOException, JSONException {
	  	//Download the file
	    JSONObject json = readJsonFromUrl("https://www.twinthread.com/code-challenge/assets.txt");
	    boolean treeCreated = false;
	    HashMap<Integer, ArrayList<Integer>> hmap = new HashMap<Integer, ArrayList<Integer>>();
	    System.out.println("Type \"options\" for a list of command options");
	    System.out.println("Enter a command: ");
	    Scanner scanner = new Scanner(System.in);
	    while (scanner.hasNextLine()) {
	        String command = scanner.nextLine();
	        command = command.toLowerCase();
	        if(command.equals("options")){
	        	listOptions();
	        }
	        if(command.equals("critical")) {
	        	findCriticalAssets(json);
	        }
	        if(command.equals("quit")) break;
	        if(command.equals("search")) {
	        	//get command, search type, and search term
	        	searchPrintOptions(json);
	        	String searchParam = scanner.nextLine();
	        	org.json.JSONArray jsonArray = json.getJSONArray("assets");
	        	JSONObject temp = jsonArray.getJSONObject(0);
	      	  	if(temp.has(searchParam)!= true) {
	      		  System.out.println("Invalid search parameter");
	      	  	}
	        	System.out.println("Enter a search term: ");
	        	String searchTerm = scanner.nextLine();
	        	search(searchParam, searchTerm, json);
	        	
	        }
	        if(command.equals("tree")) {
	        	//make tree on first tree command then get assetId
	        	if(treeCreated == false) {
	        		hmap = createTree(json);
	        		treeCreated = true;
	        	}
	        	System.out.println("Enter an assetId: ");
	        	String assetIdString = scanner.nextLine();
	        	int assetId = Integer.parseInt(assetIdString.trim());
	        	if(exists("assetId", assetIdString, json)) {
	        		printTree(hmap, assetId, 0);
	        	} else {
	        		System.out.println("Invalid asset id");
	        	}
	        }
	        System.out.println("Enter a command: ");
	    }
	    scanner.close();
  }
}