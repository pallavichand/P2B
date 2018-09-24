package infrrd.p2b.extractor;

import infrrd.p2b.entity.DocumentDetails;

public interface DocumentDetailsExtractor {
	public DocumentDetails extract(String octText ,DocumentDetails docDetails);
}
