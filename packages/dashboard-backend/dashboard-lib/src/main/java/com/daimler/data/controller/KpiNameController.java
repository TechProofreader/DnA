/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

package com.daimler.data.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.KpiName.KpiNamesApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.KpiName.KpiNameRequestVO;
import com.daimler.data.dto.KpiName.KpiNameResponseVO;
import com.daimler.data.dto.KpiName.KpiNameUpdateRequestVO;
import com.daimler.data.dto.KpiName.KpiNameVOCollection;
import com.daimler.data.service.kpiName.KpiNameService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "the KpiNames API", tags = { "kpiNames" })
@RequestMapping("/api")
public class KpiNameController implements KpiNamesApi {
	
	@Autowired
	private KpiNameService kpiNameService;

	@Override
	@ApiOperation(value = "Add a new kpi name.", nickname = "createKpiNameLov", notes = "Add a new non existing kpi name.", response = KpiNameResponseVO.class, tags={ "KpiNames", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure ", response = KpiNameResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kpinames",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
	public ResponseEntity<KpiNameResponseVO> createKpiNameLov(
			@ApiParam(value = "Request Body that contains data required for creating a new kpi name." ,required=true )  @Valid @RequestBody KpiNameRequestVO kpiNameRequestVO) {
		// TODO Auto-generated method stub
		return kpiNameService.createKpiName(kpiNameRequestVO);
	}

	@Override
	@ApiOperation(value = "Delete the kpi name identified by given ID.", nickname = "deleteKpiNameLov", notes = "Delete the kpi name identified by given ID", response = GenericMessage.class, tags={ "KpiNames", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 404, message = "Invalid id, record not found."),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kpinames/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteKpiNameLov(
			@ApiParam(value = "Id of the kpi name",required=true) @PathVariable("id") Long id) {
		// TODO Auto-generated method stub
		return kpiNameService.deleteKpiName(id);
	}

	@Override
	@ApiOperation(value = "Get all kpi name.", nickname = "getAllKpiNameLov", notes = "Get all kpi name. This endpoints will be used to Get all valid available kpi name.", response = KpiNameVOCollection.class, tags={ "KpiNames", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = KpiNameVOCollection.class),
        @ApiResponse(code = 204, message = "No content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kpinames",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
	public ResponseEntity<KpiNameVOCollection> getAllKpiNameLov(
			@ApiParam(value = "Sort kpinames based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		// TODO Auto-generated method stub
		return kpiNameService.getAllKpiNames(sortOrder);
	}

	@Override
	@ApiOperation(value = "Update the kpi name identified by given ID.", nickname = "updateKpiNameLov", notes = "Update the kpi name identified by given ID", response = KpiNameResponseVO.class, tags={ "KpiNames", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successfully updated.", response = KpiNameResponseVO.class),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 404, message = "Invalid id, record not found."),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kpinames",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
	public ResponseEntity<KpiNameResponseVO> updateKpiNameLov(
			@ApiParam(value = "Request Body that contains data required for updating kpi name." ,required=true )  @Valid @RequestBody KpiNameUpdateRequestVO kpiNameUpdateRequestVO) {
		// TODO Auto-generated method stub
		return kpiNameService.updateKpiName(kpiNameUpdateRequestVO);
	}
	
}