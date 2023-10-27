package com.daimler.data.kong.client;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttachJwtPluginConfigRequestDto implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String algorithm;
	private String secret;
	private String authurl;
	private String clientHomeUrl;
	private String privateKeyFilePath;
	private String expiresIn;
	private String client_id;
	private String client_secret;
	private String introspection_uri;
	private Boolean enableAuthTokenIntrospection;
}