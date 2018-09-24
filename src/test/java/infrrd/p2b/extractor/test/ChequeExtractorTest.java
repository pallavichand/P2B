package infrrd.p2b.extractor.test;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;
import infrrd.p2b.service.impl.DocumentServiceImpl;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class ChequeExtractorTest {
	
	
    @Test
    public void testExtractDefault() throws IOException
    {
        String filePath = " /home/pallavi/work/Repos/POC/Cheques_Tesseract_o:p/";
        //String fileName = "2D_LB_DOCS_9.19.17-page-1-1.txt"; //143.36, 0000995638, Absolute Security Systems Inc
      // String fileName = "7MED_LB_DOCS_9.19.17-page-1-1.txt"; //3946.51, 0000975300,Chiro Med Health Center Inc
       // String fileName = "70KFT_LB_DOCS_9.19.17-page-1-1.txt"; //7346.44, 57877,Numed, Inc
        //String fileName = "AGENCY_LB_DOCS_9.18.17-page-1-1.txt"; //1861.50, 18688, Insurance Aquisitions Inc (Text not found)
       //String fileName = "BLUE_LB_DOCS_9.18.17-page-1-1.txt";//12106.86, 522844580, Quallis Brands Llc
      // String fileName = "SPECCO_LB_DOCS_9.19.17-page-1-1.txt"; //Amount Issue, 11306,Allsource Supply Inc
      // String fileName = "SPECCO_LB_DOCS_9.19.17-page-3-1.txt"; //3046.46,60148 , Landscape Depot, Inc
      // String fileName = "SPECCO_LB_DOCS_9.19.17-page-5-1.txt";//820.71, 0282847
      //String fileName = "SPECCO_LB_DOCS_9.19.17-page-7-1.txt"; //742.50, 862796
      String fileName = "SPECCO_LB_DOCS_9.19.17-page-12-1.txt"; //495.31,038472, Specco Industries, Inc

        
        DocumentServiceImpl documentService = new DocumentServiceImpl();
        Map<String, String> match = documentService.getTextByPathToTest( filePath+fileName );
        log.info("Amount Found  -->  {}", match.get("Amount"));
        log.info("Cheque Number Found  -->  {}",  match.get("ChequeNumber"));
        log.info("Payor Found  --> {}",  match.get("Payor"));
    }
}
