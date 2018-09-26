package infrrd.p2b.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import infrrd.p2b.entity.DocumentDetails;
import infrrd.p2b.entity.InvoiceDetails;
import infrrd.p2b.entity.RemittenceHeader;

public class InvoiceDetailsListExtractor implements DocumentDetailsExtractor{
	String amountExtractionRegex = "(?<!\\d)(\\d{1,3}[,]?){1,3}\\d{1,3}[.]\\d{1,2}(?!\\d)";
	@Override
	public DocumentDetails extractRem (String ocrText, DocumentDetails docDetails){
		String [] lines =ocrText.toLowerCase().split("\\n");
		List<InvoiceDetails> invoiceDetailsList = new ArrayList<InvoiceDetails>();
		boolean headerFound = false;
		for (String text : lines) {
			if (text.contains("invoice")&& !headerFound) {
				List<RemittenceHeader> remittenceHeaderList = getHeaders(text);
				if (remittenceHeaderList != null) {		
					headerFound= true;
				}
			}
			if(headerFound){
				InvoiceDetails invoiceDetails= new InvoiceDetails();
				AmountsRelatedRemExtractor amountsRelatedRemExtractor=new AmountsRelatedRemExtractor();
				Map<String, String> amountsRelatedMap = amountsRelatedRemExtractor.getFields(text);
				DocumentDetailsExtractor documentDetailsExtractor = new DateExtractor();
				DocumentDetails documentDetails = new DocumentDetails();
				documentDetailsExtractor.extract(text, documentDetails);
			}
		}

		 
		docDetails.setInvoiceDetails(invoiceDetailsList);
		
		return docDetails;
		
	}

	private List<RemittenceHeader> getHeaders(String text) {
		String [] headerNames = {"invoice","date", "amount"};
		int count = 0;
		for (String string : headerNames) {
			if(text.contains(string)){
				count++;
			}
		}
		if(count >2){
			String [] headers = text.replace(" +", " ").split(" ");
			List<RemittenceHeader> remList = new ArrayList<RemittenceHeader>();
			for (String header : headers) {
				if(header.contains("invoice")){
					RemittenceHeader rem = new RemittenceHeader();
					rem.setHeader("Invoice Number");
					rem.setStartIndex(text.indexOf(header));
					rem.setEndIndex(text.indexOf(header)+header.length());
					remList.add(rem);
				}
				if(header.contains("date")){
					RemittenceHeader rem = new RemittenceHeader();
					rem.setHeader("Invoice Date");
					rem.setStartIndex(text.indexOf(header));
					rem.setEndIndex(text.indexOf(header)+header.length());
					remList.add(rem);
				}
				if(header.contains("gross")){
					RemittenceHeader rem = new RemittenceHeader();
					rem.setHeader("Gross Amount");
					rem.setStartIndex(text.indexOf(header));
					rem.setEndIndex(text.indexOf(header)+header.length());
					remList.add(rem);
				}
				if(header.contains("net")){
					RemittenceHeader rem = new RemittenceHeader();
					rem.setHeader("Net Amount");
					rem.setStartIndex(text.indexOf(header));
					rem.setEndIndex(text.indexOf(header)+header.length());
					remList.add(rem);
				}
				if(header.contains("disc")){
					RemittenceHeader rem = new RemittenceHeader();
					rem.setHeader("Discount Amount");
					rem.setStartIndex(text.indexOf(header));
					rem.setEndIndex(text.indexOf(header)+header.length());
					remList.add(rem);
				}
			}
			return remList;
		}
		return null;
	}

	@Override
	public DocumentDetails extract(String ocrText, DocumentDetails docDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
