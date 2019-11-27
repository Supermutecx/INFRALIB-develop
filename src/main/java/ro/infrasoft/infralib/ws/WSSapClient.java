package ro.infrasoft.infralib.ws;

import org.apache.log4j.Logger;
import ro.infrasoft.infralib.logger.LoggerUtil;
import ro.infrasoft.infralib.settings.Settings;
import ro.infrasoft.infralib.ws.model.ZWBGETCONTRACTPORT;
import ro.infrasoft.infralib.ws.model.ZwbgetcontractResponse;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;

/**
 * Created by Infraosoft07 on 5/22/2017.
 */
public class WSSapClient {

    private Logger logger = LoggerUtil.getLogger("pdfSplit");
    private String wsdlUrl = null;
    private String serviceNamespaceURI = null;
    private String serviceName = null;
    private String portNamespaceURI = null;
    private String portName = null;
    private String agricoverUsername = null;
    private String agricoverPassword = null;
    private URL serviceUrl = null;
    private Service service = null;
    private QName portQName = null;
    private ZWBGETCONTRACTPORT port = null;


    public WSSapClient() {
        try {
            logger.trace("CREATING WS CLIENT");
            wsdlUrl = Settings.get("agricoverSapConfig", "wsdlUrl");
            serviceNamespaceURI = Settings.get("agricoverSapConfig", "serviceNamespaceURI");
            serviceName = Settings.get("agricoverSapConfig", "serviceName");
            portNamespaceURI = Settings.get("agricoverSapConfig", "portNamespaceURI");
            portName = Settings.get("agricoverSapConfig", "portName");
            agricoverUsername = Settings.get("agricoverSapConfig", "agricoverUsername");
            agricoverPassword = Settings.get("agricoverSapConfig", "agricoverPassword");

            logger.trace("CREATING WS CLIENT - SETTINGS READ");

            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(agricoverUsername, agricoverPassword.toCharArray());
                }
            });


            serviceUrl = new URL(wsdlUrl);

            logger.trace("CREATING WS CLIENT - wsdlUrl initialized");

            service = Service.create(serviceUrl, new QName(serviceNamespaceURI, serviceName));

            logger.trace("CREATING WS CLIENT - service created");

            portQName = new QName(portNamespaceURI, portName);

            logger.trace("CREATING WS CLIENT - qname initialized");

            port = service.getPort(portQName, ZWBGETCONTRACTPORT.class);

            logger.trace("CREATING WS CLIENT - port read");

            ((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, agricoverUsername);
            ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, agricoverPassword);
            ((BindingProvider) port).getRequestContext().put("com.sun.xml.ws.connect.timeout", 1 * 60 * 1000);
            ((BindingProvider) port).getRequestContext().put("com.sun.xml.ws.request.timeout", 3 * 60 * 1000);

            logger.trace("CREATING WS CLIENT - request context initialized");

        } catch (Throwable th) {
            String message = th.getMessage();
            if (message == null){
                if (th.getCause() != null && th.getCause().getMessage() != null){
                    message = th.getCause().getMessage();
                } else {
                    message = th.toString();
                }
            }

            StringBuilder st = new StringBuilder();
            for (StackTraceElement ste: th.getStackTrace()){
                st.append(ste.toString()).append("\n");
            }

            logger.error(message);
            logger.error(st.toString());
        }
    }

    /**
     * Aduce metadatele returnate de metoda Zwbgetcontract pentru un cod de contract
     *
     * @param contract - codul de contract
     * @return - raspunsul impachetat intr-un obiect
     */
    public ZwbgetcontractResponse getMetadata(String contract) {
        final Holder<String> anRecolta = new Holder<>();
        final Holder<String> condTransport = new Holder<>();
        final Holder<String> contractCp = new Holder<>();
        final Holder<XMLGregorianCalendar> dataContract = new Holder<>();
        final Holder<XMLGregorianCalendar> dataContractCp = new Holder<>();
        final Holder<String> error = new Holder<>();
        final Holder<String> field1 = new Holder<>();
        final Holder<String> filed2 = new Holder<>();
        final Holder<String> locDescarcare = new Holder<>();
        final Holder<String> locIncarcare = new Holder<>();
        final Holder<String> material = new Holder<>();
        final Holder<String> paritate = new Holder<>();
        final Holder<String> partener = new Holder<>();
        final Holder<XMLGregorianCalendar> perioadaDeLa = new Holder<>();
        final Holder<XMLGregorianCalendar> perioadaLa = new Holder<>();
        final Holder<String> tipContract = new Holder<>();

        ZwbgetcontractResponse response = null;

        if (service != null) {
            port.zwbgetcontract(contract, anRecolta, condTransport, contractCp, dataContract, dataContractCp, error,
                    field1, filed2, locDescarcare, locIncarcare, material, paritate, partener, perioadaDeLa, perioadaLa,
                    tipContract);

            response = new ZwbgetcontractResponse();
            response.setAnRecolta(anRecolta.value);
            response.setCondTransport(condTransport.value);
            response.setContractCp(contractCp.value);
            response.setDataContract(dataContract.value);
            response.setDataContractCp(dataContractCp.value);
            response.setError(error.value);
            response.setField1(field1.value);
            response.setFiled2(filed2.value);
            response.setLocDescarcare(locDescarcare.value);
            response.setLocIncarcare(locIncarcare.value);
            response.setMaterial(material.value);
            response.setParitate(paritate.value);
            response.setPartener(partener.value);
            response.setPerioadaDeLa(perioadaDeLa.value);
            response.setPerioadaLa(perioadaLa.value);
            response.setTipContract(tipContract.value);

            System.out.println("Output:");
            System.out.println("Contract:" + contract);
            System.out.println("Cond transport:" + getValue(condTransport));
            System.out.println("Contract CP:" + getValue(contractCp));
            System.out.println("Data contract:" + getValue(dataContract));
            System.out.println("Data contract CP:" + getValue(dataContractCp));
            System.out.println("Eroare:" + getValue(error));
            System.out.println("Field 1:" + getValue(field1));
            System.out.println("Field 2:" + getValue(filed2));
            System.out.println("Loc descarcare:" + getValue(locDescarcare));
            System.out.println("Loc incarcare:" + getValue(locIncarcare));
            System.out.println("Material:" + getValue(material));
            System.out.println("Paritate:" + getValue(paritate));
            System.out.println("Partener:" + getValue(partener));
            System.out.println("Perioada de la:" + getValue(perioadaDeLa));
            System.out.println("Perioada la:" + getValue(perioadaLa));
            System.out.println("Tip contract:" + getValue(tipContract));
        }

        return response;
    }

    private String getValue(Holder obj) {
        return obj.value != null ? obj.value.toString() : "";
    }


}
