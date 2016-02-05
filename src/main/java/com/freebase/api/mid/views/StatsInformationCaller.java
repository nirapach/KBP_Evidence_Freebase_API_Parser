package com.freebase.api.mid.views;

/**
 * Created by niranjan on 6/19/15.
 */


import com.freebase.api.mid.api.FreebaseStats;

import com.freebase.api.mid.api.Wikipedia_Text_Stats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;


@Service
@SuppressWarnings("unchecked")
public class StatsInformationCaller {

    //Autowired the instance for all the API classes

    @Autowired
    FreebaseStats freebaseStats;
    Wikipedia_Text_Stats wikipedia_text_stats;

    public void getAllStats(String mid_files_address, String result_file_address, String wiki_resultfiles_address) throws IOException, URISyntaxException, PropertyVetoException, SQLException {

        /*
        objects for all the stats create here statically
         */
        Logger logger = LoggerFactory.getLogger(StatsInformationCaller.class);

        //Calling the method to get ad group action stats

        boolean finishedFbStats = freebaseStats.getOverAllFBstats(mid_files_address, result_file_address);

        if (finishedFbStats) {
            System.out.println("Freebase API MID extraction Completed Successfully");

        }

        boolean finishedWikiStats = wikipedia_text_stats.getWikitext(result_file_address, wiki_resultfiles_address);

        if (finishedWikiStats) {
            System.out.println("Wikipedia API Text extraction Completed Successfully");

        }
    }

}
