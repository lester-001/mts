package com.devoteam.srit.xmlloader.core.operations.basic.operators;

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
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
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
import java.util.Collections;
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

import threegpp.milenage.Milenage;
import threegpp.milenage.MilenageBufferFactory;
import threegpp.milenage.MilenageResult;
import threegpp.milenage.biginteger.BigIntegerBuffer;
import threegpp.milenage.biginteger.BigIntegerBufferFactory;
import threegpp.milenage.cipher.Ciphers;

import static com.ericsson.mts.nas.writer.XMLFormatWriter.bytesToHex;


public class PluggableParameterOperatorMilenage extends AbstractPluggableParameterOperator {
    
	private static String OPERATION_TYPE = "Milenage";
	
    public PluggableParameterOperatorMilenage()
    {
        this.addPluggableName(new PluggableName(OPERATION_TYPE));
        this.addPluggableName(new PluggableName("protocol." + OPERATION_TYPE));
    }

    
    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
        Parameter value = operands.get("value");
        Parameter value2 = operands.get("value2");
        Parameter value3 = operands.get("value3");
        Parameter value4 = operands.get("value4");
        Parameter value5 = operands.get("value5");
        Parameter value6 = operands.get("value6");
        Parameter presult = new Parameter();

        try
        {
            for(int i=0; i<value.length(); i++)
            {
                String key = value.get(i).toString();
                String opBytes = value2.get(i).toString();
                MilenageBufferFactory<BigIntegerBuffer> bufferFactory = BigIntegerBufferFactory.getInstance();
                Cipher cipher = Ciphers.createRijndaelCipher(DatatypeConverter.parseHexBinary(key));
                //byte [] OPc = Milenage.calculateOPc(DatatypeConverter.parseHexBinary(opBytes), cipher, bufferFactory);
                byte [] OPc = DatatypeConverter.parseHexBinary(opBytes);

                byte [] rand = DatatypeConverter.parseHexBinary(value3.get(i).toString());
                byte [] sqn = DatatypeConverter.parseHexBinary(value4.get(i).toString());
                byte [] amf = DatatypeConverter.parseHexBinary(value5.get(i).toString());
                
                // 4. Create the Milenage instance for `OPc` and Cipher instances with MilenageBufferFactory.                
                Milenage<BigIntegerBuffer> milenage = new Milenage<>(OPc, cipher, bufferFactory);
                final Map<MilenageResult, byte []> result = milenage.calculateAll(rand, sqn, amf, Executors.newCachedThreadPool());

                String ck = new String(bytesToHex(result.get(MilenageResult.CK)));
                String ik = new String(bytesToHex(result.get(MilenageResult.IK)));
                String ak = new String(bytesToHex(result.get(MilenageResult.AK)));
                String mac = new String(bytesToHex(result.get(MilenageResult.MAC_A)));
                String res = new String(bytesToHex(result.get(MilenageResult.RES)));
                //String ckik = ck + ik;

                //String sqnxorak = new String(bytesToHex(xor(result.get(MilenageResult.AK), sqn)));

                //System.out.println("--------------encode IN String format is----: " + ckik + " " + sqnxorak);
                presult.add(ck);
                presult.add(ik);
                presult.add(ak);
                presult.add(mac);
                presult.add(res);
            }
            
        }
        catch (Exception e)
        {
            throw new ParameterException("Error in Milenage operator", e);
        }

        return presult;
    }
    
    public static byte[] xor(byte[] b1, byte[] b2) {
        if (b1.length != b2.length) {
            throw new RuntimeException("Array sizes differ");
        }
        byte[] ret = new byte[b1.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (byte) (b1[i] ^ b2[i]);
        }
        return ret;
    }


    class SqnManager {
        long m_indBitLen;
        long m_wrappingDelta;
        Vector<Long> m_sqnArr;

        public SqnManager(long indBitLen, long wrappingDelta) {
            m_indBitLen = indBitLen;
            m_wrappingDelta = wrappingDelta;
            m_sqnArr = new Vector<>(1 << indBitLen, 1);
        }

        public long getSeqFromSqn(long sqn) {
            sqn &= ~((1 << m_indBitLen) - 1);
            sqn >>= m_indBitLen;
            sqn &= (1 << 48) - 1;
            return sqn;
        }
        public long getIndFromSqn(long sqn) {  
            return sqn & ((1 << m_indBitLen) - 1);
        }

        public long getSeqMs() {
            return getSeqFromSqn(getSqnMs());
        }
        
        public long getSqnMs() {
            long result = 0;
            
            try {
                result = Collections.max(m_sqnArr);
            } catch (Exception e) {
                
            }
            return result;
        }

        public boolean checkSqn(long sqn) {
            long seq = getSeqFromSqn(sqn);
            int ind = (int)getIndFromSqn(sqn);
            if (seq - getSeqMs() > m_wrappingDelta)
                return false;
            if (seq <= getSeqFromSqn(m_sqnArr.get(ind)))
                return false;
        
            m_sqnArr.set(ind, sqn);
            return true;
        }

        public long getSqn() {
            return getSqnMs();
        }
    }
}
