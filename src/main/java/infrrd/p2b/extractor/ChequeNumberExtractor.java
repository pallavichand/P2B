package infrrd.p2b.extractor;

import infrrd.p2b.entity.DocumentDetails;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChequeNumberExtractor implements DocumentDetailsExtractor {

	@Override
	public DocumentDetails extract(String octText,DocumentDetails docDetails ) {
		log.info("Inside ChequeNumberExtractor class --> OCR text {}",octText );
		
		return docDetails;
	}

}
