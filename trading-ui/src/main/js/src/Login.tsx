import { api } from "api";
import { useState } from "react";
import { Alert, Button, Container, Form } from "react-bootstrap";
import { UserCredentials } from "./types";


const Login = ({
  setUserCredentials,
}: {
  setUserCredentials: (userCredentials: Partial<UserCredentials>) => void;
}) => {

  const [invalidCredentials, setInvalidCredentials] = useState<boolean>(false);

  return (
    <Container>
      <Form onSubmit={(data) => onFinish(data, setUserCredentials, setInvalidCredentials)}>
        <Form.Group controlId="username">
          <Form.Label>Username</Form.Label>
          <Form.Control data-test-id="username-input" type="username" name="username" placeholder="Enter username" />
        </Form.Group>
        <Form.Group controlId="password">
          <Form.Label>Password</Form.Label>
          <Form.Control data-test-id="password-input" type="password" name="password" placeholder="Enter password" />
        </Form.Group>
        <Button data-test-id="login-button" variant="primary" type="submit">
          Submit
        </Button>
        {invalidCredentials && <Alert className="mt-1" variant="danger" data-test-id="login-error">Invalid Credentials!</Alert>}
      </Form>
    </Container>
  );
};

const onFinish = (
  data: any,
  setUserCredentials: (data: UserCredentials) => void,
  setInvalidCredentials: (invalid: boolean) => void,
): Promise<any> => {
  data.preventDefault()
  data.stopPropagation()

  const formData = new FormData(data.target)
  const formDataObj = Object.fromEntries(formData.entries())
  const credentials = { username: String(formDataObj.username), password: String(formDataObj.password) }

  console.log(formDataObj)

  return api
    .get("/auth", { auth: credentials })
    .then(response => {
      const success = response.status === 200;
      console.log(success)
      setUserCredentials({ ...credentials, loggedIn: success });
      setInvalidCredentials(!success);

      if (success)
      {
        window.location.href = '/account';
      }
    })
    .catch(() => setInvalidCredentials(true));
};

export default Login;
