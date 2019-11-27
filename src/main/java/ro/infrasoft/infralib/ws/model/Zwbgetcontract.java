package ro.infrasoft.infralib.ws.model;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Contract" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "contract"
})
@XmlRootElement(name = "Zwbgetcontract")
public class Zwbgetcontract {

    @XmlElement(name = "Contract", required = true)
    protected String contract;

    /**
     * Gets the value of the contract property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getContract() {
        return contract;
    }

    /**
     * Sets the value of the contract property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setContract(String value) {
        this.contract = value;
    }

}
