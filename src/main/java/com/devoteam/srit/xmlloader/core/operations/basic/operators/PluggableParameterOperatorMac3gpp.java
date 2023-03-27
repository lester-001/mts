package com.devoteam.srit.xmlloader.core.operations.basic.operators;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;

import java.util.Map;
import java.util.Vector;
import java.util.Collections;
import static com.ericsson.mts.nas.writer.XMLFormatWriter.bytesToHex;

public class PluggableParameterOperatorMac3gpp extends AbstractPluggableParameterOperator {
    
	private static String OPERATION_TYPE = "Mac3gpp";
	
    public PluggableParameterOperatorMac3gpp()
    {
        this.addPluggableName(new PluggableName(OPERATION_TYPE));
        this.addPluggableName(new PluggableName("protocol." + OPERATION_TYPE));
    }

    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
        Parameter value = operands.get("value");
        Parameter presult = new Parameter();

        try
        {
            presult.add("11"); 
        }
        catch (Exception e)
        {
            throw new ParameterException("Error in SQN operator", e);
        }

        return presult;
    }
}
