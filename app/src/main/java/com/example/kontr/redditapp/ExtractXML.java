package com.example.kontr.redditapp;

import java.util.ArrayList;
import java.util.List;

public class ExtractXML {

    private static final String TAG = "ExtractXML";

    private String tag;
    private String xml;

    public ExtractXML(String tag, String xml) {
        this.tag = tag;
        this.xml = xml;
    }

    public List<String> start(){
        List<String> result = new ArrayList<>();

        String[] splitXML = xml.split(tag + "\"");
        int count = splitXML.length;

        return result;
    }
}
