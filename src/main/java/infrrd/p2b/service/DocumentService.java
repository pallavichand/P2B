package infrrd.p2b.service;

import java.io.File;
import java.io.IOException;
import java.util.Map;


public interface DocumentService {

	Map<String, String> processDocumentwitoutUploading(File file) throws IOException;
	
	Map<String, String> processDocument(File file) throws IOException;
	
}
