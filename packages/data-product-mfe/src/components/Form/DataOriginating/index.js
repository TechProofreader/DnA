import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from '../Form.common.styles.scss';

import { useFormContext } from 'react-hook-form';
import InfoModal from 'dna-container/InfoModal';

const DataOriginating = ({ onSave }) => {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useFormContext();
  const [showInfoModal, setShowInfoModal] = useState(false);

  return (
    <>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <div>
            <h3>Identifiying Trans-national Data Transfer</h3>
            <div className={Styles.infoIcon}>
              <i className={'icon mbc-icon info'} onClick={() => setShowInfoModal(true)} />
            </div>
          </div>
          <div className={Styles.formWrapper}>
            <div
              className={classNames(
                `input-field-group include-error ${errors?.dataOriginatedFromChina ? 'error' : ''}`,
              )}
              style={{ minHeight: '50px' }}
            >
              <label className={classNames(Styles.inputLabel, 'input-label')}>
                Is data from China included? <sup>*</sup>
              </label>
              <div className={Styles.radioBtns}>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('dataOriginatedFromChina', {
                        required: '*Missing entry',
                      })}
                      type="radio"
                      className="ff-only"
                      name="dataOriginatedFromChina"
                      value="No"
                    />
                  </span>
                  <span className="label">No</span>
                </label>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('dataOriginatedFromChina', {
                        required: '*Missing entry',
                      })}
                      type="radio"
                      className="ff-only"
                      name="dataOriginatedFromChina"
                      value="Yes"
                    />
                  </span>
                  <span className="label">Yes</span>
                </label>
              </div>
              <span className={classNames('error-message')}>{errors?.dataOriginatedFromChina?.message}</span>
            </div>
            <p>
              If yes, and due to the complexity of corresponding Data Governance requirements, we recommend you reach
              out to us at the{' '}
              <a href={'***REMOVED***'} target="_blank" rel="noopener noreferrer">
                Data Governance Office FM
              </a>{' '}
              directly.
            </p>
          </div>
          <div className="btnContainer">
            <button
              className="btn btn-primary"
              type="submit"
              disabled={isSubmitting}
              onClick={handleSubmit((data) => {
                onSave();
                reset(data, {
                  keepDirty: false,
                });
              })}
            >
              Save & Next
            </button>
          </div>
        </div>
      </div>
      {showInfoModal && (
        <InfoModal
          title="Info Modal"
          show={showInfoModal}
          hiddenTitle={true}
          content={<div>Sample Info Modal</div>}
          onCancel={() => setShowInfoModal(false)}
        />
      )}
    </>
  );
};

export default DataOriginating;
