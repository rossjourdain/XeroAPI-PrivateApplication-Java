
/*
 *  Copyright 2011 Ross Jourdain
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.rossjourdain.util.xero;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import net.oauth.OAuthProblemException;

/**
 *
 * @author ross
 */
public class XeroClientException extends Exception {
    
    private ApiException apiException;
    
    public XeroClientException(String message, OAuthProblemException oAuthProblemException) {

        super(message, oAuthProblemException);
        
        String oAuthProblemExceptionString = null;
        Map<String, Object> params = oAuthProblemException.getParameters();
        for (String key : params.keySet()) {
            if (key.contains("ApiException")) {
                Object o = params.get(key);
                oAuthProblemExceptionString = key + "=" + o.toString();
            }
        }
        
        try {
            JAXBContext context = JAXBContext.newInstance(ApiException.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            JAXBElement<ApiException> element = unmarshaller.unmarshal(new StreamSource(new StringReader(oAuthProblemExceptionString)), ApiException.class);
            apiException = element.getValue();
        } catch (JAXBException ex) {
            ex.printStackTrace();
        }
    }

    public ApiException getApiException() {
        return apiException;
    }
    
    public void printDetails() {

        System.out.println("");
        System.out.println(this.getMessage());
        System.out.println("Message: " + apiException.getMessage());
        System.out.println("Error " + apiException.getErrorNumber() + ": " + apiException.getType());
        if (apiException.getElements() != null && apiException.getElements().getDataContractBase() != null) {
            List<DataContractBase> dataContractBases = apiException.getElements().getDataContractBase();
            for (DataContractBase dataContractBase : dataContractBases) {

                System.out.println("DataType: " + dataContractBase.getClass().getSimpleName());
                //if(dataContractBase instanceof Invoice) {
                //    System.out.println("Invoice Number: " + ((Invoice)dataContractBase).getInvoiceNumber());
                //} else if(dataContractBase instanceof Invoice) {
                //    System.out.println("Payment ID: " + ((Payment)dataContractBase).getPaymentID());
                //}

                if (dataContractBase.getWarnings() != null && dataContractBase.getWarnings().getWarning() != null) {
                    List<Warning> warnings = dataContractBase.getWarnings().getWarning();
                    for (int i = 0; i < warnings.size(); i++) {
                        Warning warning = warnings.get(i);
                        System.out.println("Warning " + (i + 1) + ": " + warning.getMessage());
                    }
                }
                if (dataContractBase.getValidationErrors() != null && dataContractBase.getValidationErrors().getValidationError() != null) {
                    List<ValidationError> validationErrors = dataContractBase.getValidationErrors().getValidationError();
                    for (int i = 0; i < validationErrors.size(); i++) {
                        ValidationError validationError = validationErrors.get(i);
                        System.out.println("Validation Error " + (i + 1) + ": " + validationError.getMessage());
                    }
                }
            }
        }
        System.out.println("");
        /* Add this back in if you need more details on the exception */
        //System.out.println("" + apiExceptionString);
        //System.out.println("");
    }
    
}
