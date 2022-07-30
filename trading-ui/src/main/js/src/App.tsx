import Login from "./Login"
import { UserCredentials } from "./types"
import { BrowserRouter, Redirect, Route } from "react-router-dom"
import Account from "./Account"
import { useLocalStorage } from "hooks/hooks"
import PrivateRoute from "components/PrivateRoute"
import { TradingScreen } from "TradingScreen"

const App = () => {

  const [userCredentials, setUserCredentials] = useLocalStorage<Partial<UserCredentials>>('user', {})

  return (
    <BrowserRouter>
      <Route
        path="/"
        exact
        component={() => <Login setUserCredentials={setUserCredentials} />}
      />
      <PrivateRoute
        path="/account"
        authenticationPath={"/"}
        isAuthenticated={Boolean(userCredentials.loggedIn)}
        component={() => <Account userCredentials={userCredentials} />}
        exact
      />
      <PrivateRoute
        path="/trade/:product"
        authenticationPath={"/"}
        isAuthenticated={Boolean(userCredentials.loggedIn)}
        component={() => <TradingScreen userCredentials={userCredentials} />}
        exact
      />
      
    </BrowserRouter>
  );
};


export default App
