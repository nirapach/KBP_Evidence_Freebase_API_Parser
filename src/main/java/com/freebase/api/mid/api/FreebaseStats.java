package com.freebase.api.mid.api;

/**
 * Created by niranjan on 6/18/15.
 */

import com.google.common.util.concurrent.RateLimiter;
import org.apache.http.client.config.RequestConfig;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.jayway.jsonpath.JsonPath;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.*;


import javax.xml.ws.http.HTTPException;
import java.beans.PropertyVetoException;
import java.io.*;
import java.sql.SQLException;
import java.util.*;

@Service
@SuppressWarnings("unchecked")
public class FreebaseStats {

    Logger logger = LoggerFactory.getLogger(FreebaseStats.class);

    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";
    public static Properties properties = new Properties();
    // Allow one request per second
    private RateLimiter throttle = RateLimiter.create(500.0);
    /*
      parsing all the json files
        */
    public boolean getOverAllFBstats(String file_address,String result_file_address) throws URISyntaxException, IOException, PropertyVetoException, SQLException, HTTPException {



        BufferedReader reader = null;
        FileWriter fileWriter = null;
        try {
            //properties.load(new FileInputStream("${API_KEY}"));


            HashMap<String,String> types_map=new HashMap<String,String>();
            types_map.put("spouse","/people/marriage/spouse");
            types_map.put("siblings","/people/sibling_relationship/sibling");
            types_map.put("foundedBy","/organization/organization_founder/organizations_founded");

            File indexdirectory = new File(file_address);

            File[] files=indexdirectory.listFiles();

            HttpTransport httpTransport = new NetHttpTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
            JSONParser parser = new JSONParser();
            HttpRequest request;
            HttpResponse httpResponse;

            //Hashmap for storing the event pairs

            HashMap<HashMap<String,String>,Integer> Map_One= new HashMap<HashMap<String, String>, Integer>();

            ArrayList<HashMap<String,String>> Map_List=new ArrayList<HashMap<String, String>>();

            int number_of_api_calls=0;
            for(int i=0;i<files.length;i++){

                if (!files[i].isDirectory() && !files[i].isHidden()
                        && files[i].canRead() && files[i].exists()) {
                    String input_file_canonical_path=files[i].getCanonicalPath();
                    System.out.println("\n Extraction is going on with file"
                            + input_file_canonical_path);
                    String extraction_clause=input_file_canonical_path.substring(input_file_canonical_path.lastIndexOf("-")+1,input_file_canonical_path.length());

                    //create File object
                    File csv_file = new File(result_file_address+"Freebase_Response"+extraction_clause+"."+"txt");
                    String filename=csv_file.getAbsolutePath();
                    //File writer
                    fileWriter = new FileWriter(filename);

                    FileReader fileReader = new FileReader(files[i].getCanonicalPath());
                    // Always wrap FileReader in BufferedReader.
                    reader = new BufferedReader(fileReader);

                    StringBuffer fbresponse = new StringBuffer();

                    String line;
                    GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/mqlread");
                    while ((line = reader.readLine()) != null) {

                        int count = 0;
                        String[] triples=line.split("\t");

                        String machine_id=triples[0].substring(triples[0].lastIndexOf("/"),triples[0].length()-1);
                        long startTime = System.currentTimeMillis();
                        machine_id=machine_id.replace(".","/");
                        String category=types_map.get(extraction_clause);

                        String query = "[{\"name\": null, \"id\": \""+machine_id+"\",\""+category+"\":[]}]";
                        url.put("query", query);
                        //url.put("key", properties.get("API_KEY"));
                        url.put("key", "AIzaSyA48k9yTqngQVUZDjwdnUDRKrcC8DOwiSY");

                        request = requestFactory.buildGetRequest(url);
                        request.setConnectTimeout(10000);
                        request.setReadTimeout(10000);

                        throttle.acquire();

                        httpResponse = request.execute();
                        number_of_api_calls+=1;
                        System.out.println("Total number for the API calls till now "+number_of_api_calls);
                        JSONObject response = (JSONObject)parser.parse(httpResponse.parseAsString());
                        JSONArray results = (JSONArray)response.get("result");

                        long endTime   = System.currentTimeMillis();

                        long totalTime = endTime - startTime;
                        //System.out.println("Total time for the API calls"+totalTime);

                        for (Object result : results) {

                            JSONObject jsonObject = (JSONObject) result;

                            String subject=(String) jsonObject.get("name");
                            JSONArray predicate_values=(JSONArray) jsonObject.get(category);
                            if(predicate_values!=null){
                                Iterator<String> iterator = predicate_values.iterator();
                                while (iterator.hasNext())
                                {

                                    String object=iterator.next();
                                    HashMap<String, String> Map_Key = new HashMap<String, String>();
                                    Map_Key.put(subject, object);

                                    //adding things into hashmap
                                    if (!Map_One.containsKey(Map_Key)) {
                                        count = +1;
                                        Map_One.put(Map_Key, count);
                                    }
                                }
                            }
                        }


                    }

                     /*reading from a hashmap and writing it to the outfile
            */
                    //extracting all the keys from initial hashmap
                    for (HashMap<String,String> Map_Value:Map_One.keySet()){
                        Map_List.add(Map_Value);
                    }
                    //iterating over the inner hashmap list
                    Iterator<HashMap<String,String>> iterator=Map_List.iterator();
                    while(iterator.hasNext()){
                        for (Map.Entry<String, String> entry  : iterator.next().entrySet()){
                            String write_line=extraction_clause+COMMA_DELIMITER+entry.getKey()+COMMA_DELIMITER+entry.getValue();
                            //System.out.println(write_line);
                            fileWriter.append(write_line);
                            fileWriter.append(NEW_LINE_SEPARATOR);
                        }

                    }

                }

            }

        }
        catch (IOException e) {
            logger.info("IO exception");
            logger.info(String.valueOf(e));
            e.printStackTrace();
        } catch (NullPointerException e) {
            logger.info("Null Pointer Exception");
            logger.info(String.valueOf(e));
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while closing fileReader/Writer !!!");
                e.printStackTrace();
            }
        }
        reader.close();
        return true;

    }
}
