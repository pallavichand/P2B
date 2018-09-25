package infrrd.p2b.extractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import infrrd.p2b.entity.DocumentDetails;
import infrrd.p2b.entity.FieldDetails;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class AmountExtractor implements DocumentDetailsExtractor {

	String amountExtractionRegex = "(?<!\\d)(\\d{1,3}[,]?){1,3}\\d{1,3}[.]\\d{1,2}(?!\\d)";
	@Override
	public DocumentDetails extract(String ocrText, DocumentDetails docDetails) {
	//	log.info("Inside AmountExtractor  --> OCR text {}", ocrText);
		Pattern amountPattern = Pattern.compile(amountExtractionRegex);
		Matcher amountMatcher = amountPattern.matcher(ocrText);
		List<FieldDetails> amountList = new ArrayList<FieldDetails>();
		while (amountMatcher.find()) {
			String amountVal = amountMatcher.group();
			amountVal = amountVal.replaceAll(",", "");
			if (isNumeric(amountVal)) {
				FieldDetails fieldDetailsVal = new FieldDetails();
				fieldDetailsVal.setValues(amountVal);
				String vicinty = checkForVicinity(amountMatcher.start(), ocrText);
				if (!StringUtils.isEmpty(vicinty)) {
					fieldDetailsVal.setVicinity(true);
					fieldDetailsVal.setVicinityWord(vicinty);
				}
				String currency = checkForCurrency(amountMatcher.start(), ocrText);
				if (!StringUtils.isEmpty(currency)) {
					fieldDetailsVal.setSpecialSymbol(true);
					fieldDetailsVal.setSpecialSymbolValue(currency);
				}
				amountList.add(fieldDetailsVal);
			}
		}
		if (!amountList.isEmpty()) {
			String amountVal = getFinalAmountVal(amountList);
			log.info("Amount Value Found  --> OCR text {}", amountVal);
			docDetails.setAmount(amountVal);
		}

		return docDetails;
	}
	
	private String getFinalAmountVal(List<FieldDetails> amountList) {
		if(amountList.size()==1){
			return amountList.get(0).getValues();
		}
		Collections.sort(amountList, new Comparator<FieldDetails>() {
			@Override
			public int compare(FieldDetails p1, FieldDetails p2) {
				if (p1.isVicinity() && p2.isVicinity() || !p1.isVicinity() && !p2.isVicinity()) {
				if (p1.isSpecialSymbol() && p2.isSpecialSymbol() || !p1.isSpecialSymbol() && !p2.isSpecialSymbol()) {
						return Double.valueOf(p2.getValues()).compareTo(Double.valueOf(p1.getValues()));
					} else if (p2.isVicinity()) {
						return 1;
					} else {
						return -1;
					}
				} else if (p2.isSpecialSymbol()) {
					return 1;
				} else {
					return -1;
				}
			}
		});
		log.info("Sorted List  --> OCR text {}", amountList.toString());
		return amountList.get(0).getValues();
	}

	private String checkForCurrency(int start, String ocrText) {
		String[] currencyVal = { "$", "USD" };
		int startIndex = start > 10 ? start - 10 : 0;
		int endIndex = start + 10 > ocrText.length() - 1 ? ocrText.length() - 1 : start + 10;
		String textToCheck = ocrText.substring(startIndex, endIndex);
		for (String currency : currencyVal) {
			if (textToCheck.contains(currency)) {
				return currency;
			}
		}
		return null;
	}

	private String checkForVicinity(int start, String ocrText) {
		String[] vicinityWords = { "account" , "total", "amount"};
		int startIndex = start > 50 ? start - 50 : 0;
		int endIndex = start + 50 > ocrText.length() - 1 ? ocrText.length() - 1 : start + 50;
		String textToCheck = ocrText.toLowerCase().substring(startIndex, endIndex);
		for (String vicinity : vicinityWords) {
			if (textToCheck.contains(vicinity)) {
				return vicinity;
			}
		}
		return null;
	}

	public static boolean isNumeric(String strNum) {
	    try {
	        @SuppressWarnings("unused")
			double d = Double.parseDouble(strNum);
	    } catch (NumberFormatException | NullPointerException nfe) {
	        return false;
	    }
	    return true;
	}

	@Override
	public DocumentDetails extractRem(String ocrText, DocumentDetails docDetails) {
		// TODO Auto-generated method stub
		return extract(ocrText, docDetails);
	}
	


}