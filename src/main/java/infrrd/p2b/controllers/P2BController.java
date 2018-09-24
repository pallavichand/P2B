package infrrd.p2b.controllers;

import java.awt.List;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import infrrd.p2b.service.DocumentService;
import infrrd.p2b.service.StorageService;
import infrrd.p2b.service.impl.StorageServiceImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/p2b-service")
public class P2BController {

	@Autowired
	DocumentService documentService;

	@Autowired
	StorageService storageService;

	@CrossOrigin
	@RequestMapping("/noa/fileupload")
	@PostMapping()
	public ResponseEntity<?> newDocumentFileUpload(@RequestPart("file") MultipartFile file) throws IOException {

		if ("pdf".equalsIgnoreCase(FilenameUtils.getExtension(file.getOriginalFilename()))) {

			final File uploadedFile;
			uploadedFile = storageService.uploadFile(file);
			Object documentService;
			// Map<String, String> output =
			// documentService.processDocumentwitoutUploading(uploadedFile);

			log.info("<------------------START-------------------->");
			log.info("*********************************************");
			log.info("Processing document with filename {}", file.getName());

			// Map<String,String> outputJson = getFinalOutput (output);
			Map<String, String> outputJson = new HashMap<String, String>();

			log.info("The Extracted values are "+new
			 JSONObject(outputJson).toString());
			log.info("<--------------------------COMPLETED REQUEST------------------->");
			return new ResponseEntity<String>(new JSONObject(outputJson).toString(), HttpStatus.OK);
		} else {
			log.info("File of invalid type: ", file.getName());
			return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
		}

	}

}
