package com.inetpsa.seph.messinjection;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class MessageTranslatorTest {

    @Test
    public void getSwFluxName() throws MessageFormaException {

        String expected1 = "NEW_JDD_TBMU_774_B1_b7_NOMBRE_TRAMES";
        String actual1 = MessageTranslator.getSwFluxName("NEW_JDD_TBMU_774",
                "1",
                "7",
                "Non applicable",
                "NOMBRE_TRAMES");

        String expected2 = "NEW_JDD_TBMU_774_LID1_B2_b7_CODES_DEFAUT";
        String actual2 = MessageTranslator.getSwFluxName("NEW_JDD_TBMU_774",
                "2",
                "7",
                "0x01",
                "CODES_DEFAUT");

        String expected3 = "NEW_JDD_TBMU_774_LID2_B2_b7_KILOMETRAGE_JDD";
        String actual3 = MessageTranslator.getSwFluxName("NEW_JDD_TBMU_774",
                "2",
                "7",
                "0x02",
                "KILOMETRAGE_JDD");

        String expected4 = "NEW_JDD_TBMU_774_B1_b5_NOMBRE_TRAME";
        String actual4 = MessageTranslator.getSwFluxName("NEW_JDD_TBMU_774",
                "1",
                "5",
                "LID",
                "NOMBRE_TRAME");

        String expected5 = "DYN_TBMU_334_LID11_B2_b7_BMS_BATTCELLVOLT45_14B";
        String actual5 = MessageTranslator.getSwFluxName("DYN_TBMU_334",
                "2",
                "7",
                "0b01011",
                "BMS_BATTCELLVOLT45_14B");

        System.out.println(actual5);

        assertTrue(expected1.equals(actual1));
        assertTrue(expected2.equals(actual2));
        assertTrue(expected3.equals(actual3));
        assertTrue(expected4.equals(actual4));
        assertTrue(expected5.equals(actual5));
    }

    @Test
    public void isTxSignal() {

        assertTrue(MessageTranslator.isTxSignal("EPLU ; OBC_DCDC ; TBMU ; MCU"));
        assertTrue(!MessageTranslator.isTxSignal("EPLU ; OBC_DCDC  ; MCU"));
    }

    @Test
    public void getStates() {

        String expected1 = "0:UNPLUGGED\n" +
                "1:PARTIALLY_PLUGGED\n" +
                "2:PLUGGED\n" +
                "3:UNKNOWN";
        String actual1 = MessageTranslator.getStates("BMP", "00: UNPLUGGED \n" +
                "01: PARTIALLY PLUGGED \n" +
                "10: PLUGGED \n" +
                "11: UNKNOWN");

        assertTrue(expected1.equals(actual1));

        String expected2 = "0:UNAVAILABLE\n" +
                "1:MODE2_3_PWM\n" +
                "2:MODE3_PLC\n" +
                "3:MODE4_PLC\n" +
                "4:RESERVED\n" +
                "5:RESERVED_5\n" +
                "6:RESERVED_6\n" +
                "7:RESERVED_7";
        String actual2 = MessageTranslator.getStates("BMP", "000: UNAVAILABLE\n" +
                "001: MODE2_3_PWM\n" +
                "010: MODE3_PLC\n" +
                "011: MODE4_PLC\n" +
                "100: RESERVED\n" +
                "101: RESERVED\n" +
                "110: RESERVED\n" +
                "111: RESERVED\n");
        //System.out.println(actual2);

        assertTrue(expected2.equals(actual2));

        String expected3 = "0:UNAVAILABLE\n" +
                "1:MODE2_3_PWM\n" +
                "2:MODE3_PLC\n" +
                "3:MODE4_PLC\n" +
                "4:RESERVED\n" +
                "5:RESERVED_5\n" +
                "6:RESERVED_6\n" +
                "7:RESERVED_7";
        String actual3 = MessageTranslator.getStates("BMP", "000: UNAVAILABLE\n" +
                "001: MODE2_3_PWM\n" +
                "010: MODE3_PLC\n" +
                "011: MODE4_+PLC\n" +
                "100: RESERVED\n" +
                "101: RESERVED\n" +
                "110: RESERVED\n" +
                "111: RESERVED\n");

        String actualTest = MessageTranslator.getStates("BMP", "000: No fault \n" +
                "001: FirstLevelFault: Warning Lamp   \n" +
                "010: SecondLevelFault: Stop Lamp \n" +
                "011: ThirdLevelFault: Stop Lamp + contactor opening (EPS shutdown) \n" +
                "100: FourthLevelFault: Stop Lamp + Active Discharge \n" +
                "101: Inhibition of powertrain activation \n" +
                "110: Reserved \n" +
                "111: Invalid");
        System.out.println(actualTest);

        assertTrue(expected3.equals(actual3));

        String expected4 = "-";
        String actual4 = MessageTranslator.getStates("UMP", "this means nothing");

        assertTrue(expected4.equals(actual4));

        String expected5 = "-";
        String actual5 = MessageTranslator.getStates("BMP", "Please refer to reference specification.");

        assertTrue(expected5.equals(actual5));

    }

    @Test
    public void isIgnoredContent() {

        assertTrue(MessageTranslator.isIgnoredContent("Non applicable"));
        assertTrue(MessageTranslator.isIgnoredContent("  Non applicable   "));
        assertTrue(MessageTranslator.isIgnoredContent(null));
        assertTrue(MessageTranslator.isIgnoredContent(""));
        assertTrue(!MessageTranslator.isIgnoredContent("Ext"));
    }

    @Test
    public void getFrameId() {

        String expected1 = "398";
        String actual1 = MessageTranslator.getFrameId("CHKSUM_TRAME_4B_398");

        String expected2 = "4F8";
        String actual2 = MessageTranslator.getFrameId("CPT_PROCESS_4B_4F8");

        //System.out.println(actual2);

        assertTrue(expected1.equals(actual1));
        assertTrue(expected2.equals(actual2));

    }

    @Test
    public void getByteNumber() {

        String expected1 = "8";
        String actual1 = MessageTranslator.getByteNumber("8.0");

        String expected2 = "16";
        String actual2 = MessageTranslator.getByteNumber("13");

        //System.out.println(actual2);

        assertTrue(expected1.equals(actual1));
        assertTrue(expected2.equals(actual2));

    }

    @Test
    public void getLabelAutosarNameForTx() {

        String expected1 = "stDftCod";
        String actual1 = MessageTranslator.getLabelAutosarNameForTx("VshTx_stDftCod");

        String expected2 = "bHvBattAlertLowSoc";
        String actual2 = MessageTranslator.getLabelAutosarNameForTx("CellEstim_bHvBattAlertLowSoc");

        System.out.println(actual2);

        assertTrue(expected1.equals(actual1));
        assertTrue(expected2.equals(actual2));

    }

    @Test
    public void getSwProducerNameFromLabelAutosar() {

        String expected1 = "VshTx";
        String actual1 = MessageTranslator.getSwProducerNameFromLabelAutosar("VshTx_stDftCod");

        String expected2 = "CellEstim";
        String actual2 = MessageTranslator.getSwProducerNameFromLabelAutosar("CellEstim_bHvBattAlertLowSoc");

        System.out.println(actual2);

        assertTrue(expected1.equals(actual1));
        assertTrue(expected2.equals(actual2));

    }


    @Test
    public void writeCSVReport() {

        String content = "a;\"b\nc\";e;" + "\""+ "000: UNAVAILABLE\n" +
                "001: MODE2_3_PWM\n" +
                "010: MODE3_PLC\n" +
                "011: MODE4_+PLC\n" +
                "100: RESERVED\n" +
                "101: RESERVED\n" +
                "110: RESERVED\n" +
                "111: RESERVED\";";
        String outputpath = "src/test/resources/test.csv";
        messInjectionWriter.writeCSVReport(content, outputpath);

        assertTrue(MessageTranslator.isIgnoredContent("Non applicable"));
        assertTrue(MessageTranslator.isIgnoredContent("  Non applicable   "));
        assertTrue(MessageTranslator.isIgnoredContent(null));
        assertTrue(MessageTranslator.isIgnoredContent(""));
        assertTrue(!MessageTranslator.isIgnoredContent("Ext"));


    }

    @Test
    public void messInjection() throws IOException, MessageFormaException {

        MessageAnalyze messageAnalyzer = new MessageAnalyze();
        //String inputArchitectureFile = "src/test/resources/input/01552_18_07240_TBMU_Architecture.xlsx";
        String inputArchitectureFile = "c:/user/U546345/GitProject/tbmu-umain-architecture/tbmu-umain-architecture/2_Architecture/01552_18_07240_TBMU_Architecture.xlsx";
        //String inputMessageFile = "src/test/resources/input/01562_19_00960_v6_1_Messagerie_CAN_Enveloppe_BMS_VC1R3.xlsx";
        String inputMessageFile = "c:/user/U546345/CanProject/input/step2_can_database/01562_19_00960_v8_0_Messagerie_CAN_Enveloppe_BMS.xlsx";
        //String outputPath = "src/test/resources/output";
        String outputPath = "c:/user/U546345/Documents/Project/BMS/Livraison/V43/CAN/FluxAnalysis";
        messageAnalyzer.messInjection(inputArchitectureFile, inputMessageFile, outputPath);



        String expected1 = "398";
        String actual1 = MessageTranslator.getFrameId("CHKSUM_TRAME_4B_398");

        String expected2 = "4F8";
        String actual2 = MessageTranslator.getFrameId("CPT_PROCESS_4B_4F8");

        //System.out.println(actual2);
        System.out.println("Analysis complete");
        assertTrue(expected1.equals(actual1));
        assertTrue(expected2.equals(actual2));

    }

}