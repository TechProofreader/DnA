package com.mb.dna.data.dataiku.api.dto;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataikuProjectSummaryCollectionDto implements Serializable{

	private List<DataikuProjectSummaryDto> data;
	private BigInteger totalCount;
	
}