package com.inetpsa.seph.messinjection;

import java.util.ArrayList;
import java.util.List;

public class MessageTranslator {

    //private static final Logger logger = LoggerFactory.getLogger(MessageTranslator.class);

    public static String getSwFluxName(String frameName, String bytePosition, String bitPosition, String lidValue, String signalName) throws MessageFormaException {
        if(lidValue.equals("Not applicable")||lidValue.equals("LID")){
            return frameName + "_B" + (int)(Double.parseDouble(bytePosition)) + "_b" + (int)(Double.parseDouble(bitPosition)) +"_" +signalName;
        }
        return frameName + "_LID" + getDecimalValue(lidValue) + "_B" + (int)(Double.parseDouble(bytePosition)) + "_b" + (int)(Double.parseDouble(bitPosition)) +"_" +signalName;
    }

    private static Integer getDecimalValue(String numberValue) throws MessageFormaException {
        if(numberValue.startsWith("0x")){
            return Integer.decode(numberValue);
        }else if(numberValue.startsWith("0b")){
            return Integer.parseInt(numberValue.substring(2),2);
        }else{
            throw new MessageFormaException("Unsupported format  :/ " + numberValue);
        }
    }

    public static boolean isTxSignal(String transmitterECU) {
        return transmitterECU.contains("TBMU");
    }

    public static String getUnit(String unitSymbol) throws MessageFormaException {
        switch (unitSymbol){
            case "%" : return "PSA_percent";
            case "°C" : return "PSA_degre";
            case "A" : return "PSA_ampere";
            case "A.h" :
            case "Ah" : return "PSA_ampere_hour";
            case "Degree C" : return "PSA_degre";
            case "h" : return "PSA_hour";
            case "hPa" : return "PSA_hectopascal";
            case "km" : return "PSA_kilometre";
            case "km/h" : return "PSA_kilometre_per_hour";
            case "kOhm" : return "PSA_kiloohm";
            case "kW.h":
            case "Kwh" : return "PSA_kilowatt_hour";
            case "mA" : return "PSA_milliampere";
            case "min" : return "PSA_minute";
            case "Not applicable" :
            case "-" :
            case "No Unit" :
            case "No unit" : return "PSA_no_unit";
            case "rpm" : return "PSA_revolution_per_minute";
            case "S" :
            case "s" : return "PSA_second";
            case "V" : return "PSA_volt";
            case "W" : return "PSA_watt";
            case "Wh" : return "PSA_watt_hour";
            default: throw new MessageFormaException("Unsupported unit :/ " + unitSymbol);
        }
    }

    public static String getStates(String signalType, String stateEng) {

        if(signalType.equals("BMP")){
            String states = "";
            String[] statesTab = stateEng.split("\n");
            List<String> stateContents = new ArrayList<>();
            for (String state : statesTab) {
                if(state.contains(":")){
                    String[] scaleInfo = state.split(":");
                    if (scaleInfo.length > 1) {
                        String stateToAdd = scaleInfo[1];
                        for(int index = 2; index <scaleInfo.length; index++){
                            stateToAdd += "_" + scaleInfo[index].trim();
                        }
                        stateToAdd = stateToAdd.trim().replaceAll(" ", "_");
                        if(stateToAdd.contains("+")){
                            stateToAdd = stateToAdd.replaceAll("\\+", "");
                        }
                        if(stateToAdd.contains("(")){
                            stateToAdd = stateToAdd.replaceAll("\\(", "");
                        }
                        if(stateToAdd.contains(".")){
                            stateToAdd = stateToAdd.replaceAll("\\.", "");
                        }
                        if(stateToAdd.contains(")")){
                            stateToAdd = stateToAdd.replaceAll("\\)", "");
                        }
                        if(stateToAdd.contains(",")){
                            stateToAdd = stateToAdd.replaceAll(",", "");
                        }
                        if(stateContents.contains(stateToAdd)){
                            stateToAdd = stateToAdd+"_" + stateContents.size();
                        }
                        stateContents.add(stateToAdd);
                    }
                }
            }
            if(stateContents.isEmpty()){
                return "-";
            }
            int index = 0;
            for(String stateContent : stateContents){
                states+=index + ":" + stateContent;
                if(index!=stateContents.size()-1){
                    index++;
                    states+="\n";
                }
            }
            return states;
        }
        return "-";
    }

    public static boolean isIgnoredContent(String cellContent) {
        if(cellContent==null||cellContent.isEmpty()){
            return true;
        }
        switch (cellContent.trim()){
            case "Non applicable":
            case "Not applicable":
            case "TBD":
            case "": return true;
        }
        return false;
    }

    public static String getExternalSWFluxDataType(String signalType, String signalLength) {
        if(null == signalType){
            return "uint"+getByteNumber(signalLength);
        }
        if(signalType.equals("SMP")){
            return "sint"+getByteNumber(signalLength);
        }
        return "uint"+getByteNumber(signalLength);
    }

    static String getByteNumber(String signalLength) {
        if(Double.parseDouble(signalLength)<=8){
            return "8";
        }else if(Double.parseDouble(signalLength)<=16){
            return "16";
        }else {
            return "32";
        }
    }

    public static String getExternalSWFluxInitValue(String transmitterECU, String signalTxInitValue, String signalRxInitValue) {
        if(isTxSignal(transmitterECU)){
            if(signalTxInitValue.startsWith("0x")){
                return String.valueOf(Long.parseLong(signalTxInitValue.substring(2), 16));
            }else if(signalTxInitValue.startsWith("0b")){
                return String.valueOf(Integer.parseInt(signalTxInitValue.substring(2),2));
            }else if(signalTxInitValue.contains("TBD")||signalTxInitValue.contains("Non applicable")||signalTxInitValue.contains("Last memorised value")){
                return "0";
            }else{
                return signalTxInitValue.trim();
            }

        }else{
            switch (signalRxInitValue){
                case "TBD":
                case "Non applicable":
                case "Last memorized value": return "0";
                case "252; 253": return "252";
                default: return signalRxInitValue.trim();
            }
        }

    }

    public static String getFrameId(String dataElement) {
        if(dataElement.length()>=3){
            return dataElement.substring(dataElement.length()-3);
        }
        return "";
    }

    public static String getLabelAutosarNameForTx(String labelAutosarName) {
        String labelName = "";
        if(labelAutosarName.contains("_")){
            String[] nameTab = labelAutosarName.split("_");
            labelName= labelAutosarName.substring(nameTab[0].length()+1);
        }
        return labelName;
    }

    public static String getSwProducerNameFromLabelAutosar(String labelAutosarName) {
        String labelName = "";
        if(labelAutosarName.contains("_")){
            String[] nameTab = labelAutosarName.split("_");
            labelName = nameTab[0];
        }
        return labelName;
    }

    public static boolean compareState(String states, String states1) {
        if(states.split("\n").length==states.split("\n").length){
            return true;
        }
        return false;
    }

    public static boolean compareStringNumber(String physValue, String physValue1) {
        return Double.parseDouble(physValue)==Double.parseDouble(physValue1);
    }
}
