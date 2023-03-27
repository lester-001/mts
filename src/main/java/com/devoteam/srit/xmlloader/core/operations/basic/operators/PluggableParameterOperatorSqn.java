package com.devoteam.srit.xmlloader.core.operations.basic.operators;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;

import java.util.Map;
import java.util.Vector;
import java.util.Collections;
import static com.ericsson.mts.nas.writer.XMLFormatWriter.bytesToHex;


public class PluggableParameterOperatorSqn extends AbstractPluggableParameterOperator {
    
	private static String OPERATION_TYPE = "SQN";
	
    public PluggableParameterOperatorSqn()
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
        Parameter presult = new Parameter();

        try
        {
            long indBitLen = Long.parseLong(value.get(0).toString());
            long wrappingDelta = Long.parseLong(value2.get(0).toString());

            SqnManager sqn = new SqnManager(indBitLen, wrappingDelta, value3);

            if (value4.get(0).equals("checkSqn")) {
                long receivedSqn = Long.parseLong(value5.get(0).toString(), 16);
                sqn.checkSqn(receivedSqn, presult);
            } else if (value4.get(0).equals("getSqn")) {
                long l = sqn.getSqn();
                byte[] data = longToBytes(l);
                String data_sqn = new String(bytesToHex(data));
                presult.add(data_sqn);
            }            
        }
        catch (Exception e)
        {
            throw new ParameterException("Error in SQN operator", e);
        }

        return presult;
    }

    public static byte[] longToBytes(long l) {
        byte[] result = new byte[6];
        for (int i = 5; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= 8;
        }
        return result;
    }
    
    class SqnManager {
        long m_indBitLen;
        long m_wrappingDelta;
        Vector<Long> m_sqnArr;

        public SqnManager(long indBitLen, long wrappingDelta, Parameter param) {
            m_indBitLen = indBitLen;
            m_wrappingDelta = wrappingDelta;
            m_sqnArr = new Vector<>(1 << indBitLen, 1);
            for(int i=0; i<param.length(); i++)
            {
                try {
                    m_sqnArr.add(Long.parseLong(param.get(i).toString()));
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
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

        public boolean checkSqn(long sqn, Parameter presult) {
            long seq = getSeqFromSqn(sqn);
            int ind = (int)getIndFromSqn(sqn);
            if (seq - getSeqMs() > m_wrappingDelta)  {
                presult.add(0);
                return false;
            }
            if (seq <= getSeqFromSqn(m_sqnArr.get(ind))) {
                presult.add(0);
                return false;
            }
        
            presult.add(1);
            m_sqnArr.set(ind, sqn);
            
            for(int i=0; i<m_sqnArr.size(); i++)
            {
                presult.add(m_sqnArr.get(i));
            }
            return true;
        }

        public long getSqn() {
            return getSqnMs();
        }
    }
}
