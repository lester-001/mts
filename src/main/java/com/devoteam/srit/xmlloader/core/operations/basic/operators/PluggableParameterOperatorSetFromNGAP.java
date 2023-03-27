package com.devoteam.srit.xmlloader.core.operations.basic.operators;

import com.ailink.jni.CryptUtil;
import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.ngap.StackNgap;
import com.ericsson.mts.asn1.BitArray;
import com.ericsson.mts.asn1.XMLFormatReader;
import com.ericsson.mts.asn1.XMLFormatWriter;
import com.ericsson.mts.asn1.factory.FormatReader;
import com.ericsson.mts.asn1.factory.FormatWriter;
import com.ericsson.mts.nas.BitInputStream;
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


public class PluggableParameterOperatorSetFromNGAP extends AbstractPluggableParameterOperator {
    
	private static String OPERATION_TYPE = "setFromNGAP";
	
    public PluggableParameterOperatorSetFromNGAP()
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
                        ngap.getAsn1Translator().decode("NGAP-PDU",bitInputStream, (FormatWriter) formatWriter);
        
                        Element xml = formatWriter.getResult();
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
                        
                        XMLFormatReader formatReader = new XMLFormatReader(InputStream, "NGAP-PDU");
                        BitArray bitArray = new BitArray();
                        
                        ngap.getAsn1Translator().encode("NGAP-PDU", bitArray, (FormatReader)formatReader);
                        byte[] data1 = bitArray.getBinaryArray();
                        String encoded = new String(bytesToHex(data1));
                        result.add(encoded);
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
