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
    	
    	//String filePath = "/home/pallavi/work/Repos/POC/Remittances/Remittances/";
    	//String filePath = "/home/pallavi/work/POC/P2B/Examples for Infrrd/Examples for Infrrd/";
    	//Cheques -------------------------------------------------------------------------------------
        String fileName = "2D_LB_DOCS_9.19.17-page-1-1.txt"; //143.36, 0000995638, Absolute Security Systems Inc, 2d Electronics, september 14, 2017
    //String fileName = "7MED_LB_DOCS_9.19.17-page-1-1.txt"; //3946.51, 0000975300,Chiro Med Health Center Inc, Softwise, september 12, 2017
      // String fileName = "70KFT_LB_DOCS_9.19.17-page-1-1.txt"; //7346.44, 57877,Numed, Inc, No text Found (70Kft), sep 11 2017
      //  String fileName = "AGENCY_LB_DOCS_9.18.17-page-1-1.txt"; //1861.50, 18688, Insurance Aquisitions Inc (Text not found), Agengy Revolution Of C/o P2binvestor, Inc Y, sep 7,2017
      // String fileName = "BLUE_LB_DOCS_9.18.17-page-1-1.txt";//12106.86, 522844580, Quallis Brands Llc, Blue Moon Digital Inc, 09-05-2017
       //String fileName = "SPECCO_LB_DOCS_9.19.17-page-1-1.txt"; //Amount Issue, 11306,Allsource Supply Inc, No text found, 8/28/2017
       //String fileName = "SPECCO_LB_DOCS_9.19.17-page-3-1.txt"; //3046.46,60148 , Landscape Depot, Inc, Specco Industries, 09/08/17
     // String fileName = "SPECCO_LB_DOCS_9.19.17-page-5-1.txt";//820.71, 0282847, Stetson Building Products, Llc, Specco Industries, 09/12/17
     // String fileName = "SPECCO_LB_DOCS_9.19.17-page-7-1.txt"; //742.50, 862796,Siteone Landscape Supply ,Specco Industries, 9/8/2017
     //String fileName = "SPECCO_LB_DOCS_9.19.17-page-12-1.txt"; //495.31,038472, Specco Industries, Inc, 09/11/17
    //	String fileName ="page-1.txt";
     //Remmitences --------------------------------------------------------------------------------------------
     //String fileName = "2D_LB_DOCS_9.19.17-page-2-1.txt"; 
   //  String fileName = "70KFT_LB_DOCS_9.19.17-page-2-1.txt";
    // String fileName = "AGENCY_LB_DOCS_9.18.17-page-2-1.txt";
    //String fileName = "CAGNEY_ACH_9.20.17-page-2-1.txt"; //Dummy
     //String fileName = "SEVEN_LB_DOCS_9.19.17.pdf-page-2-1.txt";
     //String fileName = "SEVEN_LB_DOCS_9.19.17.pdf-page-5-1.txt";
     //String fileName = "SEVEN_LB_DOCS_9.19.17.pdf-page-2-1.txt";
        
        DocumentServiceImpl documentService = new DocumentServiceImpl();
        Map<String, String> match = documentService.getTextByPathToTest( filePath+fileName ,  "chk");
        log.info("Amount Found  -->  {}", match.get("Amount"));
        log.info("Cheque Number Found  -->  {}",  match.get("Cheque Number"));
        log.info("Payor Found  --> {}",  match.get("Payer"));
        log.info("Payee Found  --> {}",  match.get("Payee"));
        log.info("Bill Date Found  --> {}",  match.get("Cheque Date"));
    }
}
