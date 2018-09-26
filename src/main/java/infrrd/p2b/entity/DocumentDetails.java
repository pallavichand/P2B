package infrrd.p2b.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DocumentDetails {
	private	String amount;
	private	String checkNumber;
	private	String payor;
	private	String payee;
	private	String billDate;
	private	List<InvoiceDetails> invoiceDetails;
}
