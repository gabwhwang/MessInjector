package com.inetpsa.seph.messinjection;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageAnalyze {
    private static final Logger logger = LoggerFactory.getLogger(MessageAnalyze.class);
    private static final String ARCHITECTURE_SHEET_NAME = "Application Interfaces";
    private static final String FRAME_SHEET_NAME = "FRAMES";
    private static final String SIGNALS_SHEET_NAME = "SIGNALS";

    //Column index in architecture file
    private static int COLUMN_GROUP;
    private static int COLUMN_DATA_ELEMENT;
    private static int COLUMN_SWC_PRODUCER;
    private static int COLUMN_SWC_CONSUMER;
    private static int COLUMN_DATA_DESCRIPTION;
    private static int COLUMN_DATA_TYPE;
    private static int COLUMN_DATA_UNIT;
    private static int COLUMN_DATA_WIDTH;
    private static int COLUMN_DATA_STATES;
    private static int COLUMN_DATA_INIT_VALUE;
    private static int COLUMN_DATA_PHYS_MIN;
    private static int COLUMN_DATA_PHYS_MAX;
    private static int COLUMN_RUNNABLE_PRODUCER;
    private static int COLUMN_RUNNABLE_CONSUMER;

    //Column index in message file
    private static int SIGNAL_ENABLING_FLAG;
    private static int FRAME_ENABLING_FLAG;
    private static int FRAME_RADICAL;
    private static int SIGNAL_BYTE_POSITION;
    private static int SIGNAL_BIT_POSITION;
    private static int SIGNAL_LGTH;
    private static int LID_VALUE;
    private static int SIGNAL_STUBBED_MNEMONIC;
    private static int SIGNAL_MNEMONIC;
    private static int SIGNAL_PLAINTEXT_NAME_ENG;
    private static int SIGNAL_TYPE;
    private static int SIGNAL_STATE_ENG;
    private static int SIGNAL_PHY_UNIT_ENG;
    private static int SIGNAL_PHY_MIN_VALUE;
    private static int SIGNAL_PHY_MAX_VALUE;
    private static int SIGNAL_TX_INIT_VALUE;
    private static int SIGNAL_RX_INIT_VALUE;
    private static int FRAME_TRANSMITTERECU;
    private static int SIGNAL_TRANSMITTERECU;
    private static int SIGNAL_RECEIVERECU;
    private static int SIGNAL_INTFUNCTNAME1;
    private static int SIGNAL_UNAVAILABLE_FLAG;

    private Map<String, InterfaceAttributes> fluxInArchitecture = new HashMap<>();
    private Map<String, Boolean> frameContainingCHKCPT = new HashMap<>();
    private Map<String, List<InterfaceAttributes>> interfaceAggregatedByFrame = new HashMap<>();
    private Map<String, InterfaceAttributes> labelAutosarFluxInMessager = new HashMap<>();
    //OutPut:
    private Map<String, InterfaceAttributes> fluxNonFunctionalToAdd = new HashMap<>();
    private List<String> labelAutosarFluxNamesToAdd = new ArrayList<>();
    private List<InterfaceAttributes> canExternalFluxToAdd = new ArrayList<>();
    private List<InterfaceAttributes> canExternalFluxToUpdate = new ArrayList<>();
    private List<InterfaceAttributes> architectureFluxToDelete = new ArrayList<>();
    private List<InterfaceAttributes> labelAutosarFluxToAdd = new ArrayList<>();
    private List<InterfaceAttributes> labelAutosarFluxToUpdate = new ArrayList<>();

   public void messInjection(String architectureFilePath, String messageFilePath, String outputPath) throws IOException, MessageFormaException {
       importTBMUArchitectureFile(architectureFilePath);
       importMessageFile(messageFilePath);
       analyzeFlux();
       messInjectionWriter.writeCSVReport(fluxNonFunctionalToAdd, outputPath+"/nonFunctonalToAdd.csv");
       messInjectionWriter.writeCSVReport(canExternalFluxToAdd, outputPath+"/canExternalFluxToAdd.csv");
       messInjectionWriter.writeCSVReport(canExternalFluxToUpdate, outputPath+"/canExternalFluxToUpdate.csv");
       messInjectionWriter.writeCSVReport(architectureFluxToDelete, outputPath+"/architectureFluxToDelete.csv");
       messInjectionWriter.writeCSVReportDebug(interfaceAggregatedByFrame, outputPath+"/interfaceAggregatedByFrame.csv");


       messInjectionWriter.writeCSVReport(labelAutosarFluxToAdd, outputPath+"/labelAutosarFluxToAdd.csv");
       messInjectionWriter.writeCSVReport(labelAutosarFluxToUpdate, outputPath+"/labelAutosarFluxToUpdate.csv");
   }

    private void analyzeFlux() {
        completeExternalSWFlux (interfaceAggregatedByFrame, frameContainingCHKCPT);
        interfaceAggregatedByFrame.forEach((frameName, externalSWFlux) -> {
            for(InterfaceAttributes canFlux : externalSWFlux){
                if(fluxInArchitecture.containsKey(canFlux.getDataElement())){//
                    fluxInArchitecture.get(canFlux.getDataElement()).setBeAnalyzed(true);
                    if(!isFunctionalIdenticFlux(canFlux, fluxInArchitecture.get(canFlux.getDataElement()))){
                        canExternalFluxToUpdate.add(canFlux);
                    }
                } else {
                    canExternalFluxToAdd.add(canFlux);
                }
            }
        });

        labelAutosarFluxInMessager.forEach((labelAutosarName, interfaceAttributes) -> {
            if(fluxInArchitecture.containsKey(labelAutosarName)){//
                fluxInArchitecture.get(labelAutosarName).setBeAnalyzed(true);
                if(!isLabelAutosarIdenticFlux(interfaceAttributes, fluxInArchitecture.get(labelAutosarName))){
                    labelAutosarFluxToUpdate.add(interfaceAttributes);
                }
            } else {
                labelAutosarFluxToAdd.add(interfaceAttributes);
            }

        });

        fluxInArchitecture.forEach((fluxName, architectureFlux) -> {
            if(!architectureFlux.isBeAnalyzed()){
                architectureFluxToDelete.add(architectureFlux);
            }
        });

    }

    private boolean isLabelAutosarIdenticFlux(InterfaceAttributes labelAutosarFlux, InterfaceAttributes architectureFlux) {
       boolean dataType = false;
       boolean state = false;
       boolean minValue = false;
       boolean maxValue = false;
       if(labelAutosarFlux.getDataType().equals(architectureFlux.getDataType())){
           labelAutosarFlux.setDataType("IDENTICAL");
           dataType=true;
       }
        if(MessageTranslator.compareState(labelAutosarFlux.getStates(), architectureFlux.getStates())){
            labelAutosarFlux.setStates("IDENTICAL");
            state = true;
        }
        if(MessageTranslator.compareStringNumber(labelAutosarFlux.getPhysMinValue(), architectureFlux.getPhysMinValue())){
            labelAutosarFlux.setPhysMinValue("IDENTICAL");
            minValue = true;
        }
        if(MessageTranslator.compareStringNumber(labelAutosarFlux.getPhysMaxValue(), architectureFlux.getPhysMaxValue())){
            labelAutosarFlux.setPhysMaxValue("IDENTICAL");
            maxValue=true;
        }
       return dataType&&state&&minValue&&maxValue;
    }

    private boolean isFunctionalIdenticFlux(InterfaceAttributes canFlux, InterfaceAttributes architectureFlux) {
       return canFlux.getSwcProducerNames().equals(architectureFlux.getSwcProducerNames())
               && canFlux.getSwcConsumerNames().equals(architectureFlux.getSwcConsumerNames())
               && canFlux.getRunnableProducerNames().equals(architectureFlux.getRunnableProducerNames())
               && canFlux.getRunnableConsumerNames().equals(architectureFlux.getRunnableConsumerNames());
    }

    private void completeExternalSWFlux(Map<String, List<InterfaceAttributes>> interfaceAggregatedByFrame, Map<String, Boolean> frameContainingCHKCPT) {
        frameContainingCHKCPT.forEach((frameName, isCHKCPT) -> {
            List<InterfaceAttributes> externalSWFlux = interfaceAggregatedByFrame.get(frameName);
            if (isCHKCPT) {
                if(externalSWFlux.get(0).getGroup().equals("Composition Interface Transmission")){//Tx:
                    for(InterfaceAttributes externalFlux : externalSWFlux){
                        if(isCHKCPTSignal(externalFlux.getDataElement())){
                            externalFlux.setSwcProducerNames("CanSftyTx");
                            externalFlux.setSwcConsumerNames("TOPLEVELCOMPOSITION");
                            externalFlux.setRunnableProducerNames("CanSftyTx.1");
                            externalFlux.setRunnableConsumerNames("-");
                        }else{
                            externalFlux.setSwcProducerNames("CdTSignalTx");
                            externalFlux.setSwcConsumerNames("TOPLEVELCOMPOSITION\nCanSftyTx");
                            externalFlux.setRunnableProducerNames("CdTSignalTx.1");
                            externalFlux.setRunnableConsumerNames("CanSftyTx.1");
                        }
                    }
                }else{//Rx:
                    String frameId="";
                    for(InterfaceAttributes externalFlux : externalSWFlux){
                        if(isCHKCPTSignal(externalFlux.getDataElement())){
                            frameId = MessageTranslator.getFrameId(externalFlux.getDataElement());
                            externalFlux.setSwcProducerNames("TOPLEVELCOMPOSITION");
                            externalFlux.setSwcConsumerNames("CanSftyRx");
                            externalFlux.setRunnableProducerNames("-");
                            externalFlux.setRunnableConsumerNames("CanSftyRx.1");
                        }else{
                            externalFlux.setSwcProducerNames("TOPLEVELCOMPOSITION");
                            externalFlux.setSwcConsumerNames("CdTSignalRx\nCanSftyRx");
                            externalFlux.setRunnableProducerNames("-");
                            externalFlux.setRunnableConsumerNames("CdTSignalRx.1\nCanSftyRx.1");
                        }
                    }

                    //TODO : stDgoChk095
                    if(frameId.equals("")){
                        try {
                            throw new MessageFormaException("Format for CHKCTP is not standard :/ " + frameName);
                        } catch (MessageFormaException e) {
                            e.printStackTrace();
                        }
                    }

                    if(fluxInArchitecture.containsKey("stDgoChk"+frameId)){
                        fluxInArchitecture.get("stDgoChk"+frameId).setBeAnalyzed(true);
                        //already exist, marking
                    } else {
                        InterfaceAttributes  checksumSafetyFlux = createCanSafetyFlux("stDgoChk"+frameId);
                        fluxNonFunctionalToAdd.put("stDgoChk"+frameId, checksumSafetyFlux);
                    }

                    if(fluxInArchitecture.containsKey("stDgoCnt"+frameId)){
                        fluxInArchitecture.get("stDgoCnt"+frameId).setBeAnalyzed(true);
                        //already exist, marking
                    } else {
                        InterfaceAttributes  checksumSafetyFlux = createCanSafetyFlux("stDgoCnt"+frameId);
                        fluxNonFunctionalToAdd.put("stDgoCnt"+frameId, checksumSafetyFlux);
                    }

                }
            } else {
                if(externalSWFlux.get(0).getGroup().equals("Composition Interface Transmission")) {//Tx:
                    for(InterfaceAttributes externalFlux : externalSWFlux){
                        externalFlux.setSwcProducerNames("CdTSignalTx");
                        externalFlux.setSwcConsumerNames("TOPLEVELCOMPOSITION");
                        externalFlux.setRunnableProducerNames("CdTSignalTx.1");
                        externalFlux.setRunnableConsumerNames("-");
                    }
                }else{//Rx
                    for(InterfaceAttributes externalFlux : externalSWFlux){
                        externalFlux.setSwcProducerNames("TOPLEVELCOMPOSITION");
                        externalFlux.setSwcConsumerNames("CdTSignalRx");
                        externalFlux.setRunnableProducerNames("-");
                        externalFlux.setRunnableConsumerNames("CdTSignalRx.1");
                    }
                }
            }
        });
    }

    private InterfaceAttributes createCanSafetyFlux(String fluxName) {
        InterfaceAttributes interfaceAttributes = new InterfaceAttributes();
        interfaceAttributes.setGroup("CAN Safety Error");
        interfaceAttributes.setDataElement(fluxName);
        if(fluxName.startsWith("stDgoChk")){
            interfaceAttributes.setDataDescription("CAN Safety Checksum error for RX frame");
        }else{
            interfaceAttributes.setDataDescription("CAN Safety Process Counter error for RX frame");
        }
        interfaceAttributes.setSwcProducerNames("FltMgr");
        interfaceAttributes.setSwcConsumerNames("CanSftyRx");
        interfaceAttributes.setDataType("CS");
        interfaceAttributes.setUnit("PSA_no_unit");
        interfaceAttributes.setDataWidth("1");
        interfaceAttributes.setStates("-");
        interfaceAttributes.setInitValue("-");
        interfaceAttributes.setPhysMinValue("-");
        interfaceAttributes.setPhysMaxValue("-");
        interfaceAttributes.setRunnableProducerNames("SetEventStatus");
        interfaceAttributes.setRunnableConsumerNames("CanSftyRx.1");

        return interfaceAttributes;
    }

    public void importTBMUArchitectureFile(String xlsFilePath) throws IOException, MessageFormaException {

        File excelFile = new File(xlsFilePath);
        FileInputStream fileInputStream = new FileInputStream(excelFile);
        XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);


        for (Row row : workbook.getSheet(ARCHITECTURE_SHEET_NAME)) {
            if (row.getRowNum() == 0) {
                initilazeColumnIndexForArchitectureFile(row);
                continue;
            }
            if (row == null || row.getCell(0) == null || row.getCell(0).toString().equals("")) {//This check is sometime necessary for the last raw
                break;
            }

            if(isCdtSignalRelatedFlux(row)){
                InterfaceAttributes interfaceAttributes = new InterfaceAttributes();

                interfaceAttributes.setGroup(row.getCell(COLUMN_GROUP).toString());
                String dataElementName = getDataElementName(row);
                interfaceAttributes.setDataElement(dataElementName);
                interfaceAttributes.setSwcProducerNames(row.getCell(COLUMN_SWC_PRODUCER).toString());
                interfaceAttributes.setSwcConsumerNames(row.getCell(COLUMN_SWC_CONSUMER).toString());
                interfaceAttributes.setDataDescription(row.getCell(COLUMN_DATA_DESCRIPTION).toString());
                interfaceAttributes.setDataType(row.getCell(COLUMN_DATA_TYPE).toString());
                interfaceAttributes.setUnit(row.getCell(COLUMN_DATA_UNIT).toString());
                interfaceAttributes.setDataWidth(row.getCell(COLUMN_DATA_WIDTH).toString());
                interfaceAttributes.setStates(row.getCell(COLUMN_DATA_STATES).toString());
                interfaceAttributes.setInitValue(row.getCell(COLUMN_DATA_INIT_VALUE).toString());
                interfaceAttributes.setPhysMinValue(row.getCell(COLUMN_DATA_PHYS_MIN).toString());
                interfaceAttributes.setPhysMaxValue(row.getCell(COLUMN_DATA_PHYS_MAX).toString());
                interfaceAttributes.setRunnableProducerNames(row.getCell(COLUMN_RUNNABLE_PRODUCER).toString());
                interfaceAttributes.setRunnableConsumerNames(row.getCell(COLUMN_RUNNABLE_CONSUMER).toString());

                if(fluxInArchitecture.containsKey(dataElementName)){
                    throw new MessageFormaException("Duplicated elements :/ " + dataElementName);
                }
                fluxInArchitecture.put(dataElementName, interfaceAttributes);
            }

        }

        workbook.close();
        fileInputStream.close();
    }


    private String getDataElementName(Row row) {
        if(forceDataElementName(row)){
            return row.getCell(COLUMN_DATA_ELEMENT).toString();
        }
        if(row.getCell(COLUMN_SWC_PRODUCER).toString().split("\n").length>1){//only for CellEstm
            return "CellEstim_" + row.getCell(COLUMN_DATA_ELEMENT).toString();
        }
        return row.getCell(COLUMN_SWC_PRODUCER).toString() + "_" + row.getCell(COLUMN_DATA_ELEMENT).toString();
    }

    private boolean forceDataElementName(Row row) {
        if(forceDataElementNameForProducer(row.getCell(COLUMN_SWC_PRODUCER).toString())||
                forceDataElementNameForConsumer(row.getCell(COLUMN_SWC_CONSUMER).toString())){
            return true;
        }
        return false;
    }

    private boolean forceDataElementNameForProducer(String swcNames) {
        String[] swcNamesSplit = swcNames.split("\n");
        for(String swcName : swcNamesSplit){
            switch(swcName){
                case "RCD" :
                case "CdTSignalTx" :
                case "CdTSignalRx" :
                case "FltMgr" :
                case "Stub" :
                case "TOPLEVELCOMPOSITION" :
                    return true;
            }
        }
        return false;
    }

    private boolean forceDataElementNameForConsumer(String swcNames) {
        String[] swcNamesSplit = swcNames.split("\n");
        for(String swcName : swcNamesSplit){
            switch(swcName){
                case "RCD" :
                case "CdTSignalRx" :
                case "TOPLEVELCOMPOSITION" :
                    return true;
            }
        }
        return false;
    }

    public void importMessageFile(String xlsFilePath) throws IOException, MessageFormaException {

        File excelFile = new File(xlsFilePath);
        FileInputStream fileInputStream = new FileInputStream(excelFile);
        XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);

        List<String> externalSWFluxNames = new ArrayList<>();

        for (Row row : workbook.getSheet(FRAME_SHEET_NAME)) {
            if (row.getRowNum() == 0||row.getRowNum() == 2) {
                continue;
            }
            if (row.getRowNum() == 1) {
                initilazeColumnIndexForMessageFile(row);
                continue;
            }
            if (row == null || row.getCell(0) == null || row.getCell(0).toString().equals("")) {//This check is sometime necessary for the last raw
                break;
            }
            //CAN Frame Activation Flux
            analyzeCanActivationFlux(row.getCell(FRAME_ENABLING_FLAG).toString(),row.getCell(FRAME_TRANSMITTERECU).toString(),false);

        }

        for (Row row : workbook.getSheet(SIGNALS_SHEET_NAME)) {
            if (row.getRowNum() == 0||row.getRowNum() == 2) {
                continue;
            }
            if (row.getRowNum() == 1) {
                initilazeColumnIndexForMessageFile(row);
                continue;
            }
            if (row == null || row.getCell(0) == null || row.getCell(0).toString().equals("")) {//This check is sometime necessary for the last raw
                break;
            }
            if(!row.getCell(SIGNAL_STUBBED_MNEMONIC).toString().equals("STUBBED_SIGNAL")
            && !MessageTranslator.isIgnoredContent(row.getCell(SIGNAL_INTFUNCTNAME1).toString())){

                //CAN Signal Activation Flux
                analyzeCanActivationFlux(row.getCell(SIGNAL_ENABLING_FLAG).toString(),row.getCell(SIGNAL_TRANSMITTERECU).toString(),true);
                //CAN Frame Activation Flux
                //analyzeCanActivationFlux(row.getCell(FRAME_ENABLING_FLAG).toString(),row.getCell(SIGNAL_TRANSMITTERECU).toString(),false);
                //Signal Unavaliable Flag
                analyzeSignalUnavailableFlag(row.getCell(SIGNAL_UNAVAILABLE_FLAG).toString(),row.getCell(SIGNAL_TRANSMITTERECU).toString());

                //-------flux composition
                markCHKCPTFrame(row.getCell(FRAME_RADICAL).toString(),row.getCell(SIGNAL_MNEMONIC).toString());

                String externalSWFluxName = MessageTranslator.getSwFluxName(row.getCell(FRAME_RADICAL).toString(),
                        row.getCell(SIGNAL_BYTE_POSITION).toString(),
                        row.getCell(SIGNAL_BIT_POSITION).toString(),
                        row.getCell(LID_VALUE).toString(),
                        row.getCell(SIGNAL_MNEMONIC).toString());
                /*if(externalSWFluxName.equals("NEW_JDD_TBMU_774_B1_b5_NUMERO_TRAME")){
                    String test = "";
                }*/

                if(externalSWFluxNames.contains(externalSWFluxName)){
                    throw new MessageFormaException("duplicated Frame_Signal not allowed :/ " + externalSWFluxName);
                }
                externalSWFluxNames.add(externalSWFluxName);

                String unit = MessageTranslator.getUnit(row.getCell(SIGNAL_PHY_UNIT_ENG).toString());

                String state = MessageTranslator.getStates(row.getCell(SIGNAL_TYPE).toString(), row.getCell(SIGNAL_STATE_ENG).toString());

                String dataType = MessageTranslator.getExternalSWFluxDataType(row.getCell(SIGNAL_TYPE).toString(), row.getCell(SIGNAL_LGTH).toString());

                String initValue = MessageTranslator.getExternalSWFluxInitValue(row.getCell(SIGNAL_TRANSMITTERECU).toString(), row.getCell(SIGNAL_TX_INIT_VALUE).toString(), row.getCell(SIGNAL_RX_INIT_VALUE).toString());

                InterfaceAttributes externalSWFlux = createExternalSWFlux(row.getCell(SIGNAL_TRANSMITTERECU).toString(),
                        row.getCell(SIGNAL_PLAINTEXT_NAME_ENG).toString(),
                        externalSWFluxName, unit, state, dataType, row.getCell(SIGNAL_LGTH).toString(), initValue);

                if(interfaceAggregatedByFrame.containsKey(row.getCell(FRAME_RADICAL).toString())){
                    interfaceAggregatedByFrame.get(row.getCell(FRAME_RADICAL).toString()).add(externalSWFlux);
                }else{
                    List<InterfaceAttributes> externalSWFluxList = new ArrayList<>();
                    externalSWFluxList.add(externalSWFlux);
                    interfaceAggregatedByFrame.put(row.getCell(FRAME_RADICAL).toString(), externalSWFluxList);
                }

                //Label AUTOSAR:
                if(!isCHKCPTSignal(row.getCell(SIGNAL_MNEMONIC).toString())){
                    String labelAutosarName = row.getCell(SIGNAL_INTFUNCTNAME1).toString();
                    String labelAutosarDataType = "float32";
                    if(row.getCell(SIGNAL_TYPE).toString()==null
                            ||row.getCell(SIGNAL_TYPE).toString().isEmpty()
                            ||row.getCell(SIGNAL_TYPE).toString().equals("BMP")){//Need tobe optimized
                        labelAutosarDataType = MessageTranslator.getExternalSWFluxDataType("BMP", row.getCell(SIGNAL_LGTH).toString());
                    }
                    if(fluxInArchitecture.containsKey(labelAutosarName)){
                        fluxInArchitecture.get(labelAutosarName).setBeAnalyzed(true);
                        //already exist, marking
                    } else {
                        if(!labelAutosarFluxNamesToAdd.contains(labelAutosarName)){
                            labelAutosarFluxNamesToAdd.add(labelAutosarName);
                        }
                    }

                    //Analyze
                    if(!labelAutosarFluxInMessager.containsKey(labelAutosarName)){
                        InterfaceAttributes labelAutosarFlux = createLabelAutosarFlux(row.getCell(SIGNAL_TRANSMITTERECU).toString(),
                                row.getCell(SIGNAL_PLAINTEXT_NAME_ENG).toString(),
                                labelAutosarName, labelAutosarDataType, state, unit,
                                row.getCell(SIGNAL_LGTH).toString(),
                                row.getCell(SIGNAL_PHY_MIN_VALUE).toString(),
                                row.getCell(SIGNAL_PHY_MAX_VALUE).toString());
                        labelAutosarFluxInMessager.put(labelAutosarName, labelAutosarFlux);
                    }
                }
            }
        }

        workbook.close();
        fileInputStream.close();
    }

    private InterfaceAttributes createLabelAutosarFlux(String transmissionEcus,String signalDescription, String labelAutosarName, String dataType, String state, String unit, String signalLength, String physMinValue, String physMaxValue) {
        InterfaceAttributes interfaceAttributes = new InterfaceAttributes();
        if(MessageTranslator.isTxSignal(transmissionEcus)){//Tx
            interfaceAttributes.setGroup("Automatic generated for Transmission label autosar flux");
            interfaceAttributes.setDataElement(MessageTranslator.getLabelAutosarNameForTx(labelAutosarName));

            interfaceAttributes.setSwcProducerNames(MessageTranslator.getSwProducerNameFromLabelAutosar(labelAutosarName));
            interfaceAttributes.setRunnableProducerNames(MessageTranslator.getSwProducerNameFromLabelAutosar(labelAutosarName) + ".1");
            interfaceAttributes.setSwcConsumerNames("CdTSignalTx");
            interfaceAttributes.setRunnableConsumerNames("CdTSignalTx.1");

        }else{//Rx
            interfaceAttributes.setGroup("Automatic generated for Recpetion label autosar flux");
            interfaceAttributes.setDataElement(labelAutosarName);

            interfaceAttributes.setSwcProducerNames("CdTSignalRx");
            interfaceAttributes.setRunnableProducerNames("CdTSignalRx.1");
            interfaceAttributes.setSwcConsumerNames("Stub");
            interfaceAttributes.setRunnableConsumerNames("Stub.1");
        }
        interfaceAttributes.setDataDescription(signalDescription);

        interfaceAttributes.setDataType(dataType);
        interfaceAttributes.setUnit(unit);
        interfaceAttributes.setDataWidth("1");
        interfaceAttributes.setStates(state);
        if(!state.equals("-")){
            interfaceAttributes.setInitValue("0");
            interfaceAttributes.setPhysMinValue("0");
            interfaceAttributes.setPhysMaxValue(String.valueOf(state.split("\n").length-1));
        }else if(physMinValue.equals("Not applicable")||physMaxValue.equals("Not applicable")){
            if(dataType.startsWith("uint")){
                interfaceAttributes.setInitValue("0");
                interfaceAttributes.setPhysMinValue("0");
                interfaceAttributes.setPhysMaxValue(String.valueOf((int)Math.pow(2, Double.parseDouble(signalLength))-1));
            }else{
                interfaceAttributes.setInitValue("0");
                interfaceAttributes.setPhysMinValue(String.valueOf(-(int)Math.pow(2, Double.parseDouble(signalLength)-1)));
                interfaceAttributes.setPhysMaxValue(String.valueOf((int)Math.pow(2, Double.parseDouble(signalLength)-1)-1));
            }
        }else{
            interfaceAttributes.setInitValue(physMinValue);
            interfaceAttributes.setPhysMinValue(physMinValue);
            interfaceAttributes.setPhysMaxValue(physMaxValue);
        }

       return interfaceAttributes;
    }

    private InterfaceAttributes createExternalSWFlux(String transmitterECU, String signalDescription, String externalSWFluxName, String unit, String state, String dataType, String signalLength, String initValue) {
        InterfaceAttributes interfaceAttributes = new InterfaceAttributes();

        if(MessageTranslator.isTxSignal(transmitterECU)){//Tx
            interfaceAttributes.setGroup("Composition Interface Transmission");
        }else{//Rx
            interfaceAttributes.setGroup("Composition Interface Reception");
        }
        interfaceAttributes.setDataDescription(signalDescription);
        interfaceAttributes.setDataElement(externalSWFluxName);
        interfaceAttributes.setDataType(dataType);
        interfaceAttributes.setUnit(unit);
        interfaceAttributes.setDataWidth("1");
        interfaceAttributes.setStates(state);
        interfaceAttributes.setInitValue(initValue);
        if(!state.equals("-")){
            interfaceAttributes.setPhysMinValue("0");
            interfaceAttributes.setPhysMaxValue(String.valueOf(state.split("\n").length-1));


        }else if(dataType.startsWith("uint")){
            interfaceAttributes.setPhysMinValue("0");
            interfaceAttributes.setPhysMaxValue(String.valueOf((int)Math.pow(2, Double.parseDouble(signalLength))-1));
        }else{
            interfaceAttributes.setPhysMinValue(String.valueOf(-(int)Math.pow(2, Double.parseDouble(signalLength)-1)));
            interfaceAttributes.setPhysMaxValue(String.valueOf((int)Math.pow(2, Double.parseDouble(signalLength)-1)-1));
        }

        return interfaceAttributes;
    }

    private void markCHKCPTFrame(String frameName, String signalName) {
        if(!frameContainingCHKCPT.containsKey(frameName)||!frameContainingCHKCPT.get(frameName)){
            frameContainingCHKCPT.put(frameName, isCHKCPTSignal(signalName));
        }

    }

    private Boolean isCHKCPTSignal(String signalName) {
        return signalName.contains("CHKSUM_TRAME")||signalName.contains("CHKSUM_FRAME")||signalName.contains("CPT_PROCESS");
    }

    private void analyzeSignalUnavailableFlag(String unavailableFlag, String transmissionEcus) throws MessageFormaException {
        if(!fluxNonFunctionalToAdd.containsKey(unavailableFlag)){
            if(!MessageTranslator.isIgnoredContent(unavailableFlag)){
                if(fluxInArchitecture.containsKey(unavailableFlag)){
                    fluxInArchitecture.get(unavailableFlag).setBeAnalyzed(true);
                    //already exist, marking
                } else {
                    InterfaceAttributes unavailableFlagFlux = createUnavailableFlagFlux(unavailableFlag, transmissionEcus);
                    fluxNonFunctionalToAdd.put(unavailableFlag, unavailableFlagFlux);
                }
            }

        }
    }

    private InterfaceAttributes createUnavailableFlagFlux(String unavailableFlag, String transmissionEcus) throws MessageFormaException {
        InterfaceAttributes interfaceAttributes = new InterfaceAttributes();

        interfaceAttributes.setGroup("CAN Signal Unavaliable Flag");
        interfaceAttributes.setDataDescription("CAN Signal Unavaliable Flag for CAN RX signal");

        interfaceAttributes.setDataElement(unavailableFlag);
        interfaceAttributes.setSwcProducerNames("CdTSignalRx");
        if(MessageTranslator.isTxSignal(transmissionEcus)){//Tx not allowed
            throw new MessageFormaException("CAN Signal Unavaliable Flag is only for CAN RX signal :/ " + unavailableFlag);
        }
        interfaceAttributes.setSwcConsumerNames("Stub");
        interfaceAttributes.setRunnableConsumerNames("Stub.1");
        interfaceAttributes.setDataType("boolean");
        interfaceAttributes.setUnit("PSA_no_unit");
        interfaceAttributes.setDataWidth("1");
        interfaceAttributes.setStates("-");
        interfaceAttributes.setInitValue("1");
        interfaceAttributes.setPhysMinValue("0");
        interfaceAttributes.setPhysMaxValue("1");
        interfaceAttributes.setRunnableProducerNames("CdTSignalRx.1");

        return interfaceAttributes;
    }

    private void analyzeCanActivationFlux(String activationFluxName, String transmissionEcus, boolean isSignal) {
       //if(activationFluxName!=null && !activationFluxName.isEmpty()&&!activationFluxName.equals("1")&&activationFluxName.equals("1.0")){
           if(!fluxNonFunctionalToAdd.containsKey(activationFluxName)){
               if(!activationFluxName.equals("1")
                       &&!activationFluxName.equals("1.0")
                       &&!MessageTranslator.isIgnoredContent(activationFluxName)){
                   if(fluxInArchitecture.containsKey(activationFluxName)){
                       fluxInArchitecture.get(activationFluxName).setBeAnalyzed(true);
                       //already exist, marking
                   } else {
                       InterfaceAttributes canActivationFlux = createCanActivationFlux(activationFluxName, transmissionEcus, isSignal);
                       fluxNonFunctionalToAdd.put(activationFluxName, canActivationFlux);
                   }
               }
           }
       //}

    }

    private InterfaceAttributes createCanActivationFlux(String activationFluxName, String transmissionEcus, boolean isSignal) {
        InterfaceAttributes interfaceAttributes = new InterfaceAttributes();

        if(isSignal){
            interfaceAttributes.setGroup("CAN Signal Activation Flux");
            interfaceAttributes.setDataDescription("CAN Signal Activation Flux");
        }else{
            interfaceAttributes.setGroup("CAN Frame Activation Flux");
            interfaceAttributes.setDataDescription("CAN Frame Activation Flux");
        }

        interfaceAttributes.setDataElement(activationFluxName);
        interfaceAttributes.setSwcProducerNames("Stub");
        if(MessageTranslator.isTxSignal(transmissionEcus)){//Tx
            interfaceAttributes.setSwcConsumerNames("CdTSignalTx");
            interfaceAttributes.setRunnableConsumerNames("CdTSignalTx.1");
        }else{
            interfaceAttributes.setSwcConsumerNames("CdTSignalRx");
            interfaceAttributes.setRunnableConsumerNames("CdTSignalRx.1");
        }
        interfaceAttributes.setDataType("boolean");
        interfaceAttributes.setUnit("PSA_no_unit");
        interfaceAttributes.setDataWidth("1");
        interfaceAttributes.setStates("-");
        interfaceAttributes.setInitValue("1");
        interfaceAttributes.setPhysMinValue("0");
        interfaceAttributes.setPhysMaxValue("1");
        interfaceAttributes.setRunnableProducerNames("Stub.1");

        return interfaceAttributes;
    }

    private boolean isCdtSignalRelatedFlux(Row row) {

        return isCdtSignalRelatedFlux(row.getCell(COLUMN_SWC_PRODUCER).toString())
                ||isCdtSignalRelatedFlux(row.getCell(COLUMN_SWC_CONSUMER).toString());
    }

    private boolean isCdtSignalRelatedFlux(String swcNames) {
        String[] swcNamesSplit = swcNames.split("\n");
        for(String swcName : swcNamesSplit){
            switch(swcName){
                case "CdTSignalTx" :
                case "CdTSignalRx" :
                case "CanSftyTx" :
                case "CanSftyRx":
                case "FcComRx" :
                case "TOPLEVELCOMPOSITION" :
                    return true;
            }
        }
        return false;
    }

    private void initilazeColumnIndexForArchitectureFile(Row row) {
        Iterator<Cell> cellItrator = row.cellIterator();
        while(cellItrator.hasNext()){
            Cell cell = cellItrator.next();
            switch (cell.toString().replaceAll(" ","").replaceAll("\n","")){
                case "Group" :
                    COLUMN_GROUP  = cell.getColumnIndex();
                    break;
                case "DataElement" :
                    COLUMN_DATA_ELEMENT = cell.getColumnIndex();
                    break;
                case "SWCProd" :
                    COLUMN_SWC_PRODUCER = cell.getColumnIndex();
                    break;
                case "SWCCons" :
                    COLUMN_SWC_CONSUMER = cell.getColumnIndex();
                    break;
                case "Description" :
                    COLUMN_DATA_DESCRIPTION = cell.getColumnIndex();
                    break;
                case "Datatype" :
                    COLUMN_DATA_TYPE = cell.getColumnIndex();
                    break;
                case "Unit" :
                    COLUMN_DATA_UNIT = cell.getColumnIndex();
                    break;
                case "Width" :
                    COLUMN_DATA_WIDTH = cell.getColumnIndex();
                    break;
                case "States" :
                    COLUMN_DATA_STATES = cell.getColumnIndex();
                    break;
                case "Initvalue" :
                    COLUMN_DATA_INIT_VALUE = cell.getColumnIndex();
                    break;
                case "Physmin" :
                    COLUMN_DATA_PHYS_MIN = cell.getColumnIndex();
                    break;
                case "Physmax" :
                    COLUMN_DATA_PHYS_MAX = cell.getColumnIndex();
                    break;
                case "RunnableProd" :
                    COLUMN_RUNNABLE_PRODUCER = cell.getColumnIndex();
                    break;
                case "RunnableCons" :
                    COLUMN_RUNNABLE_CONSUMER = cell.getColumnIndex();
                    break;
            }
        }
    }

    private void initilazeColumnIndexForMessageFile(Row row) {
        Iterator<Cell> cellItrator = row.cellIterator();
        while(cellItrator.hasNext()){
            Cell cell = cellItrator.next();
            switch (cell.toString().replaceAll(" ","").replaceAll("\n","")){
                case "Signal_Enabling_flag" :
                    SIGNAL_ENABLING_FLAG  = cell.getColumnIndex();
                    break;
                case "Frame_Enabling_flag" :
                    FRAME_ENABLING_FLAG = cell.getColumnIndex();
                    break;
                case "Frame_Radical" :
                    FRAME_RADICAL = cell.getColumnIndex();
                    break;
                case "Signal_Byte_Position" :
                    SIGNAL_BYTE_POSITION = cell.getColumnIndex();
                    break;
                case "Signal_Bit_Position" :
                    SIGNAL_BIT_POSITION = cell.getColumnIndex();
                    break;
                case "Signal_lgth" :
                    SIGNAL_LGTH = cell.getColumnIndex();
                    break;
                case "LID_Value" :
                    LID_VALUE = cell.getColumnIndex();
                    break;
                case "Signal_stubbed_Mnemonic" :
                    SIGNAL_STUBBED_MNEMONIC = cell.getColumnIndex();
                    break;
                case "Signal_Mnemonic" :
                    SIGNAL_MNEMONIC = cell.getColumnIndex();
                    break;
                case "Signal_PlainText_Name_eng" :
                    SIGNAL_PLAINTEXT_NAME_ENG = cell.getColumnIndex();
                    break;
                case "Signal_Type" :
                    SIGNAL_TYPE = cell.getColumnIndex();
                    break;
                case "Signal_State_eng" :
                    SIGNAL_STATE_ENG = cell.getColumnIndex();
                    break;
                case "Signal_Phy_Unit_eng" :
                    SIGNAL_PHY_UNIT_ENG = cell.getColumnIndex();
                    break;
                case "Signal_Phy_Min_Value" :
                    SIGNAL_PHY_MIN_VALUE = cell.getColumnIndex();
                    break;
                case "Signal_Phy_Max_Value" :
                    SIGNAL_PHY_MAX_VALUE = cell.getColumnIndex();
                    break;
                case "Signal_Tx_Init_Value" :
                    SIGNAL_TX_INIT_VALUE = cell.getColumnIndex();
                    break;
                case "Signal_Rx_Init_Value" :
                    SIGNAL_RX_INIT_VALUE = cell.getColumnIndex();
                    break;
                case "Frame_TransmitterECU" :
                    FRAME_TRANSMITTERECU = cell.getColumnIndex();
                    break;
                case "Signal_TransmitterECU" :
                    SIGNAL_TRANSMITTERECU = cell.getColumnIndex();
                    break;
                case "Signal_ReceiverECU" :
                    SIGNAL_RECEIVERECU = cell.getColumnIndex();
                    break;
                case "Signal_IntFunctName1" :
                    SIGNAL_INTFUNCTNAME1 = cell.getColumnIndex();
                    break;
                case "Signal_Unavailable_flag" :
                    SIGNAL_UNAVAILABLE_FLAG = cell.getColumnIndex();
                    break;
            }
        }
    }
}
