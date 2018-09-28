package infrrd.p2b.extractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import infrrd.p2b.entity.DocumentDetails;
import infrrd.p2b.entity.InvoiceDetails;
import infrrd.p2b.entity.RemittenceHeader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvoiceDetailsListExtractor implements DocumentDetailsExtractor{
	String amountExtractionRegex = "(?<!\\d)(\\d{1,3}[,]?){1,3}\\d{1,3}[.]\\d{1,2}(?!\\d)";
	@Override
	public DocumentDetails extractRem (String ocrText, DocumentDetails docDetails){
		log.info("OCR Text before correction {}",ocrText);
		ocrText = invoiceCorrection(ocrText);
		log.info("OCR Text after correction {}",ocrText);
		String [] lines =ocrText.toLowerCase().split("\\n");
		List<InvoiceDetails> invoiceDetailsList = new ArrayList<InvoiceDetails>();
		boolean headerFound = false;
		List<RemittenceHeader> remittenceHeaderList = null;
		int invoiceCount = 0;
		int nullCount = 0;
		boolean getInvoiceWithHeader = false;
		for (String text : lines) {
			
			if(foundAmountDatesColumns(text) && !getInvoiceWithHeader){
				headerFound= true;
				if( text.contains("total")){
					break;
				}
				remittenceHeaderList=getInvoiceWithoutHeader(text, invoiceDetailsList);
				
			}
			else if (text.contains("invoice")&& !headerFound) {
				 remittenceHeaderList = getHeaders(text);
				if (remittenceHeaderList != null) {		
					headerFound= true;
					getInvoiceWithHeader =true;
				}
			}
			else if(headerFound){
				text = text.replaceAll("[~-§-|]", " ").replaceAll(" +", " ");
				if( text.contains("total")){
					break;
				}
				if((StringUtils.isEmpty(text.trim())) ){
					nullCount++;
					if(nullCount > 3){
						break;
					}
					continue;
				}
				InvoiceDetails invoiceDetails= getInvoiceDetailsFromOrder(remittenceHeaderList, text);
				if(invoiceDetails != null){
					invoiceCount++;
					nullCount = 0;
				invoiceDetailsList.add(invoiceDetails);
				}
				
				else if(invoiceCount > 0){
					nullCount++;
					if(nullCount >= 3){
						break;
					}
				}
				//if(invoiceDetails == null && invoiceCount==0){
			//			invoiceDetails= 	getInvoiceDetailsFromRegex(remittenceHeaderList,text );
			//	}
				
			}
		}

		
		 
		docDetails.setInvoiceDetails(invoiceDetailsList);
		
		return docDetails;
		
	}



	private List<RemittenceHeader> getInvoiceWithoutHeader(String text, List<InvoiceDetails> invoiceDetailsList) {
		List<RemittenceHeader> remList = new ArrayList<RemittenceHeader>();
		InvoiceDetails invoiceDetails= new InvoiceDetails();
	    text = removeUnwantedWords(text);
		log.info("Text Found {}",text);
		String []columns=text.split(" "); 
		String prevVal ="";
		String prevHeader ="";
		int columnFound = 0;
		boolean isPrevVal = false;
		int count = 0;
		int amount = 0;
		for (String column : columns) {
			if(validateAmount(column)){
				RemittenceHeader remittenceHeader= new RemittenceHeader();
				if(amount == 0){
					remittenceHeader.setHeader("Gross Amount");
					invoiceDetails.setGrossAmount(column);
				}
				else if(amount == 1){
					remittenceHeader.setHeader("Discount Amount");
					invoiceDetails.setDiscountAmount(column);
				}
				else if(amount == 2){
					remittenceHeader.setHeader("Net Amount");
					invoiceDetails.setNetAmount(column);
				}
				amount++;
				
				remittenceHeader.setStartIndex(text.indexOf(column));
				remittenceHeader.setEndIndex(text.indexOf(column+column.length() ));
				columnFound++;
			}
			else if(validateDate(column)){
				RemittenceHeader remittenceHeader= new RemittenceHeader();
				remittenceHeader.setHeader("Invoice Date");
				remittenceHeader.setStartIndex(text.indexOf(column));
				remittenceHeader.setEndIndex(text.indexOf(column+column.length() ));
				remList.add(remittenceHeader);
				columnFound++;
				invoiceDetails.setInvoiceDate(column);
			}
			else{
				if(validateInvoice(column)){
					RemittenceHeader remittenceHeader= new RemittenceHeader();
					remittenceHeader.setHeader(column);
					remittenceHeader.setStartIndex(text.indexOf(column));
					remittenceHeader.setEndIndex(text.indexOf(column+column.length() ));
					remList.add(remittenceHeader);
					prevVal = column;
					isPrevVal = true;
				}
				else{
					RemittenceHeader remittenceHeader= new RemittenceHeader();
					remittenceHeader.setHeader("Unknown"+count);
					remittenceHeader.setStartIndex(text.indexOf(column));
					remittenceHeader.setEndIndex(text.indexOf(column+column.length() ));
					remList.add(remittenceHeader);
					
				}
				
			}
			if(isPrevVal && columnFound==1){
				for (RemittenceHeader rem : remList) {
					if(rem.getHeader().equals(prevVal)){
						rem.setHeader("Invoice Number");
						invoiceDetails.setInvoiceNumber(prevVal);
						break;
					}
					
				}
				
			}
				count++;
		}
		if(invoiceDetails.getInvoiceDate()!=null || invoiceDetails.getGrossAmount()!= null){
			if(invoiceDetails.getNetAmount()!= null || invoiceDetails.getGrossAmount()!=null ){
				invoiceDetails= formatDollarValue(invoiceDetails);
				invoiceDetails =setAmountByCalculating(invoiceDetails);
			}
			invoiceDetailsList.add(invoiceDetails);
		}
		return remList;
	}



	private boolean foundAmountDatesColumns(String text) {
		String []words = text.replaceAll(" +", " ").split(" ");
		int count=0;
		for (String string : words) {
			if(validateAmount(string)){
				count++;
			}
			else if(validateDate(string)){
				count++;
			}
			if(count>=3){
				return true;
			}
		}
		return false;
	}

	private InvoiceDetails getInvoiceDetailsFromOrder(List<RemittenceHeader> remittenceHeaderList, String text) {
		InvoiceDetails invoiceDetails= new InvoiceDetails();
	    text = removeUnwantedWords(text);
		log.info("Text Found {}",text);
		int i = 0;
		int remVal = 0;
		try{
		for (RemittenceHeader remittenceHeader : remittenceHeaderList) {
			String []columns=text.split(" "); 
			if(columns.length <= 2){
				return null;
			}
			log.info("Column Found {}",columns[i]);
			if(remittenceHeader.getHeader().contains("desc")){
				if(!validateAmount(columns[i])||!validateDate(columns[i]) ){
					continue;
				}
			}
			if(remittenceHeader.getHeader().equals("Invoice Number")){
				while (!validateInvoice(columns[i])) {
					i++;
					if (i == columns.length) {
						break;
					}
				}
				invoiceDetails.setInvoiceNumber(columns[i]);
				i++;
				remVal++;
			}
				else if (remittenceHeader.getHeader().equals("Invoice Date")) {
					while (!validateDate(columns[i])) {
						i++;
						if (i == columns.length) {
							break;
						}
					}
					invoiceDetails.setInvoiceDate(columns[i]);
					i++;
					remVal++;
				}
			else if(remittenceHeader.getHeader().equals("Gross Amount")){
				while(!validateAmount(columns[i])){
					i++;
					if(i==columns.length ){
						break;
					}
					}
				invoiceDetails.setGrossAmount(columns[i]);
				i++;
				remVal++;
			}
			else if(remittenceHeader.getHeader().equals("Net Amount")){
				while(!validateAmount(columns[i])){
					i++;
					if(i==columns.length ){
						break;
					}
					}
				invoiceDetails.setNetAmount(columns[i]);
				i++;
				remVal++;
			}
			else if(remittenceHeader.getHeader().equals("Discount Amount")){
				while(!validateAmount(columns[i])){
					i++;
					if(i==columns.length ){
						break;
					}
					}
				invoiceDetails.setDiscountAmount(columns[i]);
				i++;
				remVal++;
			}
			if(i==columns.length ){
				if(remVal< 2){
					return null;
				}
				break;
			}
			
		}
		}
		catch (Exception e){
			log.info("Error happened while getting invoice details {}", e.getMessage());
			return null;
		}
		if(invoiceDetails.getNetAmount()!= null || invoiceDetails.getGrossAmount()!=null ){
			invoiceDetails= formatDollarValue(invoiceDetails);
			invoiceDetails =setAmountByCalculating(invoiceDetails);
		}
		return invoiceDetails;
	}

	private InvoiceDetails formatDollarValue(InvoiceDetails invoiceDetails) {
		if((invoiceDetails.getGrossAmount()!= null && invoiceDetails.getGrossAmount().contains("$")) ||
				(invoiceDetails.getNetAmount()!= null && invoiceDetails.getNetAmount().contains("$"))||
				(invoiceDetails.getDiscountAmount()!= null && invoiceDetails.getDiscountAmount().contains("$")) ){
			Pattern nonDollarPattern = Pattern.compile("(?<!\\d|[$])(5)((\\d{1,3}[,]?){0,3}\\d{1,3}[.]\\d{1,2})(?!\\d)");
			if(invoiceDetails.getNetAmount()!= null  && !invoiceDetails.getNetAmount().contains("$")){
				Matcher nonDollarMatcher = nonDollarPattern.matcher(invoiceDetails.getNetAmount());
				if(nonDollarMatcher.find()){
					invoiceDetails.setNetAmount("$"+nonDollarMatcher.group(2));
				}
			}
			if(invoiceDetails.getGrossAmount()!= null && !invoiceDetails.getGrossAmount().contains("$")){
				Matcher nonDollarMatcher = nonDollarPattern.matcher(invoiceDetails.getGrossAmount());
				if(nonDollarMatcher.find()){
					invoiceDetails.setGrossAmount("$"+nonDollarMatcher.group(2));
				}
			}
			if(invoiceDetails.getDiscountAmount()!= null && !invoiceDetails.getDiscountAmount().contains("$")){
				Matcher nonDollarMatcher = nonDollarPattern.matcher(invoiceDetails.getDiscountAmount());
				if(nonDollarMatcher.find()){
					invoiceDetails.setDiscountAmount("$"+nonDollarMatcher.group(2));
				}
			}
		}

		
		return invoiceDetails;
	}

	private InvoiceDetails setAmountByCalculating(InvoiceDetails invoiceDetails) {
		String discount="";
		String net="";
		String gross="";
		if(invoiceDetails.getDiscountAmount()!= null){
			discount= invoiceDetails.getDiscountAmount().replaceAll("[$,-]", "");
			Pattern datePattern = Pattern.compile("([.,])(\\d{3}[.,])");
			Matcher dateMatcher = datePattern.matcher(discount);
			if(dateMatcher.find()){
				discount = net.replaceAll(dateMatcher.group(), dateMatcher.group(2));
			}
			invoiceDetails.setDiscountAmount("$"+discount);
		}
		if(invoiceDetails.getGrossAmount()!= null){
			gross= invoiceDetails.getGrossAmount().replaceAll("[$,]", "");
			Pattern datePattern = Pattern.compile("([.,])(\\d{3}[.,])");
			Matcher dateMatcher = datePattern.matcher(net);
			if(dateMatcher.find()){
				net = net.replaceAll(dateMatcher.group(), dateMatcher.group(2));
			}
			invoiceDetails.setGrossAmount("$"+gross);
		}
		if(invoiceDetails.getNetAmount()!= null){
			net= invoiceDetails.getNetAmount().replaceAll("[$,]", "");
			Pattern datePattern = Pattern.compile("([.,])(\\d{3}[.,])");
			Matcher dateMatcher = datePattern.matcher(net);
			if(dateMatcher.find()){
				net = net.replaceAll(dateMatcher.group(), dateMatcher.group(2));
			}
			invoiceDetails.setNetAmount("$"+net);
		}
		if(invoiceDetails.getDiscountAmount()!= null && invoiceDetails.getGrossAmount()!= null && invoiceDetails.getNetAmount()!=null){
			return invoiceDetails;
		}
		else {
			try {
				if (invoiceDetails.getDiscountAmount() == null) {
					if (invoiceDetails.getGrossAmount() != null && invoiceDetails.getNetAmount() != null) {
						double disc = (Double.parseDouble(gross) - Double.parseDouble(net));
						discount = "$" + String.format("%.2f", disc);
						invoiceDetails.setDiscountAmount(discount);
					} else if (invoiceDetails.getGrossAmount() != null) {
						invoiceDetails.setNetAmount(invoiceDetails.getGrossAmount());
						invoiceDetails.setDiscountAmount("$0.00");
					} else {
						invoiceDetails.setGrossAmount(invoiceDetails.getNetAmount());
						invoiceDetails.setDiscountAmount("$0.00");
					}
				} else if (invoiceDetails.getGrossAmount() == null) {
					if (invoiceDetails.getNetAmount() != null) {
						double grossAmt = (Double.parseDouble(net) + Double.parseDouble(discount));
						gross = "$" + String.format("%.2f", grossAmt);
						invoiceDetails.setGrossAmount("$" + gross);
					}
				} else{
					double netAmt = (Double.parseDouble(gross) - Double.parseDouble(discount));
					net = "$" + String.format("%.2f", netAmt);
					invoiceDetails.setGrossAmount("$" + net);
				}
			} catch (Exception e) {
				log.error("Error happenedconverting numbers {}", e.getMessage());
			}
		}
		return invoiceDetails;
		
	}

	private boolean validateInvoice(String string) {
		Pattern invoicePattern = Pattern.compile("\\d+");
		Matcher invoiceMatcher = invoicePattern.matcher(string);
		if(invoiceMatcher.find()){
			return true;
		}
		return false;
	}

	private boolean validateAmount(String string) {
		String amountExtractionRegex = "(?<!\\d)(\\d{1,3}[,]?){0,3}\\d{1,3}[.]\\d{1,2}(?!\\d)";
		Pattern amountPattern = Pattern.compile(amountExtractionRegex);
		Matcher amountMatcher = amountPattern.matcher(string);
		if(amountMatcher.find()){
			return true;
		}
		return false;
	}

	private boolean validateDate(String string) {
		List<String> dateExtractorRegex = new ArrayList<String>();
		dateExtractorRegex.add("(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)([a-z]{1,7})?(\\s)?\\d{1,2}[.,\\s]\\s?\\d{4}");	
		dateExtractorRegex.add("(?<!\\d)([0]?[1-9]|[1][1-2])[.\\--\\/]([0-2]?[1-9]|[3][0-1])[.\\--\\/]\\d{2,4}(?!\\d)");
		dateExtractorRegex.add("(?<!\\d)\\d{4}[.\\--\\/]([0]?[1-9]|[1][1-2])[.\\--\\/]([0-2]?[1-9]|[3][0-1])(?!\\d)");
		for (String dateRegex : dateExtractorRegex) {
			Pattern datePattern = Pattern.compile(dateRegex);
			Matcher dateMatcher = datePattern.matcher(string);
			if(dateMatcher.find()){
				return true;
			}
		}
		return false;
	}
	
	

	private String removeUnwantedWords(String text) {
		String invoiceCorrectRegex = "\\W({0})\\W";
	    String [] nonInvoice = {"updated"};
	    for (String string : nonInvoice) {
	    	String regexVal = invoiceCorrectRegex.replace("{0}",string);
			Pattern datePattern = Pattern.compile(regexVal);
			Matcher dateMatcher = datePattern.matcher(text);
			while(dateMatcher.find()){
				text = text.replace(string, "");
				text=text.replaceAll(" +", " ");
			}
		}
		return text;
	}

	private InvoiceDetails getInvoiceDetailsFromRegex(List<RemittenceHeader> remittenceHeaderList, String text) {
		InvoiceDetails invoiceDetails= new InvoiceDetails();
		AmountsRelatedRemExtractor amountsRelatedRemExtractor=new AmountsRelatedRemExtractor();
		Map<String, String> amountsRelatedMap = amountsRelatedRemExtractor.getFields(text);
		DocumentDetailsExtractor documentDetailsExtractor = new DateExtractor();
		DocumentDetails documentDetails = new DocumentDetails();
		documentDetailsExtractor.extract(text, documentDetails);
		invoiceDetails.setGrossAmount(amountsRelatedMap.get("total gross amount"));
		invoiceDetails.setNetAmount(amountsRelatedMap.get("total net amount"));
		invoiceDetails.setDiscountAmount(amountsRelatedMap.get("total discount amount"));
		invoiceDetails.setInvoiceDate(documentDetails.getBillDate());
		invoiceDetails.setInvoiceNumber(findInvoiceNo_(remittenceHeaderList, text));
		return invoiceDetails;
	}



	private String findInvoiceNo_(List<RemittenceHeader> remittenceHeaderList, String text) {
		int startIndex = 0;
		int endIndex = text.length();
		String invoice = "";
		for (RemittenceHeader remittenceHeader : remittenceHeaderList) {
			
			if(remittenceHeader.getHeader().equals("Invoice Number")){
				endIndex = remittenceHeader.getEndIndex();
				if(startIndex == 0){
					startIndex = remittenceHeader.getStartIndex();
				}
				invoice = text.substring(startIndex, endIndex);
			}
			else{
				startIndex = remittenceHeader.getEndIndex();
			}
			
		}
		return invoice;
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
			text = text.replaceAll("[|:]", " ");
			String [] headers = text.split("  +");
			List<RemittenceHeader> remList = new ArrayList<RemittenceHeader>();
			//String regexForHeaders = "invoice number|invoice no.|invoice|invoice date|date|net amount|gross amount|discount amount|amount";
//			Pattern headerRegex = Pattern.compile(regexForHeaders);
//			Matcher headerMapper = headerRegex.matcher(text);
//			while(headerMapper.find()){
			for (String header : headers) {
				//String header = headerMapper.group();
				if(header.contains("invoice") && !header.contains("date") ){
					RemittenceHeader rem = new RemittenceHeader();
					rem.setHeader("Invoice Number");
					rem.setStartIndex(text.indexOf(header));
					rem.setEndIndex(text.indexOf(header)+header.length());
					remList.add(rem);
				}
				else if(header.contains("date")){
					RemittenceHeader rem = new RemittenceHeader();
					rem.setHeader("Invoice Date");
					rem.setStartIndex(text.indexOf(header));
					rem.setEndIndex(text.indexOf(header)+header.length());
					remList.add(rem);
				}
				else if(header.contains("gross")|| header.equals("amount")){
					RemittenceHeader rem = new RemittenceHeader();
					rem.setHeader("Gross Amount");
					rem.setStartIndex(text.indexOf(header));
					rem.setEndIndex(text.indexOf(header)+header.length());
					remList.add(rem);
				}
				else if(header.contains("net")){
					RemittenceHeader rem = new RemittenceHeader();
					rem.setHeader("Net Amount");
					rem.setStartIndex(text.indexOf(header));
					rem.setEndIndex(text.indexOf(header)+header.length());
					remList.add(rem);
				}
				else if(header.contains("disc")){
					RemittenceHeader rem = new RemittenceHeader();
					rem.setHeader("Discount Amount");
					rem.setStartIndex(text.indexOf(header));
					rem.setEndIndex(text.indexOf(header)+header.length());
					remList.add(rem);
				}
				else{
					RemittenceHeader rem = new RemittenceHeader();
					rem.setHeader(header);
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
	
	private  String invoiceCorrection(String ocrText) {
		Map<String, String> invoiceCorrect = new HashMap<String, String>();
		String invoiceCorrectRegex = "\\W({0})\\W";
		ocrText =ocrText.toLowerCase();
	    invoiceCorrect.put("orossamount","grossamount");
		for (Entry<String, String> dateVal : invoiceCorrect.entrySet()) {
			String regexVal = invoiceCorrectRegex.replace("{0}",dateVal.getKey() );
			Pattern datePattern = Pattern.compile(regexVal);
			Matcher dateMatcher = datePattern.matcher(ocrText);
			while(dateMatcher.find()){
				ocrText = ocrText.replace(dateMatcher.group(), " "+dateVal.getValue()+" ");
			}
		}
		return ocrText;
	}

	
}
