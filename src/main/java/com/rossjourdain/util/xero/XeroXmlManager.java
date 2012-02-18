package com.rossjourdain.util.xero;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import net.oauth.OAuthProblemException;

/**
 *
 * @author ross
 */
public class XeroXmlManager {
    
    public static ArrayOfInvoice xmlToInvoices(InputStream invoiceStream) {

        ArrayOfInvoice arrayOfInvoices = null;
        
        try {
            JAXBContext context = JAXBContext.newInstance(ResponseType.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            JAXBElement<ResponseType> element = unmarshaller.unmarshal(new StreamSource(invoiceStream), ResponseType.class);
            ResponseType response = element.getValue();
            arrayOfInvoices = response.getInvoices();

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        return arrayOfInvoices;
    }

    public static ResponseType xmlToResponse(InputStream responseStream) {

        ResponseType response = null;

        try {
            JAXBContext context = JAXBContext.newInstance(ResponseType.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            JAXBElement<ResponseType> element = unmarshaller.unmarshal(new StreamSource(responseStream), ResponseType.class);
            response = element.getValue();

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        return response;
    }
    
    public static ApiException xmlToException(String exceptionString) {

        ApiException apiException = null;

        try {
            JAXBContext context = JAXBContext.newInstance(ApiException.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            JAXBElement<ApiException> element = unmarshaller.unmarshal(new StreamSource(new StringReader(exceptionString)), ApiException.class);
            apiException = element.getValue();

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        return apiException;
    }

    public static String oAuthProblemExceptionToXml(OAuthProblemException authProblemException) {
        
        String oAuthProblemExceptionString = null;
        
        Map<String, Object> params = authProblemException.getParameters();
        for (String key : params.keySet()) {
            Object o = params.get(key);
            if (key.contains("ApiException")) {
                oAuthProblemExceptionString = key + "=" + o.toString();
            }
        }
        
        return oAuthProblemExceptionString;
    }

    public static String contactsToXml(ArrayOfContact arrayOfContacts) {

        String contactsString = null;

        try {

            JAXBContext context = JAXBContext.newInstance(ResponseType.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);

            ObjectFactory factory = new ObjectFactory();
            JAXBElement<ArrayOfContact> element = factory.createContacts(arrayOfContacts);

            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(element, stringWriter);
            contactsString = stringWriter.toString();

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        return contactsString;
    }

    public static String invoicesToXml(ArrayOfInvoice arrayOfInvoices) {

        String invoicesString = null;

        try {

            JAXBContext context = JAXBContext.newInstance(ResponseType.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);

            ObjectFactory factory = new ObjectFactory();
            JAXBElement<ArrayOfInvoice> element = factory.createInvoices(arrayOfInvoices);

            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(element, stringWriter);
            invoicesString = stringWriter.toString();

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        return invoicesString;
    }
    
    public static String paymentsToXml(ArrayOfPayment arrayOfPayment) {

        String paymentsString = null;

        try {

            JAXBContext context = JAXBContext.newInstance(ResponseType.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);

            ObjectFactory factory = new ObjectFactory();
            JAXBElement<ArrayOfPayment> element = factory.createPayments(arrayOfPayment);

            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(element, stringWriter);
            paymentsString = stringWriter.toString();

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        return paymentsString;
    }
    
}
