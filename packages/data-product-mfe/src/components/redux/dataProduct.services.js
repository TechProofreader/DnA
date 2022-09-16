import { createAsyncThunk } from '@reduxjs/toolkit';
import { dataProductsApi } from '../../apis/dataproducts.api';
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { deserializeFormData, serializeDivisionSubDivision, serializeFormData } from '../../Utility/formData';

export const GetDataProducts = createAsyncThunk('products/GetDataProducts', async (arg, { getState }) => {
  ProgressIndicator.show();
  try {
    const res = await dataProductsApi.getAllDataProducts();
    ProgressIndicator.hide();
    const {
      provideDataProducts: { pagination },
    } = getState(); // redux store method
    return {
      data: res.data.records,
      pagination,
    };
  } catch (e) {
    ProgressIndicator.hide();
    Notification.show(e.response?.data?.errors?.[0]?.message || 'Error while fetching data products', 'alert');
  }
});

export const SetDataProducts = createAsyncThunk('products/SetDataProducts', async (data, { rejectWithValue }) => {
  const {
    values,
    onSave,
    provideDataProducts: { divisionList, pagination },
  } = data;

  const division = serializeDivisionSubDivision(divisionList, values);

  const requestBody = serializeFormData(values, division);
  ProgressIndicator.show();
  try {
    const res = await dataProductsApi.createDataProduct(requestBody);
    onSave();
    const data = deserializeFormData(res?.data?.data);
    ProgressIndicator.hide();
    return {
      data,
      pagination,
    };
  } catch (e) {
    ProgressIndicator.hide();
    Notification.show(e?.response?.data?.errors[0]?.message, 'alert');
    return rejectWithValue(e?.response?.data?.errors[0]?.message);
  }
});

export const UpdateDataProducts = createAsyncThunk('products/SetDataProducts', async (data, { rejectWithValue }) => {
  const {
    values,
    onSave,
    provideDataProducts: { divisionList, pagination },
    type, // "provider" form or "consumer" form
    state, // "edit" or "create"
  } = data;

  const isProviderForm = type === 'provider';
  const isEdit = state === 'edit';
  const division = serializeDivisionSubDivision(divisionList, values);
  if (isProviderForm && values.consumer) {
    values.consumer['serializedDivision'] = serializeDivisionSubDivision(divisionList, values?.consumer);
  }
  const requestBody = serializeFormData(values, division, type);
  ProgressIndicator.show();
  try {
    let res = {};
    if (isProviderForm) {
      res = await dataProductsApi.updateProvider(requestBody);
    } else {
      res = await dataProductsApi.updateConsumer(requestBody);
    }
    ProgressIndicator.hide();
    onSave();
    const responseData = res?.data?.data;
    const data = deserializeFormData(responseData, type);

    if (isProviderForm && responseData?.providerInformation?.providerFormSubmitted) {
      if (responseData?.notifyUsers) {
        Notification.show('Information updated sucessfully. Members (if any) will be notified on the data transfer.');
      } else {
        Notification.show(isEdit ? 'Information updated sucessfully.' : 'Progress saved in Data Transfer Overview');
      }
    }

    if (!isProviderForm && responseData?.publish) {
      if (responseData?.notifyUsers) {
        Notification.show('Information updated sucessfully. Members will be notified.');
      } else Notification.show(isEdit ? 'Information updated sucessfully.' : 'Transfer is now complete!');
    }
    return {
      data,
      pagination,
    };
  } catch (e) {
    ProgressIndicator.hide();
    Notification.show(e?.response?.data?.errors[0]?.message, 'alert');
    return rejectWithValue(e?.response?.data?.errors[0]?.message);
  }
});
