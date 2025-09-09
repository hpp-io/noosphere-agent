import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import ApplicationConfiguration from './application-configuration';

const ApplicationConfigurationRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<ApplicationConfiguration />} />
  </ErrorBoundaryRoutes>
);

export default ApplicationConfigurationRoutes;
