package com.devoteam.srit.xmlloader.ngap;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.s1ap.MsgS1ap;
import com.ericsson.mts.asn1.ASN1Translator;
import com.ericsson.mts.nas.BitInputStream;
import com.ericsson.mts.nas.message.AbstractMessage;
import com.ericsson.mts.nas.registry.Registry;

import com.ericsson.mts.nas.writer.XMLFormatWriter;
import com.ericsson.mts.nas.reader.XMLFormatReader;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.xml.bind.DatatypeConverter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import static com.ericsson.mts.nas.writer.XMLFormatWriter.bytesToHex;

public class MsgNgap extends MsgS1ap {

    public MsgNgap(Stack stack) throws IOException {
        super(stack);
    }

    @Override
    public String getXmlRootNodeName() {
        return "NGAP-PDU";
    }

    public ASN1Translator getASN1Translator() {
        return ((StackNgap) this.stack).getAsn1Translator();
    }

    protected AbstractMessage getNASTranslator() throws IOException { return ((StackNgap) this.stack).getNASTranslator(); }

    protected Registry getRegistryNas() throws IOException { return ((StackNgap) this.stack).getRegistry5GSNasTranslator(); }


    protected int getPpid() {
        return 60;
    }

    @Override
    public byte[] encode() throws Exception {   
        return super.encode();
    }

    @Override
    public void decode(byte[] data) throws Exception {
        super.decode(data);

        //Decode the NAS part of the binary message
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        XPathExpression expr = xpath.compile("//NAS-PDU");
        NodeList nodes = (NodeList) expr.evaluate(element, XPathConstants.NODESET);

        if(nodes.getLength() > 0) {
            for(int i = 0; i < nodes.getLength(); i++){
                XMLFormatWriter formatWriter = new XMLFormatWriter();
                Element nasPDUElement = (Element) nodes.item(i);

                BitInputStream bitInputStream = new BitInputStream(new ByteArrayInputStream(DatatypeConverter.parseHexBinary(nasPDUElement.getTextContent())));
                getNASTranslator().decode(getRegistryNas(),bitInputStream, formatWriter);

                Element xml = formatWriter.getResultElement();
                //delete the existing child of the node
                while (nasPDUElement.hasChildNodes()){
                    nasPDUElement.removeChild(nasPDUElement.getFirstChild());
                }
                //replace the current node by the new Element
                nasPDUElement.appendChild(nasPDUElement.getOwnerDocument().adoptNode(xml));
            }
        }
 
        /* 
 {
            String nasxml= "<L3MessageWrapper><ExtendedProtocolDiscriminator><ExtendedProtocolDiscriminator><ExtendedProtocolDiscriminator>A5gsMobilityManagementMessages</ExtendedProtocolDiscriminator></ExtendedProtocolDiscriminator></ExtendedProtocolDiscriminator><SpareHalfOctet><SpareHalfOctet/></SpareHalfOctet><SecurityHeaderType><SecurityHeaderType><SecurityHeaderType>Plain5gsNasMessageNotSecurityProtected</SecurityHeaderType><Plain5gsNasMessageNotSecurityProtected><MessageType><MessageType>IdentityRequest</MessageType><IdentityRequest><SpareHalfOctet><SpareHalfOctet/></SpareHalfOctet><A5gsIdentityType><A5gsIdentityType><TypeOfIdentity>1</TypeOfIdentity></A5gsIdentityType></A5gsIdentityType></IdentityRequest></MessageType></Plain5gsNasMessageNotSecurityProtected></SecurityHeaderType></SecurityHeaderType></L3MessageWrapper>";
            System.out.println("--------------encode IN String format is----: " + nasxml);
            InputStream InputStream = new ByteArrayInputStream((nasxml.getBytes()));
            
            XMLFormatReader formatReader = new XMLFormatReader(InputStream, "");
            byte[] data1 = getNASTranslator().encode(getRegistryNas(), formatReader);
            System.out.println("--------------encode IN String format is: " + new String(data1));
        }

        {
            String nas = "7e004179000bf264f086020040fb008d0e2e02f070";
            //String nas = "7E005B01";
            XMLFormatWriter formatWriter = new XMLFormatWriter();

            BitInputStream bitInputStream = new BitInputStream(new ByteArrayInputStream(DatatypeConverter.parseHexBinary(nas)));
            getNASTranslator().decode(getRegistryNas(),bitInputStream, formatWriter);
            Element xml = formatWriter.getResultElement();
            
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            StringWriter sw = new StringWriter();
            trans.transform(new DOMSource(xml), new StreamResult(sw));
            System.out.println("--------------XML IN String format is: \n" + sw.toString());

            String nasxml = sw.toString();
            
            InputStream InputStream = new ByteArrayInputStream((nasxml.getBytes()));
            
            XMLFormatReader formatReader = new XMLFormatReader(InputStream, "");
            byte[] data1 = getNASTranslator().encode(getRegistryNas(), formatReader);
            System.out.println("--------------encode IN String format is: " + new String(data1));
        } */
    }

    @Override
    public void parseFromXml(ParseFromXmlContext context, org.dom4j.Element root, Runner runner) throws Exception {
        super.parseFromXml(context, root, runner);
    }
}
