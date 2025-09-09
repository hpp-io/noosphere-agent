import axios from 'axios';
import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { serializeAxiosError } from 'app/shared/reducers/reducer.utils';
import { ApplicationProperties, ValidationResult } from 'app/shared/model/application-configuration.model';

const initialState = {
  loading: false,
  errorMessage: null as unknown as string,
  config: null as ApplicationProperties | null,
  validationResult: null as ValidationResult | null,
  updating: false,
  updateSuccess: false,
  testing: false,
};

export type ApplicationConfigurationState = Readonly<typeof initialState>;

// API actions
export const getConfiguration = createAsyncThunk(
  'applicationConfiguration/fetch_configuration',
  async () => {
    const response = await axios.get<ApplicationProperties>('api/application-configuration');
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

export const updateConfiguration = createAsyncThunk(
  'applicationConfiguration/update_configuration',
  async (config: ApplicationProperties) => {
    const response = await axios.put<ApplicationProperties>('api/application-configuration', config);
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

export const reloadConfiguration = createAsyncThunk(
  'applicationConfiguration/reload_configuration',
  async () => {
    const response = await axios.post<ApplicationProperties>('api/application-configuration/reload', {});
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

export const validateConfiguration = createAsyncThunk(
  'applicationConfiguration/validate_configuration',
  async (config: ApplicationProperties) => {
    const response = await axios.post<ValidationResult>('api/application-configuration/validate', config);
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

export const ApplicationConfigurationSlice = createSlice({
  name: 'applicationConfiguration',
  initialState: initialState as ApplicationConfigurationState,
  reducers: {
    reset() {
      return initialState;
    },
    clearValidationResult(state) {
      state.validationResult = null;
    },
    clearError(state) {
      state.errorMessage = null;
    },
  },
  extraReducers(builder) {
    builder
      .addCase(getConfiguration.pending, state => {
        state.loading = true;
        state.errorMessage = null;
      })
      .addCase(getConfiguration.fulfilled, (state, action) => {
        state.loading = false;
        state.config = action.payload;
        state.errorMessage = null;
      })
      .addCase(getConfiguration.rejected, (state, action) => {
        state.loading = false;
        state.errorMessage = action.error.message;
      })
      .addCase(updateConfiguration.pending, state => {
        state.updating = true;
        state.updateSuccess = false;
        state.errorMessage = null;
      })
      .addCase(updateConfiguration.fulfilled, (state, action) => {
        state.updating = false;
        state.updateSuccess = true;
        state.config = action.payload;
        state.validationResult = { valid: true, message: 'Configuration updated successfully' };
      })
      .addCase(updateConfiguration.rejected, (state, action) => {
        state.updating = false;
        state.updateSuccess = false;
        state.errorMessage = action.error.message;
      })
      .addCase(reloadConfiguration.pending, state => {
        state.loading = true;
        state.errorMessage = null;
      })
      .addCase(reloadConfiguration.fulfilled, (state, action) => {
        state.loading = false;
        state.config = action.payload;
        state.validationResult = { valid: true, message: 'Configuration reloaded successfully' };
      })
      .addCase(reloadConfiguration.rejected, (state, action) => {
        state.loading = false;
        state.errorMessage = action.error.message;
      })
      .addCase(validateConfiguration.pending, state => {
        state.testing = true;
      })
      .addCase(validateConfiguration.fulfilled, (state, action) => {
        state.testing = false;
        state.validationResult = action.payload;
      })
      .addCase(validateConfiguration.rejected, (state, action) => {
        state.testing = false;
        state.errorMessage = action.error.message;
      });
  },
});

export const { reset, clearValidationResult, clearError } = ApplicationConfigurationSlice.actions;

export default ApplicationConfigurationSlice.reducer;
