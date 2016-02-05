package com.freebase.api.mid.main;

/**
 * Created by niranjan on 6/19/15.
 */

import com.freebase.api.mid.views.StatsInformationCaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;


@Component
@SuppressWarnings("unchecked")
public class OpenFDAClient {


    @Autowired
    StatsInformationCaller statsInformationCaller;

    public static void main(String args[]) throws IOException, PropertyVetoException, URISyntaxException, SQLException {

        String mid_files_address=args[0];
        String freebase_result_files_address=args[1];
        String wiki_resultfiles_address=args[2];

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("Freebase-Parser-app-context.xml");
        context.getBean(OpenFDAClient.class).statsInformationCaller.getAllStats(mid_files_address,freebase_result_files_address,wiki_resultfiles_address);
    }

}
