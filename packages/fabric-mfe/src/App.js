import React, { useEffect } from 'react';

import { Provider } from 'react-redux';
import store, { history } from './store';

import Routes, { protectedRoutes } from './routes/FabricRoutes';

const App = ({ user, ...rest }) => {
  useEffect(() => {
    const pathName = rest.history?.location?.pathname?.replace('/fabric', '') || '/';
    const isValidRoute = protectedRoutes.find((route) => pathName === route.path)?.path;

    if (user?.roles?.length > 0 && rest.history?.location?.pathname.includes('/fabric')) {
      if (isValidRoute) {
        history.replace(isValidRoute);
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  return (
    <Provider store={store}>
      <Routes user={user} hostHistory={rest.hostHistory}/>
    </Provider>
  );
};

export default App;