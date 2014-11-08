package org.aksw.gerbil.web;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.servlet.http.HttpServletResponse;

import org.aksw.gerbil.transfer.FileMeta;
import org.aksw.gerbil.transfer.UploadFileContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Controller
@RequestMapping("/file")
public class FileUploadController {

	private static final transient Logger logger = LoggerFactory
			.getLogger(FileUploadController.class);
	@Value("org.aksw.gerbil.UploadPath")
	private String path;

	public FileUploadController() {

	}

	@RequestMapping(value = "upload", method = RequestMethod.GET)
	public ModelAndView upload() {
		return new ModelAndView("fileupload");
	}

	@RequestMapping(value = "upload", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<UploadFileContainer> upload(
			MultipartHttpServletRequest request, HttpServletResponse response){
		
		if (path == null){
			logger.error("Path must be not null");
			return new ResponseEntity<UploadFileContainer>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		LinkedList<FileMeta> files = new LinkedList<FileMeta>();
		MultipartFile mpf = null;

		for (Iterator<String> it = request.getFileNames(); it.hasNext();) {
			mpf = request.getFile(it.next());
			logger.debug("{} uploaded", mpf.getOriginalFilename());

			FileMeta fileContainer = new FileMeta();
			fileContainer.setName(mpf.getOriginalFilename());
			fileContainer.setSize(mpf.getSize() / 1024 + "Kb");
			fileContainer.setFileType(mpf.getContentType());

			try {
				fileContainer.setBytes(mpf.getBytes());
				FileCopyUtils.copy(mpf.getBytes(), new FileOutputStream(path
						+ mpf.getOriginalFilename()));

			} catch (IOException e) {
				logger.error("Error during file upload", e);
				fileContainer.setError(e.getMessage());
			}
			files.add(fileContainer);
		}

		UploadFileContainer uploadFileContainer = new UploadFileContainer(files);
		return new ResponseEntity<UploadFileContainer>(uploadFileContainer,
				HttpStatus.OK);
	}

}
