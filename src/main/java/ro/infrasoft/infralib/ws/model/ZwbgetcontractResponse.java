package ro.infrasoft.infralib.ws.model;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


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
 *         &lt;element name="AnRecolta" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CondTransport" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ContractCp" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="DataContract" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="DataContractCp" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="Error" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Field1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Filed2" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="LocDescarcare" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="LocIncarcare" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Material" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Paritate" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Partener" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PerioadaDeLa" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="PerioadaLa" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="TipContract" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
        "anRecolta",
        "condTransport",
        "contractCp",
        "dataContract",
        "dataContractCp",
        "error",
        "field1",
        "filed2",
        "locDescarcare",
        "locIncarcare",
        "material",
        "paritate",
        "partener",
        "perioadaDeLa",
        "perioadaLa",
        "tipContract"
})
@XmlRootElement(name = "ZwbgetcontractResponse")
public class ZwbgetcontractResponse {

    @XmlElement(name = "AnRecolta", required = true)
    protected String anRecolta;
    @XmlElement(name = "CondTransport", required = true)
    protected String condTransport;
    @XmlElement(name = "ContractCp", required = true)
    protected String contractCp;
    @XmlElement(name = "DataContract", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar dataContract;
    @XmlElement(name = "DataContractCp", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar dataContractCp;
    @XmlElement(name = "Error", required = true)
    protected String error;
    @XmlElement(name = "Field1", required = true)
    protected String field1;
    @XmlElement(name = "Filed2", required = true)
    protected String filed2;
    @XmlElement(name = "LocDescarcare", required = true)
    protected String locDescarcare;
    @XmlElement(name = "LocIncarcare", required = true)
    protected String locIncarcare;
    @XmlElement(name = "Material", required = true)
    protected String material;
    @XmlElement(name = "Paritate", required = true)
    protected String paritate;
    @XmlElement(name = "Partener", required = true)
    protected String partener;
    @XmlElement(name = "PerioadaDeLa", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar perioadaDeLa;
    @XmlElement(name = "PerioadaLa", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar perioadaLa;
    @XmlElement(name = "TipContract", required = true)
    protected String tipContract;

    /**
     * Gets the value of the anRecolta property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAnRecolta() {
        return anRecolta;
    }

    /**
     * Sets the value of the anRecolta property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAnRecolta(String value) {
        this.anRecolta = value;
    }

    /**
     * Gets the value of the condTransport property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCondTransport() {
        return condTransport;
    }

    /**
     * Sets the value of the condTransport property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCondTransport(String value) {
        this.condTransport = value;
    }

    /**
     * Gets the value of the contractCp property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getContractCp() {
        return contractCp;
    }

    /**
     * Sets the value of the contractCp property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setContractCp(String value) {
        this.contractCp = value;
    }

    /**
     * Gets the value of the dataContract property.
     *
     * @return
     *     possible object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getDataContract() {
        return dataContract;
    }

    /**
     * Sets the value of the dataContract property.
     *
     * @param value
     *     allowed object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *
     */
    public void setDataContract(XMLGregorianCalendar value) {
        this.dataContract = value;
    }

    /**
     * Gets the value of the dataContractCp property.
     *
     * @return
     *     possible object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getDataContractCp() {
        return dataContractCp;
    }

    /**
     * Sets the value of the dataContractCp property.
     *
     * @param value
     *     allowed object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *
     */
    public void setDataContractCp(XMLGregorianCalendar value) {
        this.dataContractCp = value;
    }

    /**
     * Gets the value of the error property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getError() {
        return error;
    }

    /**
     * Sets the value of the error property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setError(String value) {
        this.error = value;
    }

    /**
     * Gets the value of the field1 property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getField1() {
        return field1;
    }

    /**
     * Sets the value of the field1 property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setField1(String value) {
        this.field1 = value;
    }

    /**
     * Gets the value of the filed2 property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFiled2() {
        return filed2;
    }

    /**
     * Sets the value of the filed2 property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFiled2(String value) {
        this.filed2 = value;
    }

    /**
     * Gets the value of the locDescarcare property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getLocDescarcare() {
        return locDescarcare;
    }

    /**
     * Sets the value of the locDescarcare property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setLocDescarcare(String value) {
        this.locDescarcare = value;
    }

    /**
     * Gets the value of the locIncarcare property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getLocIncarcare() {
        return locIncarcare;
    }

    /**
     * Sets the value of the locIncarcare property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setLocIncarcare(String value) {
        this.locIncarcare = value;
    }

    /**
     * Gets the value of the material property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMaterial() {
        return material;
    }

    /**
     * Sets the value of the material property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMaterial(String value) {
        this.material = value;
    }

    /**
     * Gets the value of the paritate property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getParitate() {
        return paritate;
    }

    /**
     * Sets the value of the paritate property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setParitate(String value) {
        this.paritate = value;
    }

    /**
     * Gets the value of the partener property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPartener() {
        return partener;
    }

    /**
     * Sets the value of the partener property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPartener(String value) {
        this.partener = value;
    }

    /**
     * Gets the value of the perioadaDeLa property.
     *
     * @return
     *     possible object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getPerioadaDeLa() {
        return perioadaDeLa;
    }

    /**
     * Sets the value of the perioadaDeLa property.
     *
     * @param value
     *     allowed object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *
     */
    public void setPerioadaDeLa(XMLGregorianCalendar value) {
        this.perioadaDeLa = value;
    }

    /**
     * Gets the value of the perioadaLa property.
     *
     * @return
     *     possible object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getPerioadaLa() {
        return perioadaLa;
    }

    /**
     * Sets the value of the perioadaLa property.
     *
     * @param value
     *     allowed object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *
     */
    public void setPerioadaLa(XMLGregorianCalendar value) {
        this.perioadaLa = value;
    }

    /**
     * Gets the value of the tipContract property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTipContract() {
        return tipContract;
    }

    /**
     * Sets the value of the tipContract property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTipContract(String value) {
        this.tipContract = value;
    }

}
