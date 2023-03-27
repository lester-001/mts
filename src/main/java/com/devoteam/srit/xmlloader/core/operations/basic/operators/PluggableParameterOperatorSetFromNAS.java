package com.devoteam.srit.xmlloader.core.operations.basic.operators;

import com.ailink.jni.CryptUtil;
import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.ngap.StackNgap;
import com.ericsson.mts.nas.BitInputStream;
import com.ericsson.mts.nas.reader.XMLFormatReader;
import com.ericsson.mts.nas.writer.XMLFormatWriter;

import java.util.Map;
import java.util.Vector;
import java.nio.ByteBuffer;

import javax.sdp.BandWidth;
import javax.sdp.Connection;
import javax.sdp.Key;
import javax.sdp.Media;
import javax.sdp.MediaDescription;
import javax.sdp.Origin;
import javax.sdp.RepeatTime;
import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;
import javax.sdp.TimeDescription;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;
import java.io.ByteArrayInputStream;

import javax.xml.bind.DatatypeConverter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import static com.ericsson.mts.nas.writer.XMLFormatWriter.bytesToHex;


public class PluggableParameterOperatorSetFromNAS extends AbstractPluggableParameterOperator {
    
	private static String OPERATION_TYPE = "setFromNAS";
	
    public PluggableParameterOperatorSetFromNAS()
    {
        this.addPluggableName(new PluggableName(OPERATION_TYPE));
        this.addPluggableName(new PluggableName("protocol." + OPERATION_TYPE));
    }

    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
        Parameter contents = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
        Parameter operate_type = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
        Parameter result = new Parameter();
        
        try
        {
            StackNgap ngap = new StackNgap();

            for(int j=0; j<operate_type.length(); j++)
            {
                String type = operate_type.get(j).toString();
                if (type.equals("decode")) {
                    for(int i=0; i<contents.length(); i++)
                    {
                        String nashex = contents.get(i).toString();
                        
                        XMLFormatWriter formatWriter = new XMLFormatWriter();        
                        BitInputStream bitInputStream = new BitInputStream(new ByteArrayInputStream(DatatypeConverter.parseHexBinary(nashex)));
                        ngap.getNASTranslator().decode(ngap.getRegistry5GSNasTranslator(),bitInputStream, formatWriter);
        
                        Element xml = formatWriter.getResultElement();
                        TransformerFactory tf = TransformerFactory.newInstance();
                        Transformer trans = tf.newTransformer();
                        StringWriter sw = new StringWriter();
                        trans.transform(new DOMSource(xml), new StreamResult(sw));
                        result.add(sw.toString());
                    }

                } else if (type.equals("encode")) {
                    for(int i=0; i<contents.length(); i++)
                    {
                        String nasxml = contents.get(i).toString();
                        InputStream InputStream = new ByteArrayInputStream((nasxml.getBytes()));
                        
                        XMLFormatReader formatReader = new XMLFormatReader(InputStream, "");
                        byte[] data1 = ngap.getNASTranslator().encode(ngap.getRegistry5GSNasTranslator(), formatReader);
                        String encoded = new String(bytesToHex(data1));
                        result.add(encoded);
                    }

                } else if (type.equals("IA1")) {
                    Parameter para_key = PluggableParameterOperatorList.assertAndGetParameter(operands, "value3");
                    //Parameter para_data = PluggableParameterOperatorList.assertAndGetParameter(operands, "value4");
                    Parameter para_count = PluggableParameterOperatorList.assertAndGetParameter(operands, "value4");
                    Parameter para_dir = PluggableParameterOperatorList.assertAndGetParameter(operands, "value5");

                    if(contents.length() != 0)
                    {
                        byte[] data = contents.get(0).toString().getBytes();
                        byte[] key = para_key.get(0).toString().getBytes();
                        int count = Integer.parseInt(para_count.get(0).toString());
                        int dir = Integer.parseInt(para_dir.get(0).toString());

                        int ret = CryptUtil.ComputeMacEia2(key, count, 1, dir, data);
                        byte[] bytes = ByteBuffer.allocate(4).putInt(ret).array();

                        result.add(bytesToHex(bytes));
                        result.add(bytesToHex(data));
                    }
                } else if (type.equals("IA2")) {
                    Parameter para_key = PluggableParameterOperatorList.assertAndGetParameter(operands, "value3");
                    //Parameter para_data = PluggableParameterOperatorList.assertAndGetParameter(operands, "value4");
                    Parameter para_count = PluggableParameterOperatorList.assertAndGetParameter(operands, "value4");
                    Parameter para_dir = PluggableParameterOperatorList.assertAndGetParameter(operands, "value5");
                    //System.out.println("-------" + para_dir.get(0).toString() + "2222222222");
                    if(contents.length() != 0)
                    {
                        byte[] data = contents.get(0).toString().getBytes();
                        byte[] key = para_key.get(0).toString().getBytes();
                        int count = Integer.parseInt(para_count.get(0).toString());
                        int dir = Integer.parseInt(para_dir.get(0).toString());

                        int ret = CryptUtil.ComputeMacEia2(key, count, 1, dir, data);
                        byte[] bytes = ByteBuffer.allocate(4).putInt(ret).array();

                        result.add(bytesToHex(bytes));
                        result.add(bytesToHex(data));
                    }
                } else if (type.equals("IA3")) {
                    Parameter para_key = PluggableParameterOperatorList.assertAndGetParameter(operands, "value3");
                    //Parameter para_data = PluggableParameterOperatorList.assertAndGetParameter(operands, "value4");
                    Parameter para_count = PluggableParameterOperatorList.assertAndGetParameter(operands, "value4");
                    Parameter para_dir = PluggableParameterOperatorList.assertAndGetParameter(operands, "value5");

                    if(contents.length() != 0)
                    {
                        byte[] data = contents.get(0).toString().getBytes();
                        byte[] key = para_key.get(0).toString().getBytes();
                        int count = Integer.parseInt(para_count.get(0).toString());
                        int dir = Integer.parseInt(para_dir.get(0).toString());

                        int ret = CryptUtil.ComputeMacEia3(key, count, 1, dir, data);
                        byte[] bytes = ByteBuffer.allocate(4).putInt(ret).array();

                        result.add(bytesToHex(bytes));
                        result.add(bytesToHex(data));
                    }
                } else if (type.equals("SeafAmf")) {
                    //Parameter para_data = PluggableParameterOperatorList.assertAndGetParameter(operands, "value4");
                    Parameter snn = PluggableParameterOperatorList.assertAndGetParameter(operands, "value3");
                    Parameter abba = PluggableParameterOperatorList.assertAndGetParameter(operands, "value4");
                    Parameter ausf = PluggableParameterOperatorList.assertAndGetParameter(operands, "value5");

                    if(contents.length() != 0)
                    {
                        byte[] supi = contents.get(0).toString().getBytes();
                        byte[] bsnn = snn.get(0).toString().getBytes();
                        byte[] babba = abba.get(0).toString().getBytes();
                        byte[] bausf = ausf.get(0).toString().getBytes();
                        byte[] kseaf = new byte[32];
                        byte[] kamf = new byte[32];

                        CryptUtil.DeriveKeysSeafAmf(bausf, supi, bsnn, babba, kseaf, kamf);
                        result.add(bytesToHex(kseaf));
                        result.add(bytesToHex(kamf));
                    }
                } else if (type.equals("NasKey")) {
                    //Parameter para_data = PluggableParameterOperatorList.assertAndGetParameter(operands, "value4");
                    Parameter para_ciphering = PluggableParameterOperatorList.assertAndGetParameter(operands, "value3");
                    Parameter para_integrity = PluggableParameterOperatorList.assertAndGetParameter(operands, "value4");

                    if(contents.length() != 0)
                    {
                        byte[] kamf = contents.get(0).toString().getBytes();
                        byte[] kNasEnc = new byte[16];
                        byte[] kNasInt = new byte[16];
                        int ciphering = Integer.parseInt(para_ciphering.get(0).toString());
                        int integrity = Integer.parseInt(para_integrity.get(0).toString());

                        CryptUtil.DeriveNasKeys(kamf, kNasEnc, kNasInt, ciphering, integrity);
                        result.add(bytesToHex(kNasEnc));
                        result.add(bytesToHex(kNasInt));
                    }
                }
            }
            
        }
        catch (Exception e)
        {
            throw new ParameterException("Error in setFromNAS operator", e);
        }

        return result;
    }
}
