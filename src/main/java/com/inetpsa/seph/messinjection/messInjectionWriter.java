package com.inetpsa.seph.messinjection;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

public class messInjectionWriter {
    public static void writeCSVReport(Map<String, InterfaceAttributes> fluxMap, String outputPath) {
        String content = "Group;DataElement;BMS Functional Domain External Interface;SWC Prod;SWC Cons;Description;Datatype;Unit;Width;States;Initvalue;Physmin;Physmax;Runnable Prod;Runnable Cons\n";
        for(Map.Entry<String, InterfaceAttributes> map : fluxMap.entrySet()){
            content+=getCSVFormatContent(map.getValue()) + "\n";
        }

        writeCSVReport(content, outputPath);
    }

    public static void writeCSVReport(List<InterfaceAttributes> fluxList, String outputPath) {
        String content = "Group;DataElement;Rte_IsUpdated API;SWC Prod;SWC Cons;Description;Datatype;Unit;Width;States;Initvalue;Physmin;Physmax;Runnable Prod;Runnable Cons\n";
        for(InterfaceAttributes flux : fluxList){
            content+=getCSVFormatContent(flux) + "\n";
        }

        writeCSVReport(content, outputPath);
    }

    public static void writeCSVReportDebug(Map<String, List<InterfaceAttributes>> fluxMap, String outputPath) {
        String content = "Group;DataElement;Rte_IsUpdated API;SWC Prod;SWC Cons;Description;Datatype;Unit;Width;States;Initvalue;Physmin;Physmax;Runnable Prod;Runnable Cons\n";
        for(Map.Entry<String, List<InterfaceAttributes>> map : fluxMap.entrySet()){
            for(InterfaceAttributes flux : map.getValue()){
                content+=getCSVFormatContent(flux) + "\n";
            }
        }

        writeCSVReport(content, outputPath);
    }

    public static void writeCSVReport(String content, String outputPath) {

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputPath), "ISO-8859-15")) {
            writer.write(content);
        } catch (IOException ignored) {
        }
    }

    private static String getCSVFormatContent(InterfaceAttributes interfaceAttributes) {
        String content = "";
        if(interfaceAttributes.getGroup()!=null){
            content += interfaceAttributes.getGroup() + ";";

        }else{
            content +="No group found;";
        }
        if(interfaceAttributes.getDataElement()!=null){
            content += interfaceAttributes.getDataElement().trim() + ";";

        }else{
            content +=";";
        }
        content +="No;";
        if(interfaceAttributes.getSwcProducerNames()!=null){
            String contentName = interfaceAttributes.getSwcProducerNames();
            if(contentName.contains("\n")){
                contentName = "\"" + contentName + "\"";
            }
            content += contentName + ";";

        }else{
            content +=";";
        }

        if(interfaceAttributes.getSwcConsumerNames()!=null){
            String contentName = interfaceAttributes.getSwcConsumerNames();
            if(contentName.contains("\n")){
                contentName = "\"" + contentName + "\"";
            }
            content += contentName + ";";

        }else{
            content +=";";
        }

        if(interfaceAttributes.getDataDescription()!=null&&!interfaceAttributes.getDataDescription().equals("")){
            String contentName = interfaceAttributes.getDataDescription();
            if(contentName.contains("\n")){
                contentName = "\"" + contentName + "\"";
            }
            content += contentName + ";";

        }else{
            content +="No description found;";
        }
        if(interfaceAttributes.getDataType()!=null){
            content += interfaceAttributes.getDataType() + ";";

        }else{
            content +=";";
        }
        if(interfaceAttributes.getUnit()!=null){
            content += interfaceAttributes.getUnit() + ";";

        }else{
            content +=";";
        }

        if(interfaceAttributes.getDataWidth()!=null){
            content += interfaceAttributes.getDataWidth() + ";";

        }else{
            content +=";";
        }

        if(interfaceAttributes.getStates()!=null){
            String contentName = interfaceAttributes.getStates();
            if(contentName.contains("\n")){
                contentName = "\"" + contentName + "\"";
            }
            content += contentName + ";";

        }else{
            content +=";";
        }

        if(interfaceAttributes.getInitValue()!=null){
            content += interfaceAttributes.getInitValue() + ";";

        }else{
            content +=";";
        }

        if(interfaceAttributes.getPhysMinValue()!=null){
            content += interfaceAttributes.getPhysMinValue() + ";";

        }else{
            content +=";";
        }

        if(interfaceAttributes.getPhysMaxValue()!=null){
            content += interfaceAttributes.getPhysMaxValue() + ";";

        }else{
            content +=";";
        }

        if(interfaceAttributes.getRunnableProducerNames()!=null){
            String contentName = interfaceAttributes.getRunnableProducerNames();
            if(contentName.contains("\n")){
                contentName = "\"" + contentName + "\"";
            }
            content += contentName + ";";

        }else{
            content +=";";
        }

        if(interfaceAttributes.getRunnableConsumerNames()!=null){
            String contentName = interfaceAttributes.getRunnableConsumerNames();
            if(contentName.contains("\n")){
                contentName = "\"" + contentName + "\"";
            }
            content += contentName + ";";

        }else{
            content +=";";
        }

        return content;
    }


}
