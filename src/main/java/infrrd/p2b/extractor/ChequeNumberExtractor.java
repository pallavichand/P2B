package infrrd.p2b.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import infrrd.p2b.entity.ChequeDetails;
import infrrd.p2b.entity.DocumentDetails;
import infrrd.p2b.entity.FieldDetails;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChequeNumberExtractor implements DocumentDetailsExtractor {
	String chequeNumberExtractionRegex = "\\d{5,}$";

	@Override
	public DocumentDetails extract(String ocrText, DocumentDetails docDetails) {
	//	log.info("Inside ChequeNumberExtractor class --> OCR text {}", ocrText);
		String[] ocrTextArray = ocrText.split("\\n");
		ChequeDetails checkDetails = new ChequeDetails(docDetails);
		boolean checkNOFound = false;
		int count = 0;
		for (String lineText : ocrTextArray) {
			count++;
			lineText = lineText.replaceAll("[;.,]", "").trim();
			Pattern chequePattern = Pattern.compile(chequeNumberExtractionRegex);
			Matcher chequeMatcher = chequePattern.matcher(lineText);
			if (chequeMatcher.find()) {
				String chequeNO = chequeMatcher.group();
				checkDetails.setCheckNumber(chequeNO);
				checkNOFound = true;
				log.info("Cheque Number Found  --> OCR text {}", chequeNO);
			}
			if (checkNOFound || count > 10) {
				break;
			}
		}

		return checkDetails;
	}

}
