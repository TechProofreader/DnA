import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './DeployModal.scss';
// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';

import { CodeSpaceApiClient } from '../../../../services/CodeSpaceApiClient';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import Modal from 'components/formElements/modal/Modal';
import { ICodeSpaceData } from '../CodeSpace';
import { CODE_SPACE_TITLE } from 'globals/constants';
import { Envs } from 'globals/Envs';
import { trackEvent } from '../../../../services/utils';

// import TextBox from '../../shared/textBox/TextBox';

export interface IBranch {
  name: string;
}

export interface IDeployRequest {
  targetEnvironment: string; // int or prod
  branch: string;
  secureWithIAMRequired?: boolean;
  technicalUserDetailsForIAMLogin?: string;
  valutInjectorEnable?: boolean;
}

interface DeployModalProps {
  codeSpaceData: ICodeSpaceData;
  enableSecureWithIAM: boolean;
  setShowCodeDeployModal: (show: boolean) => void;
  startDeployLivelinessCheck?: (workspaceId: string, deployEnvironment: string) => void;
  setCodeDeploying: (codeDeploying: boolean) => void;
  setIsApiCallTakeTime: (apiCallTakeTime: boolean) => void;
  navigateSecurityConfig: () => void;
}

const DeployModal = (props: DeployModalProps) => {
  const [secureWithIAMSelected, setSecureWithIAMSelected] = useState<boolean>(true);
  const [branches, setBranches] = useState<IBranch[]>([]);
  const [branchValue, setBranchValue] = useState('main');
  const [deployEnvironment, setDeployEnvironment] = useState('staging');
  const [vaultEnabled, setVaultEnabled] = useState(false);
  const [acceptContinueCodingOnDeployment, setAcceptContinueCodingOnDeployment] = useState<boolean>(true);
  // const [iamTechnicalUserID, setIAMTechnicalUserID] = useState<string>('');
  // const [iamTechnicalUserIDError, setIAMTechnicalUserIDError] = useState<string>('');

  // const requiredError = '*Missing entry';
  

  const projectDetails = props.codeSpaceData?.projectDetails;

  useEffect(() => {
    // setIAMTechnicalUserID('');
    ProgressIndicator.show();
    CodeSpaceApiClient.getCodeSpacesGitBranchList(projectDetails?.gitRepoName)
      .then((res: any) => {
        ProgressIndicator.hide();
        props.setShowCodeDeployModal(true);
        setBranches(res);
        // setIAMTechnicalUserID(projectDetails?.intDeploymentDetails?.technicalUserDetailsForIAMLogin || '');
        setSecureWithIAMSelected(projectDetails?.intDeploymentDetails?.secureWithIAMRequired || false);
        SelectBox.defaultSetup();
      })
      .catch((err: Error) => {
        ProgressIndicator.hide();
        Notification.show('Error in getting code space branch list - ' + err.message, 'alert');
      });
    setVault();
  }, []);

  useEffect(() => {
    setVault();
  }, [deployEnvironment]);

  const onBranchChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setBranchValue(e.currentTarget.value);
  };

  const onChangeSecureWithIAM = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSecureWithIAMSelected(e.target.checked);
  };

  const onAcceptContinueCodingOnDeployment = (e: React.ChangeEvent<HTMLInputElement>) => {
    setAcceptContinueCodingOnDeployment(e.target.checked);
  };

  const onDeployEnvironmentChange = (evnt: React.FormEvent<HTMLInputElement>) => {
    const deployEnv = evnt.currentTarget.value.trim();
    setDeployEnvironment(deployEnv);
    if (deployEnv === 'staging') {
      setSecureWithIAMSelected(projectDetails?.intDeploymentDetails?.secureWithIAMRequired || false);
      // setIAMTechnicalUserID(projectDetails?.intDeploymentDetails?.technicalUserDetailsForIAMLogin || '');
    } else {
      setSecureWithIAMSelected(projectDetails?.prodDeploymentDetails?.secureWithIAMRequired || false);
      // setIAMTechnicalUserID(projectDetails?.prodDeploymentDetails?.technicalUserDetailsForIAMLogin || '');
    }
  };

  // const onIAMTechnicalUserIDOnChange = (evnt: React.FormEvent<HTMLInputElement>) => {
  //   const iamUserID = evnt.currentTarget.value.trim();
  //   setIAMTechnicalUserID(iamUserID);
  //   setIAMTechnicalUserIDError(iamUserID.length ? '' : requiredError);
  // };

  const setVault = () => {
    ProgressIndicator.show();
    CodeSpaceApiClient.read_secret(
      projectDetails?.projectName.toLowerCase(),
      deployEnvironment === 'staging' ? 'int' : 'prod',
    )
      .then((response) => {
        ProgressIndicator.hide();
        Object.keys(response).length !== 0 ? setVaultEnabled(true) : setVaultEnabled(false);
      })
      .catch((err) => {
        ProgressIndicator.hide();
        // if (err?.response?.data?.errors?.length > 0) {
        //   err?.response?.data?.errors.forEach((err: any) => {
        //     Notification.show(err?.message || 'Something went wrong.', 'alert');
        //   });
        // } else {
        //   Notification.show(err?.message || 'Something went wrong.', 'alert');
        // }
      });
  };

  const onAcceptCodeDeploy = () => {
    // if (secureWithIAMSelected && iamTechnicalUserID.trim() === '') {
    //   setIAMTechnicalUserIDError(requiredError);
    //   return;
    // } else {
    //   setIAMTechnicalUserIDError('');
    // }
    const deployRequest: IDeployRequest = {
      secureWithIAMRequired: secureWithIAMSelected,
      // technicalUserDetailsForIAMLogin: secureWithIAMSelected ? iamTechnicalUserID : null,
      targetEnvironment: deployEnvironment === 'staging' ? 'int' : 'prod', // int or prod
      branch: branchValue,
      valutInjectorEnable: vaultEnabled,
    };
    ProgressIndicator.show();
    CodeSpaceApiClient.deployCodeSpace(props.codeSpaceData.id, deployRequest)
      .then((res: any) => {
        trackEvent('DnA Code Space', 'Deploy', 'Deploy code space');
        if (res.success === 'SUCCESS') {
          // setCreatedCodeSpaceName(res.data.name);
          props.setCodeDeploying(true);
          if (acceptContinueCodingOnDeployment) {
            ProgressIndicator.hide();
            Notification.show(
              `Code space '${projectDetails.projectName}' deployment successfully started. Please check the status later.`,
            );
            props.setShowCodeDeployModal(false);
          } else {
            props.setIsApiCallTakeTime(true);
          }
          props.startDeployLivelinessCheck && props.startDeployLivelinessCheck(props.codeSpaceData.workspaceId, deployEnvironment);
        } else {
          props.setIsApiCallTakeTime(false);
          ProgressIndicator.hide();
          Notification.show(
            'Error in deploying code space. Please try again later.\n' + res.errors[0].message,
            'alert',
          );
        }
      })
      .catch((err: Error) => {
        ProgressIndicator.hide();
        Notification.show('Error in deploying code space. Please try again later.\n' + err.message, 'alert');
      });
  };

  return (
    <Modal
      title={'Deploy Code'}
      showAcceptButton={true}
      acceptButtonTitle={'Deploy'}
      cancelButtonTitle={'Cancel'}
      onAccept={onAcceptCodeDeploy}
      showCancelButton={true}
      modalWidth="600px"
      buttonAlignment="center"
      show={true}
      content={
        <div className={Styles.deployModal}>
          <p>
            The code from your workspace will be deployed and is run in a container and you will get the access url
            after the deployment.
          </p>
          <div className={Styles.flexLayout}>
            <div>
              <div id="branchContainer" className="input-field-group">
                <label id="branchLabel" className="input-label" htmlFor="branchSelect">
                  Code Branch to Deploy
                </label>
                <div id="branch" className="custom-select">
                  <select id="branchSelect" onChange={onBranchChange} value={branchValue}>
                    {branches.map((obj: any) => (
                      <option key={obj.name} id={obj.name + '-branch'} value={obj.name}>
                        {obj.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            </div>
            <div>
              <div id="deployEnvironmentContainer" className="input-field-group">
                <label className="input-label">Deploy Environment</label>
                <div>
                  <label className={classNames('radio')}>
                    <span className="wrapper">
                      <input
                        type="radio"
                        className="ff-only"
                        value="staging"
                        name="deployEnvironment"
                        onChange={onDeployEnvironmentChange}
                        checked={deployEnvironment === 'staging'}
                      />
                    </span>
                    <span className="label">Staging</span>
                  </label>
                  <label className={classNames('radio')}>
                    <span className="wrapper">
                      <input
                        type="radio"
                        className="ff-only"
                        value="production"
                        name="deployEnvironment"
                        onChange={onDeployEnvironmentChange}
                        checked={deployEnvironment === 'production'}
                      />
                    </span>
                    <span className="label">Production</span>
                  </label>
                </div>
              </div>
            </div>
          </div>
          {props.enableSecureWithIAM && (
            <>
              {deployEnvironment === 'staging' && (
                <>
                  <div>
                    <label className="checkbox">
                      <span className="wrapper">
                        <input
                          type="checkbox"
                          className="ff-only"
                          checked={secureWithIAMSelected}
                          onChange={onChangeSecureWithIAM}
                          // disabled={projectDetails?.intDeploymentDetails?.secureWithIAMRequired}
                          disabled={projectDetails?.publishedSecuirtyConfig?.status !== 'PUBLISHED'}
                        />
                      </span>
                      <span className="label">
                        Secure with {Envs.DNA_APPNAME_HEADER} IAM{' '}
                        <span className={classNames(Styles.configLink)} onClick={props.navigateSecurityConfig}>
                          <a target="_blank" rel="noreferrer">
                            {CODE_SPACE_TITLE} (
                            {projectDetails?.publishedSecuirtyConfig?.status ||
                              projectDetails?.securityConfig?.status ||
                              'New'}
                            )
                          </a>
                        </span>
                      </span>
                    </label>
                  </div>
                  {/* {secureWithIAMSelected && (
                <div
                  className={classNames(
                    Styles.flexLayout,
                    projectDetails?.intDeploymentDetails?.secureWithIAMRequired && Styles.disabledDiv,
                  )}
                >
                  <div>
                    <TextBox
                      type="text"
                      controlId={'iamTechnicalUserID'}
                      labelId={'iamTechnicalUserIDLabel'}
                      label={'Technical User ID'}
                      placeholder={'IAM Technical User Id'}
                      value={iamTechnicalUserID}
                      errorText={iamTechnicalUserIDError}
                      required={true}
                      maxLength={7}
                      onChange={onIAMTechnicalUserIDOnChange}
                    />
                  </div>
                  <div className={Styles.createTechUserWrapper}>
                    <a href={IAM_URL} target="_blank" rel="noreferrer">
                      Create a new technical user in IAM (Enabled only with Production IAM)
                    </a>
                  </div>
                </div>
              )} */}
                </>
              )}
              {deployEnvironment === 'production' && (
                <>
                  <div>
                    <label className="checkbox">
                      <span className="wrapper">
                        <input
                          type="checkbox"
                          className="ff-only"
                          checked={secureWithIAMSelected}
                          onChange={onChangeSecureWithIAM}
                          // disabled={projectDetails?.prodDeploymentDetails?.secureWithIAMRequired}
                          disabled={projectDetails?.publishedSecuirtyConfig?.status !== 'PUBLISHED'}
                        />
                      </span>
                      <span className="label">
                        Secure with {Envs.DNA_APPNAME_HEADER} IAM{' '}
                        <span className={classNames(Styles.configLink)} onClick={props.navigateSecurityConfig}>
                          <a target="_blank" rel="noreferrer">
                            {CODE_SPACE_TITLE} (
                            {projectDetails?.publishedSecuirtyConfig?.status ||
                              projectDetails?.securityConfig?.status ||
                              'New'}
                            )
                          </a>
                        </span>
                      </span>
                    </label>
                  </div>
                  {/* {secureWithIAMSelected && (
                <div
                  className={classNames(
                    Styles.flexLayout,
                    projectDetails?.prodDeploymentDetails?.secureWithIAMRequired && Styles.disabledDiv,
                  )}
                >
                  <div>
                    <TextBox
                      type="text"
                      controlId={'iamTechnicalUserID'}
                      labelId={'iamTechnicalUserIDLabel'}
                      label={'Technical User ID'}
                      placeholder={'IAM Technical User Id'}
                      value={iamTechnicalUserID}
                      errorText={iamTechnicalUserIDError}
                      required={true}
                      maxLength={7}
                      onChange={onIAMTechnicalUserIDOnChange}
                    />
                  </div>
                  <div className={Styles.createTechUserWrapper}>
                    <a href={IAM_URL} target="_blank" rel="noreferrer">
                      Create a new technical user in IAM (Enabled only with Production IAM)
                    </a>
                  </div>
                </div>
              )} */}
                </>
              )}
            </>
          )}
          {props.startDeployLivelinessCheck && (
            <div>
              <label className="checkbox">
                <span className="wrapper">
                  <input
                    type="checkbox"
                    className="ff-only"
                    checked={acceptContinueCodingOnDeployment}
                    onChange={onAcceptContinueCodingOnDeployment}
                  />
                </span>
                <span className="label">Continue with your workspace while the deployment is in progress?</span>
              </label>
            </div>
          )}
        </div>
      }
      scrollableContent={false}
      onCancel={() => props.setShowCodeDeployModal(false)}
    />
  );
};

export default DeployModal;