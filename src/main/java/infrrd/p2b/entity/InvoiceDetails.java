package infrrd.p2b.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InvoiceDetails {
@JsonProperty("Invoice Number")
String invoiceNumber;
@JsonProperty("Invoice Date")
String invoiceDate;
@JsonProperty("Net Amount")
String netAmount;
@JsonProperty("Discount Amount")
String discountAmount;
@JsonProperty("Gross Amount")
String grossAmount;

@Override
public String toString(){
    return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this);
}
}
