package com.freebase.api.mid.responseparser.inputfile.resultinfo;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Niranjan on 1/25/2016.
 */
public class FBresults {

    @SerializedName("/type/object/id")
    public String type_mid;

    @SerializedName("/type/object/name")
    public String type_name;

    public type type;



}
