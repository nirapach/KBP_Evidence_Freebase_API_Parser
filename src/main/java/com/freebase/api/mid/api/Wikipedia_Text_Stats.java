package com.freebase.api.mid.api;

/**
 * Created by Niranjan on 2/4/2016.
 */

import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.CoreConnectionPNames;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.JsonPath;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


import javax.xml.ws.http.HTTPException;
import java.beans.PropertyVetoException;
import java.io.*;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;

@Service
@SuppressWarnings("unchecked")
public class Wikipedia_Text_Stats {


    Logger logger = LoggerFactory.getLogger(FreebaseStats.class);

    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";
    public static Properties properties = new Properties();
    // Allow one request per second
    private RateLimiter throttle = RateLimiter.create(1000.0);

    /*
      parsing all the json files
        */
    public boolean getWikitext(String file_address, String result_file_address) throws URISyntaxException, IOException, PropertyVetoException, SQLException, HTTPException {


        throttle.acquire();

        File indexdirectory = new File(file_address);

        File[] files = indexdirectory.listFiles();
        //url for the get request
        String Campaign_Stats_Get_URL = "en.wikipedia.org";
        CloseableHttpClient httpClient = null;

        //setting parameters for the get request
        URIBuilder builder;

        //getting the httpresponse
        CloseableHttpResponse httpResponse;
        //declaring the httpget request
        HttpGet httpGet;
        //json parser
        JSONParser parser = new JSONParser();

        BufferedReader wikiResponseReader = null;
        BufferedReader inputFileReader = null;
        FileWriter fileWriter = null;

        int CONNECTION_TIMEOUT = 10000*1000; // timeout in millis
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECTION_TIMEOUT)
                .setConnectTimeout(CONNECTION_TIMEOUT)
                .setSocketTimeout(CONNECTION_TIMEOUT)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .build();

        try {

            for (int i = 0; i < files.length; i++) {

                if (!files[i].isDirectory() && !files[i].isHidden() && files[i].canRead() && files[i].exists()) {
                    String input_file_canonical_path = files[i].getCanonicalPath();
                    System.out.println("\n Extraction of entities is going on with file" + input_file_canonical_path);
                    String extractionClause = input_file_canonical_path.substring(input_file_canonical_path.lastIndexOf("/") + 1, input_file_canonical_path.length());
                    //create File object
                    File txt_file = new File(result_file_address + "Wikipedia_Response" + extractionClause);
                    String textFileName = txt_file.getAbsolutePath();
                    //File writer
                    fileWriter = new FileWriter(textFileName);

                    FileReader fileReader = new FileReader(files[i].getCanonicalPath());
                    // Always wrap FileReader in BufferedReader.
                    inputFileReader = new BufferedReader(fileReader);
                    String inputFileLine;

                    int lineCount = 0;
                    while ((inputFileLine = inputFileReader.readLine()) != null) {

                        String[] inputTitles = inputFileLine.split(",");
                        lineCount+=1;
                        System.out.println("Processing file "+extractionClause+":"+lineCount);

                        for (int t = 1; t < inputTitles.length; t++) {

                            if ((inputTitles[t]!=null && inputTitles[t] !=" ") && (inputTitles[t-1] != inputTitles[t]) ) {

                                builder = new URIBuilder();
                                httpClient = HttpClients.createDefault();

                                //wiki extraction
                                builder.setScheme("https").setHost(Campaign_Stats_Get_URL).setPath("/w/api.php")
                                        .setParameter("format", "json")
                                        .setParameter("action", "query")
                                        .setParameter("prop", "extracts")
                                        .setParameter("explaintext", " ")
                                        .setParameter("titles", inputTitles[t]);

                                httpGet = new HttpGet(builder.build());
                                httpGet.setConfig(requestConfig);

                                httpResponse = httpClient.execute(httpGet);

                                wikiResponseReader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

                                String inputLine;
                                StringBuffer fbResponse = new StringBuffer();
                                while ((inputLine = wikiResponseReader.readLine()) != null) {
                                    fbResponse.append(inputLine);
                                }
                                String jsonFeed = fbResponse.toString();
                                JSONObject jsonMainResponse = (JSONObject) parser.parse(jsonFeed);
                                JSONObject jsonResponseQuery = (JSONObject) jsonMainResponse.get("query");
                                JSONObject jsonResponsePages = (JSONObject) jsonResponseQuery.get("pages");

                                Set<String> PagesKeys = jsonResponsePages.keySet();

                                for (String fieldNext : PagesKeys) {

                                    if (fieldNext != "-1") {
                                        JSONObject jsonResponseResults = (JSONObject) jsonResponsePages.get(fieldNext);
                                        Long wikiPageId = (Long) jsonResponseResults.get("pageid");
                                        String wikiTitle = (String) jsonResponseResults.get("title");
                                        String wikiExtract = (String) jsonResponseResults.get("extract");

                                        String writePageId = "<pageId>" + wikiPageId + "</pageId>";
                                        fileWriter.append(writePageId);
                                        fileWriter.append(NEW_LINE_SEPARATOR);
                                        String writeTitle = "<title>" + wikiTitle + "</title>";
                                        fileWriter.append(writeTitle);
                                        fileWriter.append(NEW_LINE_SEPARATOR);
                                        String writeExtract = "<extract>" + wikiExtract + "</extract>";
                                        fileWriter.append(writeExtract);
                                        fileWriter.append(NEW_LINE_SEPARATOR);
                                    }
                                }
                            }
                            wikiResponseReader.close();
                            httpClient.close();
                        }
                    }
                }
            }


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
        inputFileReader.close();
        return true;

    }

}
