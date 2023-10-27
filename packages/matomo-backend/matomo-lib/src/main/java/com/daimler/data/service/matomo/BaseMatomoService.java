package com.daimler.data.service.matomo;

import com.daimler.data.application.client.MatomoClient;
import com.daimler.data.assembler.MatomoAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.MatomoNsql;
import com.daimler.data.db.json.Matomo;
import com.daimler.data.db.repo.matomo.MatomoCustomRepository;
import com.daimler.data.db.repo.matomo.MatomoRepository;
import com.daimler.data.dto.MatomoGetSiteResponseDto;
import com.daimler.data.dto.MatomoGetSitesAccessCollectionDto;
import com.daimler.data.dto.MatomoGetSitesAccessDto;
import com.daimler.data.dto.MatomoSiteResponseDto;
import com.daimler.data.dto.matomo.*;
import com.daimler.data.service.common.BaseCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BaseMatomoService extends BaseCommonService<MatomoVO, MatomoNsql, String> implements MatomoService {

    @Autowired
    private MatomoCustomRepository customRepo;
    @Autowired
    private MatomoAssembler assembler;
    @Autowired
    private MatomoClient matomoClient;
    @Autowired
    private MatomoRepository jpaRepo;

    @Override
    public MatomoResponseVO createMatomoSite(String matomoId, String siteId, Date createdOn, Date lastModified, MatomoSiteRequestVO matomoRequestVO, CreatedByVO requestUser) {
        GenericMessage responseMessage = new GenericMessage();
        MatomoVO matomoVO = new MatomoVO();
        MatomoSiteVO matomoSiteVO = new MatomoSiteVO();
        matomoVO.setId(matomoId);
        matomoVO.setSiteId(siteId);
        matomoVO.setSiteName(matomoRequestVO.getSiteName());
        matomoVO.setSiteUrl(matomoRequestVO.getSiteUrl());
        matomoVO.setDivision(matomoRequestVO.getDivision());
        matomoVO.setSubDivision(matomoRequestVO.getSubDivision());
        matomoVO.setDepartment(matomoRequestVO.getDepartment());
        matomoVO.setClassificationType(matomoRequestVO.getClassificationType());
        matomoVO.setPiiData(matomoRequestVO.isPiiData());
        matomoVO.setCreatedBy(requestUser);
        matomoVO.setStatus(matomoRequestVO.getStatus());
        matomoVO.setPermission(matomoRequestVO.getPermission());
        matomoVO.setCollaborators(matomoRequestVO.getCollaborators());
        matomoVO.setCreatedOn(createdOn);
        matomoVO.setLastModified(lastModified);

        MatomoResponseVO responseWrapperVO = new MatomoResponseVO();
        try {
            super.create(matomoVO);
            responseMessage.setSuccess("SUCCESS");

        } catch (Exception e) {
            log.error("Failed while saving details of matomo site {} to database for site {}, created by {}", siteId, matomoRequestVO.getSiteName(), requestUser);
            MessageDescription msg = new MessageDescription("Failed to save matomo site details to table ");
            List<MessageDescription> errors = new ArrayList<>();
            errors.add(msg);
            responseMessage.setErrors(errors);
        }
        responseWrapperVO.setData(matomoVO);
        responseWrapperVO.setResponse(responseMessage);
        return responseWrapperVO;
    }


    @Override
    public MatomoCollectionVO getAll(int limit, int offset, String user) {
        MatomoCollectionVO matomoCollectionWrapper = new MatomoCollectionVO();
        int totalCount = 0;
        MatomoGetSiteResponseDto getSiteResponse = new MatomoGetSiteResponseDto();
        List<MatomoGetSitesAccessDto> getSiteAccess = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        MatomoVO matomoVO = new MatomoVO();
        List<MatomoVO> newMatomoList = new ArrayList<>();
        List<MatomoVO> matomoVOResponse = new ArrayList<>();
        MatomoGetSitesAccessCollectionDto response = matomoClient.getSitesAccessFromUser(user);
        if (response != null && response.getData() != null && "SUCCESS".equalsIgnoreCase(response.getStatus())) {
            getSiteAccess = response.getData();

            List<String> siteIdList = getSiteAccess.stream().map(MatomoGetSitesAccessDto::getSite).collect(Collectors.toList());
            if (siteIdList != null || !siteIdList.isEmpty()) {
                for (String siteId : siteIdList) {
                    MatomoNsql entity = customRepo.findUserById(siteId);
                    if (entity != null) {
                        matomoVO = assembler.toVo(entity);
                        List<CollaboratorVO> collaborators = new ArrayList<CollaboratorVO>();
                        getSiteResponse = matomoClient.listParticularMatomoSite(matomoVO.getSiteId());
                        if (getSiteResponse != null && "SUCCESS".equalsIgnoreCase(getSiteResponse.getStatus())) {
                            matomoVO.setSiteName(getSiteResponse.getName());
                            matomoVO.setSiteUrl(getSiteResponse.getMain_url());
                        }

                        map = matomoClient.getUsersAccessFromSite(matomoVO.getSiteId());
                        for (Map.Entry<String, Object> entry : map.entrySet()) {

                            String key = entry.getKey();
                            if (key.equalsIgnoreCase(user)) {
                                matomoVO.setPermission(entry.getValue().toString());
                            } else {
                                CollaboratorVO collaborator = new CollaboratorVO();
                                collaborator.setId(entry.getKey());
                                collaborator.setPermission(entry.getValue().toString());
                                collaborators.add(collaborator);
                            }
                        }
                        matomoVO.setCollaborators(collaborators);
                        matomoVOResponse.add(matomoVO);
                    }
                }


                totalCount = matomoVOResponse.size();
                int endLimit = offset + limit;
                if (endLimit > matomoVOResponse.size()) {
                    endLimit = matomoVOResponse.size();
                }
                newMatomoList = matomoVOResponse.subList(offset, endLimit);
                matomoCollectionWrapper.setRecords(newMatomoList);
                matomoCollectionWrapper.setTotalCount(totalCount);
            }
        }

        return matomoCollectionWrapper;

    }

    @Override
    public MatomoVO getMatomoById(String matomoId, String user) {
        MatomoVO matomoVO = new MatomoVO();
        MatomoGetSiteResponseDto getSiteResponse = new MatomoGetSiteResponseDto();
        Map<String, Object> map = new HashMap<>();
        Optional<MatomoNsql> anyEntity = this.jpaRepo.findById(matomoId);
        if (anyEntity != null && anyEntity.isPresent()) {
            MatomoNsql entity = anyEntity.get();
            matomoVO = assembler.toVo(entity);
            try {
                List<CollaboratorVO> collaborators = new ArrayList<CollaboratorVO>();
                getSiteResponse = matomoClient.listParticularMatomoSite(matomoVO.getSiteId());
                if (getSiteResponse != null && "SUCCESS".equalsIgnoreCase(getSiteResponse.getStatus())) {
                    matomoVO.setSiteName(getSiteResponse.getName());
                    matomoVO.setSiteUrl(getSiteResponse.getMain_url());
                }
                map = matomoClient.getUsersAccessFromSite(matomoVO.getSiteId());
                for (Map.Entry<String, Object> entry : map.entrySet()) {

                    String key = entry.getKey();
                    if (key.equalsIgnoreCase(user)) {
                        matomoVO.setPermission(entry.getValue().toString());
                    } else {
                        CollaboratorVO collaborator = new CollaboratorVO();
                        collaborator.setId(entry.getKey());
                        collaborator.setPermission(entry.getValue().toString());
                        collaborators.add(collaborator);
                    }
                }
                matomoVO.setCollaborators(collaborators);
            } catch (Exception e) {
                log.error("Failed while getting matamo site details results data for user  {} with exception {} ", user, e.getMessage());
            }
        }
        return matomoVO;
    }


    @Override
    public GenericMessage deleteMatomoByID(String id, String user) {
        GenericMessage responseMessage = new GenericMessage();
        List<MessageDescription> errors = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        List<MessageDescription> warnings = new ArrayList<>();
        boolean isAdmin = false;
        Optional<MatomoNsql> entityOptional = jpaRepo.findById(id);
        if (entityOptional != null) {
            MatomoNsql entity = entityOptional.get();
            String siteId = entity.getData().getSiteId();
            map = matomoClient.getUsersAccessFromSite(siteId);
            for (Map.Entry<String, Object> entry : map.entrySet()) {

                String key = entry.getKey();
                if (key.equalsIgnoreCase(user)) {
                    String userPermission = entry.getValue().toString();
                    if ("admin".equalsIgnoreCase(userPermission)) {
                        isAdmin = true;
                    }
                }
            }
            if (isAdmin) {
                MatomoSiteResponseDto deleteMatomoSiteResponse = matomoClient.deleteMatomoSite(siteId);

                if (deleteMatomoSiteResponse != null && "FAILED".equalsIgnoreCase(deleteMatomoSiteResponse.getStatus())) {
                    String msg = "Failed to delete Bucket.";
                    MessageDescription errMsg = new MessageDescription(msg);
                    errors.add(errMsg);
                    responseMessage.setSuccess("FAILED");
                    responseMessage.setErrors(errors);
                    log.error("Failed to delete matomo site try again.");
                    return responseMessage;
                }

                try {
                    // To delete an Entity.
                    this.jpaRepo.delete(entity);
                    responseMessage.setErrors(null);
                    responseMessage.setSuccess("SUCCESS");
                } catch (Exception e) {
                    log.error("Failed while deleting matomo site {}", siteId);
                    MessageDescription msg = new MessageDescription("Failed while deleting matomo site ");
                    errors.add(msg);
                    responseMessage.setErrors(errors);
                    responseMessage.setSuccess("FAILED");
                }
            } else {
                log.error("Failed while deleting matomo sit {} as user{} is not admin", siteId, user);
                MessageDescription msg = new MessageDescription("Failed while deleting matomo site as user " + user + " is not admin");
                errors.add(msg);
                responseMessage.setErrors(errors);
                responseMessage.setSuccess("FAILED");

            }
        }
        return responseMessage;
    }



    public MatomoResponseVO updateMatomoSiteById(MatomoSiteUpdateRequestVO matomoUpdateRequestVO, String id, List<CollaboratorVO> allCollaborators) {
        GenericMessage responseMessage = new GenericMessage();
        MatomoVO matomoVO = new MatomoVO();
        MatomoVO matomoVOResponse = new MatomoVO();
        MatomoResponseVO responseWrapperVO = new MatomoResponseVO();
        try {
            Optional<MatomoNsql> anyEntity = this.jpaRepo.findById(id);
            if(anyEntity!=null && anyEntity.isPresent()) {
                MatomoNsql entity = anyEntity.get();
                matomoVOResponse = assembler.toVo(entity);
                matomoVO.setId(matomoVOResponse.getId());
                matomoVO.setSiteId(matomoVOResponse.getSiteId());
                matomoVO.setSiteName(matomoUpdateRequestVO.getSiteName());
                matomoVO.setSiteUrl(matomoUpdateRequestVO.getSiteUrl());
                matomoVO.setDivision(matomoUpdateRequestVO.getDivision());
                matomoVO.setSubDivision(matomoUpdateRequestVO.getSubDivision());
                matomoVO.setDepartment(matomoUpdateRequestVO.getDepartment());
                matomoVO.setClassificationType(matomoUpdateRequestVO.getClassificationType());
                matomoVO.setPiiData(matomoUpdateRequestVO.isPiiData());
                matomoVO.setCreatedBy(matomoVOResponse.getCreatedBy());
                matomoVO.setStatus(matomoUpdateRequestVO.getStatus());
                matomoVO.setPermission(matomoUpdateRequestVO.getPermission());
                matomoVO.setCollaborators(allCollaborators);
                matomoVO.setCreatedOn(matomoVOResponse.getCreatedOn());
                matomoVO.setLastModified(matomoVOResponse.getLastModified());
                MatomoNsql updatedEntity =assembler.toEntity(matomoVO);

                this.jpaRepo.save(updatedEntity);
            }
            responseMessage.setSuccess("SUCCESS");
        }catch(Exception e) {
            responseMessage.setSuccess("FAILED");
            List<MessageDescription> errors = new ArrayList<>();
            MessageDescription errMsg = new MessageDescription("Failed to update site details with exception " + e.getMessage());
            log.error("Failed to update site details with exception", e.getMessage());
        }
        responseWrapperVO.setData(matomoVO);
        responseWrapperVO.setResponse(responseMessage);
        return responseWrapperVO;

    }
}



