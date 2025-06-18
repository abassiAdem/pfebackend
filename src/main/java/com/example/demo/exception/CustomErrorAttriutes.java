package com.example.demo.exception;

import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;
import java.util.Arrays;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
@Component
public class CustomErrorAttriutes extends DefaultErrorAttributes {

	@Override
	public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
		
		Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);

		errorAttributes.put("success", Boolean.FALSE);
		errorAttributes.put("status", errorAttributes.get("error"));
		errorAttributes.put("exception", errorAttributes.get("message"));
		errorAttributes.put("details", Arrays.asList(errorAttributes.get("message")));
		errorAttributes.remove("error");
		errorAttributes.remove("path");

		return errorAttributes;
	}

}
